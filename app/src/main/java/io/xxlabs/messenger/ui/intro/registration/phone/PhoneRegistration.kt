package io.xxlabs.messenger.ui.intro.registration.phone

import android.app.Application
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.support.dialog.info.SpanConfig
import io.xxlabs.messenger.ui.intro.registration.tfa.TwoFactorAuthCredentials
import javax.inject.Inject

class PhoneRegistration @Inject constructor(
    private val repo: BaseRepository,
    private val scheduler: SchedulerProvider,
    private val application: Application
) : PhoneRegistrationController {

    override val phoneTitle: Spanned = getSpannableTitle()
    override var phone = MutableLiveData<String?>()

    private var country: Country = Country.getDefaultCountry()
        set(value) {
            dialCode.value = (value.flag + value.dialCode)
            field = value
        }

    override val dialCodeUI: LiveData<String> get() = dialCode
    private val dialCode = MutableLiveData(country.flag + country.dialCode)

    override val maxPhoneLength: Int = MAX_EMAIL_LENGTH

    override val phoneError: LiveData<String?> get() = error
    private val error = MutableLiveData<String?>(null)

    override val phoneSkipButtonEnabled: LiveData<Boolean> get() = inputEnabled
    override val phoneInputEnabled: LiveData<Boolean> get() = inputEnabled
    private val inputEnabled = MutableLiveData(true)

    override val phoneNextButtonEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        var phoneEntered = false
        var inputAllowed = true

        addSource(phone) {
            phoneEntered = !it.isNullOrBlank()
            value = phoneEntered && inputAllowed
        }

        addSource(inputEnabled) {
            inputAllowed = it
            value = phoneEntered && inputAllowed
        }
    }

    override val phoneDialogUI: InfoDialogUI by lazy {
        val spanConfig = SpanConfig.create(
            application.getString(R.string.registration_phone_info_link_text),
            application.getString(R.string.registration_phone_info_link_url)
        )
        InfoDialogUI.create(
            title = application.getString(R.string.registration_phone_info_title),
            body = application.getString(R.string.registration_phone_info_body),
            listOf(spanConfig)
        )
    }
    override val phoneInfoClicked: LiveData<Boolean> get() = infoClicked
    private val infoClicked = MutableLiveData(false)

    override val phoneCountryCodeClicked: LiveData<Boolean> get() = countryCodeClicked
    private val countryCodeClicked = MutableLiveData(false)

    override val phoneNavigateNextStep: LiveData<TwoFactorAuthCredentials?> get() = navigateNextStep
    private val navigateNextStep = MutableLiveData<TwoFactorAuthCredentials?>(null)

    override val phoneNavigateSkip: LiveData<Boolean> get() = navigateSkip
    private val navigateSkip = MutableLiveData(false)

    private val phoneUtil: PhoneNumberUtil by lazy {
        PhoneNumberUtil.getInstance()
    }

    override fun onPhoneInfoClicked() {
        if (infoClicked.value == true) return
        infoClicked.value = true
    }

    override fun onPhoneInfoHandled() {
        infoClicked.value = false
    }

    override fun onCountryCodeClicked() {
        countryCodeClicked.value = true
        countryCodeClicked.value = false
    }

    override fun onPhoneCountryCodeSelected(selectedCountry: Country?) {
        country = selectedCountry ?: return
    }

    override fun onPhoneNextClicked() {
        disableUI()
        if (isValidPhoneNumber()) registerPhone(phone.value+country.countryCode)
        else enableUI()
    }

    private fun isValidPhoneNumber(): Boolean {
        return try {
            val numberProto = phoneUtil.parse(
                phone.value,
                country.countryCode
            )
            phoneUtil.isValidNumber(numberProto)
        } catch (e: NumberParseException) {
            false
        }
    }

    private fun disableUI() {
        inputEnabled.value = false
        error.value = null
    }

    private fun enableUI() {
        inputEnabled.value = true
    }

    private fun registerPhone(phone: String) {
        repo.registerUdPhone(phone)
            .subscribeOn(scheduler.single)
            .observeOn(scheduler.main)
            .doOnError { err ->
                enableUI()
                error.postValue(bindingsErrorMessage(err))
            }.doOnSuccess { confirmationId ->
                enableUI()
                requestVerificationCode(confirmationId)
            }.subscribe()
    }

    private fun requestVerificationCode(confirmationId: String) {
        phone.value?.let { phone ->
            navigateNextStep.value = TwoFactorAuthCredentials.create(
                confirmationId = confirmationId,
                factType = FactType.PHONE,
                fact = phone,
                countryCode = country.countryCode
            )
        }
    }

    override fun onPhoneSkipClicked() {
        disableUI()
        phone.value = null
        navigateSkip.value = true
    }

    override fun onPhoneNavigateHandled() {
        enableUI()
        phone.value = null
        navigateNextStep.value = null
        navigateSkip.value = false
    }

    private fun getSpannableTitle(): Spanned {
        val highlight = application.getColor(R.color.brand_default)
        val title = application.getString(R.string.registration_phone_title)
        val startIndex = title.indexOf("phone", ignoreCase = true)

        return SpannableString(title).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex,
                title.length-1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    companion object {
        private const val MAX_EMAIL_LENGTH = 32
    }
}
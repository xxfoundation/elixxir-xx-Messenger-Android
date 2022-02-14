package io.xxlabs.messenger.ui.intro.registration.email

import android.app.Application
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.support.dialog.info.SpanConfig
import io.xxlabs.messenger.ui.intro.registration.tfa.TwoFactorAuthCredentials
import javax.inject.Inject

class EmailRegistration @Inject constructor(
    private val repo: BaseRepository,
    private val scheduler: SchedulerProvider,
    private val application: Application
): EmailRegistrationController {

    override val emailTitle: Spanned = getSpannableTitle()
    override val email = MutableLiveData<String?>()

    override val maxEmailLength: Int = MAX_EMAIL_LENGTH

    override val emailError: LiveData<String?> get() = error
    private val error = MutableLiveData<String?>(null)

    override val emailSkipButtonEnabled: LiveData<Boolean> get() = inputEnabled

    override val emailInputEnabled: LiveData<Boolean> get() = inputEnabled
    private val inputEnabled = MutableLiveData(true)

    override val emailNextButtonEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        var validEmail = false
        var inputAllowed = true

        addSource(email) {
            validEmail = it.isValidEmail()
            value = validEmail && inputAllowed
        }

        addSource(inputEnabled) {
            inputAllowed = it
            value = validEmail && inputAllowed
        }
    }

    override val emailDialogUI: InfoDialogUI by lazy {
        val spanConfig = SpanConfig.create(
            application.getString(R.string.registration_email_info_link_text),
            application.getString(R.string.registration_email_info_link_url)
        )
        InfoDialogUI.create(
            title = application.getString(R.string.registration_email_info_title),
            body = application.getString(R.string.registration_email_info_body),
            listOf(spanConfig)
        )
    }
    override val emailInfoClicked: LiveData<Boolean> get() = infoClicked
    private val infoClicked = MutableLiveData(false)

    override val emailNavigateNextStep: LiveData<TwoFactorAuthCredentials?> get() = navigateNextStep
    private val navigateNextStep = MutableLiveData<TwoFactorAuthCredentials?>(null)

    override val emailNavigateSkip: LiveData<Boolean> get() = navigateSkip
    private val navigateSkip = MutableLiveData(false)

    override fun onEmailInfoClicked() {
        if (infoClicked.value == true) return
        infoClicked.value = true
    }

    override fun onEmailInfoHandled() {
        infoClicked.value = false
    }

    override fun onEmailNextClicked() {
        disableUI()
        email.value?.let {
            if (it.isValidEmail()) registerEmail(it)
            else invalidEmail()
        } ?: invalidEmail()
    }

    private fun String?.isValidEmail() =
        !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun disableUI() {
        inputEnabled.value = false
        error.value = null
    }

    private fun invalidEmail() {
        enableUI()
        error.value = "Invalid email"
    }

    private fun enableUI() {
        inputEnabled.value = true
    }

    private fun registerEmail(email: String) {
        repo.registerUdEmail(email.lowercase())
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
        email.value?.let { email ->
            navigateNextStep.value = TwoFactorAuthCredentials.create(
                confirmationId = confirmationId,
                factType = FactType.EMAIL,
                fact = email
            )
        }
    }

    override fun onEmailSkipClicked() {
        disableUI()
        email.value = null
        navigateSkip.value = true
    }

    override fun onEmailNavigateHandled() {
        enableUI()
        email.value = null
        navigateNextStep.value = null
        navigateSkip.value = false
    }

    private fun getSpannableTitle(): Spanned {
        val highlight = application.getColor(R.color.brand_default)
        val title = application.getString(R.string.registration_email_title)
        val startIndex = title.indexOf("email", ignoreCase = true)

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
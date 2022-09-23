package io.xxlabs.messenger.ui.intro.registration.tfa

import android.app.Application
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.elixxir.feature.registration.registration.tfa.TfaState
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.repository.base.BaseRepository

class PhoneTfaState(
    private val repo: BaseRepository,
    private val scheduler: SchedulerProvider,
    private val application: Application
) : TfaState {

    override val inputEnabled: LiveData<Boolean> get() = phoneCodeInputEnabled
    private val phoneCodeInputEnabled = MutableLiveData(true)

    override val resendEnabled: LiveData<Boolean> get() = phoneRetryEnabled
    private val phoneRetryEnabled = MutableLiveData(false)

    override val navigateNextStep: LiveData<Boolean> get() = phoneNavigateNextStep
    private val phoneNavigateNextStep = MutableLiveData(false)

    override val nextButtonEnabled: LiveData<Boolean> get() = phoneNextButtonEnabled
    private val phoneNextButtonEnabled = MutableLiveData(false)

    override val retryClicked: LiveData<Boolean> get() = _retryClicked
    private val _retryClicked = MutableLiveData(false)

    override val tfaTitle: Spanned by lazy { generateTfaTitle() }

    override var tfaCode: String? = null
        set(value) {
            phoneNextButtonEnabled.value = !value.isNullOrEmpty()
            field = value
        }

    override val tfaError: LiveData<String?> get() = error
    private val error = MutableLiveData<String?>(null)

    override val resendText: LiveData<String> get() = retryText
    private val retryText = MutableLiveData("")
    private var retryTimer: CountDownTimer? = null
    private var retryCredentials: TwoFactorAuthCredentials? = null

    private fun generateTfaTitle(): Spanned {
        if (retryTimer == null) startTimer()

        val factString = application.getString(R.string.registration_phone_2fa_title)
        val highlight = application.getColor(R.color.brand_default)
        val title = application.getString(R.string.registration_2fa_title, factString)

        val startIndex = title.indexOf(factString, ignoreCase = true)
        val endIndex = startIndex + factString.length - 1

        return SpannableString(title).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun onTfaNavigateHandled() {
        resetTimer()
        phoneNavigateNextStep.value = false
        enablePhoneUI()
    }

    override fun onTfaNextClicked(tfaCredentials: TwoFactorAuthCredentials) {
        error.value = null
        disablePhoneUI()
        validateCode(retryCredentials ?: tfaCredentials)
    }

    private fun validateCode(tfaCredentials: TwoFactorAuthCredentials) {
        if (tfaCode.isNullOrEmpty()) {
            enablePhoneUI()
            return
        }

        with (tfaCredentials) {
            repo.confirmFact(confirmationId, tfaCode!!, fact+countryCode, false)
                .subscribeOn(scheduler.single)
                .observeOn(scheduler.main)
                .doOnError { err ->
                    enablePhoneUI()
                    error.postValue(bindingsErrorMessage(err))
                }.doOnSuccess {
                    onValidCode()
                }.subscribe()
        }
    }

    private fun onValidCode() {
        tfaCode = ""
        retryCredentials = null
        phoneNavigateNextStep.value = true
    }

    override fun onResendClicked(credentials: TwoFactorAuthCredentials) {
        tfaCode = ""
        registerPhone(credentials)
        startTimer()
    }

    private fun startTimer() {
        phoneRetryEnabled.value = false

        retryTimer = object : CountDownTimer(
            TfaRegistration.RETRY_COUNTDOWN_MS,
            TfaRegistration.RETRY_COUNTDOWN_INTERVAL_MS
        ) {
            override fun onTick(millisUntilFinished: Long) {
                retryText.postValue(application.getString(
                    R.string.registration_2fa_resend,
                    (millisUntilFinished/1_000)
                ))
            }

            override fun onFinish() {
                phoneRetryEnabled.value = true
            }
        }.start()
    }

    private fun resetTimer() {
        retryTimer?.cancel()
        retryTimer = null
        retryText.value = ""
    }

    private fun updateCredentials(
        credentials: TwoFactorAuthCredentials,
        confirmationId: String
    ): TwoFactorAuthCredentials {
        return TwoFactorAuthCredentials.create(
            confirmationId = confirmationId,
            factType = credentials.factType,
            fact = credentials.fact,
            countryCode = credentials.countryCode
        )
    }

    private fun registerPhone(credentials: TwoFactorAuthCredentials) {
        repo.registerUdPhone(credentials.fact+credentials.countryCode)
            .subscribeOn(scheduler.single)
            .observeOn(scheduler.main)
            .doOnError { err ->
                enablePhoneUI()
                val message =
                    if (err?.localizedMessage?.contains("expired") == true)
                        application.getString(R.string.registration_2fa_code_expired)
                    else err.localizedMessage
                error.postValue(message)
            }.doOnSuccess { confirmationId ->
                enablePhoneUI()
                retryCredentials = updateCredentials(credentials, confirmationId)
            }.subscribe()
    }

    private fun disablePhoneUI() {
        error.value = null
        phoneCodeInputEnabled.value = false
        phoneNextButtonEnabled.value = false
    }


    private fun enablePhoneUI() {
        phoneCodeInputEnabled.value = true
        phoneNextButtonEnabled.value = true
    }
}
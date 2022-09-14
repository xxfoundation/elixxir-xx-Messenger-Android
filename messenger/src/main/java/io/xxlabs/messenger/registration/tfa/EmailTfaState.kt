package io.xxlabs.messenger.ui.intro.registration.tfa

import android.app.Application
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.repository.base.BaseRepository

class EmailTfaState(
    private val repo: BaseRepository,
    private val scheduler: SchedulerProvider,
    private val application: Application
) : TfaState {

    override val inputEnabled: LiveData<Boolean> get() = emailCodeInputEnabled
    private val emailCodeInputEnabled = MutableLiveData(true)

    override val resendEnabled: LiveData<Boolean> get() = emailRetryEnabled
    private val emailRetryEnabled = MutableLiveData(false)

    override val navigateNextStep: LiveData<Boolean> get() = emailNavigateNextStep
    private val emailNavigateNextStep = MutableLiveData(false)

    override val nextButtonEnabled: LiveData<Boolean> get() = emailNextButtonEnabled
    private val emailNextButtonEnabled = MutableLiveData(false)

    override val retryClicked: LiveData<Boolean> get() = _retryClicked
    private val _retryClicked = MutableLiveData(false)

    override val tfaTitle: Spanned by lazy { generateTfaTitle() }

    private var retryCredentials: TwoFactorAuthCredentials? = null

    override val resendText: LiveData<String> get() = retryText
    private val retryText = MutableLiveData("")

    private var retryTimer: CountDownTimer? = null

    override var tfaCode: String? = null
        set(value) {
            emailNextButtonEnabled.value = !value.isNullOrEmpty()
            field = value
        }

    override val tfaError: LiveData<String?> get() = error
    private val error = MutableLiveData<String?>(null)


    private fun generateTfaTitle(): Spanned {
        if (retryTimer == null) startTimer()

        val factString = application.getString(R.string.registration_email_2fa_title)
        val highlight = application.getColor(R.color.brand_default)
        val title = application.getString(R.string.registration_2fa_title, factString)

        val startIndex = title.indexOf(factString, ignoreCase = true)
        val endIndex = startIndex + factString.length-1

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
        emailNavigateNextStep.value = false
        enableEmailUI()
    }

    override fun onTfaNextClicked(tfaCredentials: TwoFactorAuthCredentials) {
        error.value = null
        disableEmailUI()
        validateCode(retryCredentials ?: tfaCredentials)
    }

    private fun validateCode(tfaCredentials: TwoFactorAuthCredentials) {
        if (tfaCode.isNullOrEmpty()) {
            enableEmailUI()
            return
        }

        with (tfaCredentials) {
            repo.confirmFact(confirmationId, tfaCode!!, fact, true)
                .subscribeOn(scheduler.single)
                .observeOn(scheduler.main)
                .doOnError { err ->
                    enableEmailUI()
                    error.postValue(bindingsErrorMessage(err))
                }.doOnSuccess {
                    onValidCode()
                }.subscribe()
        }
    }

    private fun onValidCode() {
        tfaCode = ""
        retryCredentials = null
        emailNavigateNextStep.value = true
    }

    override fun onResendClicked(credentials: TwoFactorAuthCredentials) {
        tfaCode = null
        registerEmail(credentials)
        startTimer()
    }

    private fun startTimer() {
        emailRetryEnabled.value = false

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
                emailRetryEnabled.value = true
            }
        }.start()
    }

    private fun resetTimer() {
        retryTimer?.cancel()
        retryTimer = null
        retryText.value = ""
    }

    private fun registerEmail(tfaCredentials: TwoFactorAuthCredentials) {
        repo.registerUdEmail(tfaCredentials.fact.lowercase())
            .subscribeOn(scheduler.single)
            .observeOn(scheduler.main)
            .doOnError { err ->
                error.postValue(err.localizedMessage)
            }.doOnSuccess { confirmationId ->
                retryCredentials = updateCredentials(tfaCredentials, confirmationId)
            }.subscribe()
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

    private fun disableEmailUI() {
        emailCodeInputEnabled.value = false
        emailNextButtonEnabled.value = false
    }

    private fun enableEmailUI() {
        emailCodeInputEnabled.value = true
        emailNextButtonEnabled.value = true
    }
}
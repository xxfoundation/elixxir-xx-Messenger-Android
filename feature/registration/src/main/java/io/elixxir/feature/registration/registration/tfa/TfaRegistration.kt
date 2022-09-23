package io.xxlabs.messenger.ui.intro.registration.tfa

import android.app.Application
import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.elixxir.feature.registration.registration.tfa.TfaState
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import javax.inject.Inject

class TfaRegistration @Inject constructor(
    private val repo: BaseRepository,
    private val scheduler: SchedulerProvider,
    private val application: Application
) : TfaRegistrationController {

    private lateinit var tfaState: TfaState
    private val phoneTfaState: TfaState by lazy { PhoneTfaState(repo, scheduler, application) }
    private val emailTfaState: TfaState by lazy { EmailTfaState(repo, scheduler, application) }

    override val tfaDialogUI: InfoDialogUI by lazy {
        InfoDialogUI.create(
            title = application.getString(R.string.registration_2fa_info_title),
            body = application.getString(R.string.registration_2fa_info_body),
        )
    }

    override val tfaInfoClicked: LiveData<Boolean> get() = infoClicked
    private val infoClicked = MutableLiveData(false)

    override val tfaRetryClicked: LiveData<Boolean> get() = tfaState.retryClicked

    override var tfaInput: String? = null
        set(value) {
            tfaState.tfaCode = value
            field = value
        }

    override val maxTfaInputLength: Int = MAX_TFA_INPUT_LENGTH

    override val tfaError: LiveData<String?> get() = tfaState.tfaError

    override fun getTfaTitle(tfaCredentials: TwoFactorAuthCredentials): Spanned =
        tfaState.tfaTitle

    override fun getTfaBody(tfaCredentials: TwoFactorAuthCredentials): String {
        return application.getString(
            R.string.registration_2fa_body,
            tfaCredentials.sanitizedFact
        )
    }

    private val TwoFactorAuthCredentials.sanitizedFact: String
        get() = when (factType) {
            FactType.PHONE -> {
                fact.substring(0, 2) + "..." + fact.substring(fact.length - 2, fact.length)
            }
            FactType.EMAIL -> {
                fact.first() + "...@" + fact.substringAfter("@")
            }
            else -> "***"
        }

    override fun isTfaInputEnabled(
        tfaCredentials: TwoFactorAuthCredentials
    ): LiveData<Boolean> = tfaState.inputEnabled

    override fun onTfaNavigateNextStep(
        tfaCredentials: TwoFactorAuthCredentials
    ): LiveData<Boolean> {
        tfaState = when (tfaCredentials.factType) {
            FactType.EMAIL -> emailTfaState
            else -> phoneTfaState
        }
        tfaInput = ""
        return tfaState.navigateNextStep
    }

    override fun isTfaNextButtonEnabled(
        tfaCredentials: TwoFactorAuthCredentials
    ): LiveData<Boolean> = tfaState.nextButtonEnabled

    override fun onTfaNavigateHandled(tfaCredentials: TwoFactorAuthCredentials) =
        tfaState.onTfaNavigateHandled()

    override fun onTfaNextClicked(tfaCredentials: TwoFactorAuthCredentials) =
        tfaState.onTfaNextClicked(tfaCredentials)

    override fun isResendEnabled(
        tfaCredentials: TwoFactorAuthCredentials
    ): LiveData<Boolean> = tfaState.resendEnabled

    override fun onResendClicked(tfaCredentials: TwoFactorAuthCredentials) =
        tfaState.onResendClicked(tfaCredentials)

    override val resendText: LiveData<String> get() = tfaState.resendText

    override fun onTfaInfoClicked() {
        if (infoClicked.value == true) return
        infoClicked.value = true
    }

    override fun onTfaInfoHandled() {
        infoClicked.value = false
    }

    companion object {
        const val MAX_TFA_INPUT_LENGTH = 6
        const val RETRY_COUNTDOWN_MS = 60_000L
        const val RETRY_COUNTDOWN_INTERVAL_MS = 1_000L
    }
}
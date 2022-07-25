package io.xxlabs.messenger.ui.intro.registration.username

import android.app.Application
import android.content.Context
import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.dialog.info.SpanConfig
import io.xxlabs.messenger.ui.global.NetworkViewModel
import kotlinx.coroutines.*
import kotlin.random.Random.Default.nextInt

/**
 * Encapsulates username registration logic.
 */
class UsernameRegistration @AssistedInject constructor(
    private val repo: BaseRepository,
    private val scheduler: SchedulerProvider,
    private val preferences: PreferencesRepository,
    private val application: Application,
    @Assisted private val sessionPassword: ByteArray,
    @Assisted private val networking: NetworkViewModel
) : UsernameRegistrationController {

    private val scope = CoroutineScope(
        CoroutineName("UsernameRegistration")
            + Job()
            + Dispatchers.Default
    )

    private val loggedIn get() = repo.isLoggedIn().blockingGet()

    private val username = MutableLiveData<String?>(null)
    override val usernameTitle: Spanned = getSpannableTitle()
    override val maxUsernameLength: Int = MAX_USERNAME_LENGTH

    // Required by Play Store for Google app review
    private val demoAccount: String
        get() {
            return (1..MAX_USERNAME_LENGTH)
                .map { nextInt(0, DEMO_ACCT_CHARS.size) }
                .map(DEMO_ACCT_CHARS::get)
                .joinToString("")
        }

    override val usernameError: LiveData<String?> get() = error
    private val error = MutableLiveData<String?>(null)

    override val usernameInputEnabled: LiveData<Boolean> get() = inputEnabled
    private val inputEnabled = MutableLiveData(true)

    override val usernameNextButtonEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        var validUsername = false
        var inputAllowed = true

        addSource(username) {
            validUsername = it.isValidUsername()
            value = validUsername && inputAllowed
        }

        addSource(usernameInputEnabled) {
            inputAllowed = it
            value = validUsername && inputAllowed
        }
    }

    override val usernameDialogUI: InfoDialogUI by lazy {
        InfoDialogUI.create(
            title = application.getString(R.string.registration_username_info_title),
            body = application.getString(R.string.registration_username_info_body),
            spans = mutableListOf(
                SpanConfig.create(
                    application.getString(R.string.registration_username_info_dialog_link_text),
                    application.getString(R.string.registration_username_info_dialog_link_url)
                )
            )
        )
    }

    override val usernameInfoClicked: LiveData<Boolean> get() = infoClicked
    private val infoClicked = MutableLiveData(false)

    override val usernameNavigateNextStep: LiveData<String?> get() = navigateNextStep
    private val navigateNextStep = MutableLiveData<String?>(null)

    override val usernameNavigateDemo: LiveData<Boolean> get() = navigateDemoAcct
    private val navigateDemoAcct = MutableLiveData(false)

    override val usernameNavigateRestore: LiveData<Boolean> get() = navigateRestore
    private val navigateRestore = MutableLiveData(false)

    override val usernameFilters: Array<InputFilter> =
        arrayOf(
            InputFilter { source, start, end, _, _, _ ->
                val input = source?.subSequence(start, end)
                val filtered = source?.subSequence(start, end)
                    ?.replace(Regex(USERNAME_FILTER_REGEX), "")
                if (filtered == input) null else filtered
            },
            InputFilter.LengthFilter(MAX_USERNAME_LENGTH)
        )

    override val restoreEnabled: LiveData<Boolean> by ::_restoreEnabled
    private val _restoreEnabled = MutableLiveData(true)

    override fun onUsernameInfoClicked() {
        if (infoClicked.value == true) return
        infoClicked.value = true
    }

    override fun onUsernameInfoHandled() {
        infoClicked.value = false
    }

    override fun onUsernameNextClicked() {
        disableAccountRestore()
        disableUI()
        username.value?.apply {
            when {
                isPlayStoreDemoAccount() -> registerUsername(demoAccount, true)
                isValidUsername() -> registerUsername(this)
                else -> enableUI()
            }
        } ?: enableUI()
    }

    /**
     * Prevent the restore account flow once a username has been submitted.
     * Submitting a username instantiates a Client object that can't be replaced.
     */
    private fun disableAccountRestore() {
        _restoreEnabled.value = false
    }

    override fun onUsernameInput(text: Editable) {
        error.postValue(null)
        username.postValue(text.toString())
    }

    private fun disableUI() {
        inputEnabled.value = false
        error.value = null
    }

    private fun enableUI() {
        inputEnabled.value = true
    }

    override fun onUsernameNavigateHandled() {
        username.postValue(null)
        navigateNextStep.value = null
        navigateDemoAcct.value = false
        navigateRestore.value = false
    }

    private fun String?.isPlayStoreDemoAccount(): Boolean =
        this.equals(PLAY_STORE_DEMO_USERNAME, true)

    private fun String?.isValidUsername(): Boolean {
        if (isNullOrEmpty() || !isMinimumLength()) {
            minimumLengthError()
            return false
        }

        return if (this.matches(USERNAME_VALIDATION_REGEX.toRegex())) {
            error.value = null
            true
        } else {
            invalidUsernameError()
            false
        }
    }

    private fun String.isMinimumLength() = length > MIN_USERNAME_LENGTH

    private fun minimumLengthError() {
        error.value = application.getString(R.string.registration_error_username_min_len)
    }

    private fun invalidUsernameError() {
        error.value = application.getString(R.string.registration_error_username_invalid)
    }

    private fun registerUsername(username: String, isDemoAcct: Boolean = false) {
        if (!loggedIn) {
            getOrCreateSession()
            return
        }

        repo.registerUdUsername(username)
            .subscribeOn(scheduler.single)
            .observeOn(scheduler.main)
            .doOnError {
                it.message?.let { error ->
                    if (error.isNetworkNotHealthyError()) handleNetworkHealthError()
                    else {
                        displayError(error)
                        enableUI()
                    }
                }
            }.doOnSuccess {
                onSuccessfulRegistration(username, isDemoAcct)
            }.subscribe()
    }

    private fun String.isNetworkNotHealthyError() =
        contains("network is not healthy")

    private fun handleNetworkHealthError() {
        onUsernameNextClicked()
    }

    private fun displayError(errorMsg: String) {
        error.postValue(bindingsErrorMessage(Exception(errorMsg)))
    }

    private fun getOrCreateSession(context: Context = appContext()) {
        scope.launch {
            val appFolder = repo.createSessionFolder(context)
            try {
                repo.newClient(appFolder, sessionPassword)
                preferences.lastAppVersion = BuildConfig.VERSION_CODE
                connectToCmix()
            } catch (err: Exception) {
                err.printStackTrace()
                displayError(err.toString())
            }
        }
    }

    private fun connectToCmix() {
        with (networking) {
            checkRegisterNetworkCallback()
            tryStartNetworkFollower { onUsernameNextClicked() }
        }
    }

    private fun onSuccessfulRegistration(username: String, isDemoAcct: Boolean) {
        preferences.name = username
        enableUI()
        if (isDemoAcct) navigateDemoAcct.value = true
        else navigateNextStep.value = username
    }

    private fun getSpannableTitle(): Spanned {
        val highlight = application.getColor(R.color.brand_default)
        val title = application.getString(R.string.registration_username_title)
        val span = application.getString(R.string.registration_username_title_span)
        val startIndex = title.indexOf(span, ignoreCase = true)

        return SpannableString(title).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex,
                startIndex + span.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun onRestoreAccountClicked() {
        if (_restoreEnabled.value == false) {
            error.value = appContext().getString(R.string.registration_restore_disabled_error)
        } else {
            navigateRestore.value = true
        }
    }

    companion object {
        private const val MIN_USERNAME_LENGTH = 3
        private const val MAX_USERNAME_LENGTH = 32
        private const val USERNAME_FILTER_REGEX = "[^a-zA-Z0-9_\\-+@.#]*\$"
        private const val USERNAME_VALIDATION_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+@.#]*[a-zA-Z0-9]\$"
        private const val PLAY_STORE_DEMO_USERNAME = "GPlayStoreDemoAcc"
        private val DEMO_ACCT_CHARS: List<Char> = ('a'..'z') + ('0'..'9')

        fun provideFactory(
            assistedFactory: UsernameRegistrationFactory,
            sessionPassword: ByteArray,
            networking: NetworkViewModel,
        ): UsernameRegistration {
            return assistedFactory.create(sessionPassword, networking)
        }
    }
}

@BindingAdapter("inputFilters")
fun EditText.setInputFilters(filters: Array<InputFilter>) {
    this.filters = filters
}

@AssistedFactory
interface UsernameRegistrationFactory {
    fun create(sessionPassword: ByteArray, networking: NetworkViewModel): UsernameRegistration
}
package io.elixxir.feature.registration.registration.username

import android.content.Context
import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.qualifiers.ApplicationContext
import io.elixxir.core.ui.dialog.info.InfoDialogUi
import io.elixxir.core.ui.dialog.info.SpanConfig
import io.elixxir.core.ui.model.UiText
import io.elixxir.core.ui.util.genericError
import io.elixxir.data.session.SessionRepository
import io.elixxir.data.session.model.SessionState
import io.elixxir.data.userdiscovery.UserRepository
import io.elixxir.feature.registration.R
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.random.Random.Default.nextInt

/**
 * Encapsulates username registration logic.
 */
class UsernameRegistration @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepo: SessionRepository,
    private val userRepo: UserRepository,
) : UsernameRegistrationController {

    private val scope = CoroutineScope(
        CoroutineName("UsernameRegistration")
            + Job()
            + Dispatchers.Default
    )

    private val sessionExists get() = sessionRepo.getSessionState() == SessionState.ExistingUser

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

    override val usernameError: LiveData<UiText?> get() = error
    private val error = MutableLiveData<UiText?>(null)

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

    override val usernameDialogUI: InfoDialogUi by lazy {
        InfoDialogUi.create(
            title = UiText.StringResource(R.string.registration_username_info_title),
            body = UiText.StringResource(R.string.registration_username_info_body),
            spans = mutableListOf(
                SpanConfig.create(
                    UiText.StringResource(R.string.registration_username_info_dialog_link_text),
                    UiText.StringResource(R.string.registration_username_info_dialog_link_url)
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
       return when {
            isNullOrEmpty() || !isMinimumLength() -> {
                minimumLengthError()
                false
            }
            matches(USERNAME_VALIDATION_REGEX.toRegex()) -> {
                error.value = null
                true
            }
            contains(Regex(USERNAME_FILTER_REGEX)) -> {
                invalidCharsInUsernameError()
                false
            }
            else -> {
                invalidUsernameError()
                false
            }
        }
    }

    private fun String.isMinimumLength() = length > MIN_USERNAME_LENGTH

    private fun minimumLengthError() {
        error.value = UiText.StringResource(R.string.registration_error_username_min_len)
    }

    private fun invalidUsernameError() {
        error.value = UiText.StringResource(R.string.registration_error_username_invalid)
    }

    private fun invalidCharsInUsernameError() {
        error.postValue(
            UiText.StringResource(R.string.registration_error_username_invalid_chars)
        )
    }

    private fun registerUsername(username: String, isDemoAcct: Boolean = false) {
        scope.launch {
            userRepo.registerUsername(username).apply {
                if (isSuccess) onSuccessfulRegistration(username, isDemoAcct)
                else error.postValue(
                    UiText.DynamicString(
                    exceptionOrNull()?.message ?: genericError("register username")
                    )
                )
            }
        }
    }

    private fun onSuccessfulRegistration(username: String, isDemoAcct: Boolean) {
        enableUI()
        if (isDemoAcct) navigateDemoAcct.value = true
        else navigateNextStep.value = username
    }

    private fun getSpannableTitle(): Spanned {
        val highlight = context.getColor(R.color.brand_default)
        val title = context.getString(R.string.registration_username_title)
        val span = context.getString(R.string.registration_username_title_span)
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
            error.value = UiText.StringResource(R.string.registration_restore_disabled_error)
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
    }
}

@BindingAdapter("inputFilters")
fun EditText.setInputFilters(filters: Array<InputFilter>) {
    this.filters = filters
}
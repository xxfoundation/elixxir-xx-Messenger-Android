package io.xxlabs.messenger.ui.intro.registration.username

import android.app.Application
import android.text.*
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.support.dialog.info.SpanConfig
import javax.inject.Inject

/**
 * Encapsulates username registration logic.
 */
class UsernameRegistration @Inject constructor(
    private val repo: BaseRepository,
    private val scheduler: SchedulerProvider,
    private val preferences: PreferencesRepository,
    private val application: Application
) : UsernameRegistrationController {

    override val usernameTitle: Spanned = getSpannableTitle()
    override val username = MutableLiveData("")
    override val maxUsernameLength: Int = MAX_USERNAME_LENGTH

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

    override val usernameFilters: Array<InputFilter> get() =
        arrayOf(
            InputFilter { source, start, end, _, _, _ ->
                source?.subSequence(start, end)
                    ?.replace(Regex(USERNAME_FILTER_REGEX), "")
            }
        )

    override fun onUsernameInfoClicked() {
        if (infoClicked.value == true) return
        infoClicked.value = true
    }

    override fun onUsernameInfoHandled() {
        infoClicked.value = false
    }

    override fun onUsernameNextClicked() {
        disableUI()
        username.value?.let {
            if (it.isValidUsername()) registerUsername(it)
            else enableUI()
        } ?: enableUI()
    }

    private fun disableUI() {
        inputEnabled.value = false
        error.value = null
    }

    private fun enableUI() {
        inputEnabled.value = true
    }

    override fun onUsernameNavigateHandled() {
        username.value = null
        navigateNextStep.value = null
    }

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

    private fun registerUsername(username: String) {
        repo.registerUdUsername(username)
            .subscribeOn(scheduler.single)
            .observeOn(scheduler.main)
            .doOnError {
                it.message?.let { error ->
                    displayError(error)
                }
                enableUI()
            }.doOnSuccess {
                onSuccessfulRegistration(username)
            }.subscribe()
    }

    private fun displayError(errorMsg: String) {
        error.postValue(bindingsErrorMessage(Exception(errorMsg)))
    }

    private fun onSuccessfulRegistration(username: String) {
        preferences.name = username
        enableUI()
        navigateNextStep.value = username
    }

    private fun getSpannableTitle(): Spanned {
        val highlight = application.getColor(R.color.brand_default)
        val title = application.getString(R.string.registration_username_title)
        val startIndex = title.indexOf("username", ignoreCase = true)

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
        private const val MIN_USERNAME_LENGTH = 3
        private const val MAX_USERNAME_LENGTH = 32
        private const val USERNAME_FILTER_REGEX = "[^a-zA-Z0-9_\\-+@.#]*\$"
        private const val USERNAME_VALIDATION_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9_\\-+@.#]*[a-zA-Z0-9]\$"
    }
}

@BindingAdapter("inputFilters")
fun EditText.setInputFilters(filters: Array<InputFilter>) {
    this.filters = filters
}
package io.xxlabs.messenger.registration.keygen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.xxlabs.messenger.R
import io.xxlabs.messenger.dialog.info.InfoDialogUi
import io.xxlabs.messenger.dialog.warning.WarningDialogUi
import io.xxlabs.messenger.keystore.KeyStoreManager
import io.xxlabs.messenger.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class KeyGenViewModel(
    private val keyStoreManager: KeyStoreManager
) : ViewModel() {

    val keyGenerationComplete: Flow<Boolean> by :: _keyGenerationComplete
    private val _keyGenerationComplete = MutableStateFlow(false)

    val keyGenError: Flow<WarningDialogUi?> by ::_keyGenError
    private val _keyGenError = MutableStateFlow<WarningDialogUi?>(null)

    init {
        generateSessionPassword()
    }

    private fun generateSessionPassword() {
        viewModelScope.launch {
            with (keyStoreManager.generatePassword()) {
                when {
                    isSuccess -> _keyGenerationComplete.emit(true)
                    isFailure -> _keyGenError.emit(createKeyGenError(exceptionOrNull()))
                }
            }
        }
    }

    private fun createKeyGenError(error: Throwable?) : WarningDialogUi {
        val body = error?.message?.let {
            UiText.DynamicString(it)
        } ?: UiText.StringResource(R.string.keygen_generation_error_body)

        val infoUi = InfoDialogUi.create(
            title = UiText.StringResource(R.string.keygen_generation_error_title),
            body = body,
            spans = null,
            onDismissed = ::onKeyGenErrorButtonClicked
        )

        return WarningDialogUi.create(
            infoDialogUi = infoUi,
            buttonText = UiText.StringResource(R.string.keygen_generation_error_button),
            buttonOnClick = ::onKeyGenErrorButtonClicked
        )
    }

    private fun onKeyGenErrorButtonClicked() {
        generateSessionPassword()
    }
}
package io.elixxir.feature.registration.registration.keygen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.elixxir.core.ui.dialog.info.InfoDialogUi
import io.elixxir.core.ui.dialog.warning.WarningDialogUi
import io.elixxir.core.ui.model.UiText
import io.elixxir.data.session.keystore.KeyStoreManager
import io.elixxir.feature.registration.R
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
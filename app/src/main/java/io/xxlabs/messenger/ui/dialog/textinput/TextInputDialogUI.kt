package io.xxlabs.messenger.ui.dialog.textinput

import android.text.Editable
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI

interface TextInputDialogUI : TwoButtonInfoDialogUI {
    val maxInputLength: Int
    val inputHint: Int
    val inputError: LiveData<String?>
    val positiveButtonEnabled: LiveData<Boolean>
    fun onTextInput(input: Editable)

    companion object Factory {
        fun create(
            maxInputLength: Int,
            inputHint: Int,
            inputError: LiveData<String?>,
            positiveButtonEnabled: LiveData<Boolean>,
            onTextInput: (Editable) -> Unit,
            twoButtonInfoDialogUI: TwoButtonInfoDialogUI,
        ): TextInputDialogUI {
            return object : TextInputDialogUI,
                TwoButtonInfoDialogUI by twoButtonInfoDialogUI {
                override val maxInputLength: Int = maxInputLength
                override val inputHint: Int = inputHint
                override val inputError: LiveData<String?> = inputError
                override val positiveButtonEnabled: LiveData<Boolean> = positiveButtonEnabled
                override fun onTextInput(input: Editable) = onTextInput(input)
            }
        }
    }
}
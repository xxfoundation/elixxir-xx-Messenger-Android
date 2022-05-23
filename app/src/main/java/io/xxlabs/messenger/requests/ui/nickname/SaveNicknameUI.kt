package io.xxlabs.messenger.requests.ui.nickname

import android.text.Editable
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.ui.dialog.components.CloseButtonUI

interface SaveNicknameUI : CloseButtonUI {
    val nicknameHint: LiveData<String>
    val nicknameError: LiveData<String?>
    val maxNicknameLength: Int
    val positiveButtonEnabled: LiveData<Boolean>
    fun onNicknameInput(editable: Editable)
    fun onPositiveClick()
}
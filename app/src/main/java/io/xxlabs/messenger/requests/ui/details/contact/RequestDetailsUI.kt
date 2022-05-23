package io.xxlabs.messenger.requests.ui.details.contact

import android.text.Editable
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.ui.dialog.components.CloseButtonUI
import io.xxlabs.messenger.ui.dialog.components.PositiveNegativeButtonUI

interface RequestDetailsUI : CloseButtonUI, PositiveNegativeButtonUI {
    val username: String
    val email: String?
    val phone: String?
    val nicknameHint: LiveData<String>
    val nicknameError: LiveData<String?>
    val maxNicknameLength: Int
    fun onNicknameInput(editable: Editable)
}
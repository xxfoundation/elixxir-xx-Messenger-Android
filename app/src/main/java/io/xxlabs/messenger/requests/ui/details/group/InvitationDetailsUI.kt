package io.xxlabs.messenger.requests.ui.details.group

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.ui.dialog.components.CloseButtonUI
import io.xxlabs.messenger.ui.dialog.components.PositiveNegativeButtonUI

interface InvitationDetailsUI : CloseButtonUI, PositiveNegativeButtonUI {
    val groupName: String
    val isLoading: LiveData<Boolean>
}
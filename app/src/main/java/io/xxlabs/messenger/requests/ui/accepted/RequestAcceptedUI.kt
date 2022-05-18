package io.xxlabs.messenger.requests.ui.accepted

import io.xxlabs.messenger.ui.dialog.components.CloseButtonUI
import io.xxlabs.messenger.ui.dialog.components.PositiveNegativeButtonUI

interface RequestAcceptedUI : CloseButtonUI, PositiveNegativeButtonUI {
    val title: Int
    val subtitle: String
    val body: Int
}
package io.xxlabs.messenger.ui.main.ud.search

import android.text.Spanned
import androidx.lifecycle.LiveData

interface UdSearchUi {
    val callToActionText: Spanned
    val placeholderText: Spanned
    val placeholderVisible: LiveData<Boolean>
    val isSearching: LiveData<Boolean>

    fun onPlaceholderClicked()
    fun onUsernameSearchClicked()
    fun onEmailSearchClicked()
    fun onPhoneSearchClicked()
    fun onQrCodeSearchClicked()
    fun onCancelSearchClicked()
}
package io.xxlabs.messenger.search

import android.text.Spanned

interface UdSearchUi {
    val callToActionText: Spanned?
    val placeholderText: Spanned?
    val placeholderVisible: Boolean
    val isSearching: Boolean

    fun onPlaceholderClicked()
    fun onCancelSearchClicked()
}

data class SearchUiState(
    override val callToActionText: Spanned? = null,
    override val placeholderText: Spanned? = null,
    override val placeholderVisible: Boolean = false,
    override val isSearching: Boolean = false,
    private val placeHolderClicked: () -> Unit = {},
    private val cancelClicked: () -> Unit = {}
): UdSearchUi {
    override fun onPlaceholderClicked() = placeHolderClicked()
    override fun onCancelSearchClicked() = cancelClicked()
}
package io.xxlabs.messenger.ui.main.contacts.list

import androidx.lifecycle.LiveData

interface ConnectionsListUI {
    val currentLetter: LiveData<String?>
    val scrollBarLetters: String
    val emptyListPlaceholderVisible: LiveData<Boolean>
    val connectionsList: LiveData<List<Connection>>
    fun onAddContactClicked()
}

interface ConnectionsListScrollHandler {
    val connectionsList: LiveData<List<Connection>>
    val scrollToPosition: LiveData<Int?>
    fun onLettersScrolled(top: Int, bottom: Int, currentY: Float)
    fun onScrollStopped()
}
package io.xxlabs.messenger.ui.main.chats

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.ui.main.chats.search.SearchResultItem

interface ChatsListListener {
    val searchResults: LiveData<List<SearchResultItem>>
    val navigateToChat: LiveData<Contact?>
    val navigateToGroup: LiveData<Group?>

    fun onSearchHasFocus(focus: Boolean)
    fun onSearchTextChanged(text: String?)
    fun onAddContactClicked()
    fun onCreateGroupClicked()
}
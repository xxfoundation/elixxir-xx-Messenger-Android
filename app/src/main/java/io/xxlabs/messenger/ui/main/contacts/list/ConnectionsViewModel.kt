package io.xxlabs.messenger.ui.main.contacts.list

import android.graphics.Bitmap
import android.text.SpannedString
import androidx.lifecycle.*
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.data.room.model.resolveBitmap
import io.xxlabs.messenger.data.room.model.thumbnail
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.requests.ui.list.adapter.ItemThumbnail
import io.xxlabs.messenger.support.toolbar.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

class ConnectionsViewModel @Inject constructor(
    val daoRepository: DaoRepository,
): ViewModel(),
    ConnectionListener,
    ToolbarListener,
    MenuItemListener,
    ConnectionsListUI
{

    private val contactItemFlow = daoRepository.getAllAcceptedContactsLive().asFlow().map { list ->
        list.map { contact ->
            ContactItem(contact, this, createThumbnail(contact))
        }
    }

    private val groupItemFlow = daoRepository.getAllAcceptedGroupsLive().asFlow().map { list ->
        list.map { group ->
            GroupItem(group, this, group.thumbnail)
        }
    }

    private val connectionsFlow = contactItemFlow.combine(groupItemFlow) { contacts, groups ->
        (contacts + groups).sortedBy { it.name }
    }

    val contactItems: LiveData<List<ContactItem>> = contactItemFlow.asLiveData()
//    override val connectionsList: LiveData<List<Connection>> = connectionsFlow.asLiveData()

    override val connectionsList: LiveData<List<Connection>> = MutableLiveData(dummyContacts())

    override val emptyListPlaceholderVisible = Transformations.map(connectionsList) { it.isEmpty() }

    override val currentLetter: LiveData<String?> by ::_currentLetter
    private val _currentLetter = MutableLiveData<String?>(null)

    override val scrollBarLetters: String by lazy {
        generateScrollBarLetters()
    }

    private val charList: MutableList<Char> = mutableListOf()

    private fun generateScrollBarLetters(): String {
        var scrollbar = ""
        for (char in 'a'..'z') {
            charList.add(char)
            scrollbar += "${char}\n"
        }
        with ('#') {
            charList.add(this)
            scrollbar += this
        }
        return scrollbar
    }

    val scrollToPosition: LiveData<Int?> by ::_scrollToPosition
    private val _scrollToPosition = MutableLiveData<Int?>(null)

    val navigateToChat: LiveData<Contact?> by ::_navigateToChat
    private val _navigateToChat = MutableLiveData<Contact?>(null)

    val navigateToGroup: LiveData<Group?> by ::_navigateToGroup
    private val _navigateToGroup = MutableLiveData<Group?>(null)

    val navigateToContactSelection: LiveData<Boolean> by ::_navigateToContactSelection
    private val _navigateToContactSelection = MutableLiveData(false)

    val navigateToSearch: LiveData<Boolean> by ::_navigateToSearch
    private val _navigateToSearch = MutableLiveData(false)

    val navigateUp: MutableLiveData<Boolean> by ::_navigateUp
    private val _navigateUp = MutableLiveData(false)

    private val createGroupMenuItem : ToolbarMenuItem by lazy {
        ToolbarItem(
            listener =this,
            id = ITEM_CREATE_GROUP_ID,
            icon = R.drawable.ic_create_group
        )
    }

    private val addContactMenuItem : ToolbarMenuItem by lazy {
        ToolbarItem(
            listener =this,
            id = ITEM_ADD_CONTACT_ID,
            icon = R.drawable.ic_add_contact
        )
    }

    private val menuItems = listOf(createGroupMenuItem, addContactMenuItem)

    val toolbar: ToolbarUI by lazy {
        CustomToolbar(this, SpannedString("Connections"), menuItems)
    }

    private suspend fun createThumbnail(contact: Contact): ItemThumbnail {
        val bitmap = contact.resolveBitmap()
        return object: ItemThumbnail {
            override val itemPhoto: Bitmap? = bitmap
            override val itemIconRes: Int? = null
            override val itemInitials: String = contact.initials
        }
    }

    override fun onClicked(connection: Connection) {
        when (connection) {
            is ContactItem -> onContactClicked(connection.model)
            is GroupItem -> onGroupClicked(connection.model)
        }
    }

    private fun onContactClicked(contact: Contact) {
        _navigateToChat.value = contact
    }

    fun onNavigateToChatHandled() {
        _navigateToChat.value = null
    }

    private fun onGroupClicked(group: Group) {
        _navigateToGroup.value = group
    }

    fun onNavigateToGroupHandled() {
        _navigateToGroup.value = null
    }

    override fun onActionClicked() {
        _navigateUp.value = true
    }

    fun onNavigateUpHandled() {
        _navigateUp.value = false
    }

    override fun onClick(item: ToolbarMenuItem) {
        when (item.id) {
            ITEM_CREATE_GROUP_ID -> onCreateGroupClicked()
            ITEM_ADD_CONTACT_ID -> onAddContactClicked()
        }
    }

    private fun onCreateGroupClicked() {
        _navigateToContactSelection.value = true
    }

    override fun onAddContactClicked() {
        _navigateToSearch.value = true
    }

    fun onContactSelectionNavigationHandled() {
        _navigateToContactSelection.value = false
    }

    fun onSearchNavigationHandled() {
        _navigateToSearch.value = false
    }

    fun onLettersScrolled(top: Int, bottom: Int, currentY: Float) {
        viewModelScope.launch {
            val totalHeight = abs(bottom) - abs(top)
            val relativePosition = currentY / totalHeight
            val letterPosition = (relativePosition * charList.size)
                .toInt()
                .coerceAtMost(charList.size-1)
                .coerceAtLeast(0)

            val letter = charList[letterPosition].toString()
            _currentLetter.postValue(letter)
        }
    }

    fun onScrollStopped() {
        _currentLetter.postValue(null)
    }

    companion object {
        private const val ITEM_CREATE_GROUP_ID = 0
        private const val ITEM_ADD_CONTACT_ID = 1
    }
}
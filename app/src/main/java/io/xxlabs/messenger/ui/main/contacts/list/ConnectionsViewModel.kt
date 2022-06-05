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
import io.xxlabs.messenger.ui.main.contacts.select.SelectedContact
import io.xxlabs.messenger.ui.main.contacts.select.SelectedContactListener
import io.xxlabs.messenger.ui.main.contacts.select.SelectedContactUI
import kotlinx.coroutines.flow.Flow
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
    ConnectionsListUI,
    ConnectionsListScrollHandler,
    SelectedContactListener
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

    private val listPositionMap = mutableMapOf<Char, Int>()

    private val connectionsFlow = contactItemFlow.combine(groupItemFlow) { contacts, groups ->
        (contacts + groups).sortedBy { it.name }.apply {
            mapLetterIndices(map { it.name })
        }
    }

    private fun mapLetterIndices(names: List<String>) {
        names.forEachIndexed { index, name ->
            listPositionMap.putIfAbsent(name[0], index)
        }
    }

    override val connectionsList: LiveData<List<Connection>> = connectionsFlow.asLiveData()

    val selectableContacts: LiveData<List<SelectableContact>> = contactItemFlow.map { list ->
        list.map { contactItem ->
            SelectableContact(contactItem, this@ConnectionsViewModel,)
        }
    }.asLiveData()

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

    override val scrollToPosition: LiveData<Int?> by ::_scrollToPosition
    private val _scrollToPosition = MutableLiveData<Int?>(null)

    val navigateToChat: LiveData<Contact?> by ::_navigateToChat
    private val _navigateToChat = MutableLiveData<Contact?>(null)

    val navigateToGroup: LiveData<Group?> by ::_navigateToGroup
    private val _navigateToGroup = MutableLiveData<Group?>(null)

    val navigateToContactSelection: LiveData<Boolean> by ::_navigateToContactSelection
    private val _navigateToContactSelection = MutableLiveData(false)

    val navigateToSearch: LiveData<Boolean> by ::_navigateToSearch
    private val _navigateToSearch = MutableLiveData(false)

    val navigateUp: LiveData<Boolean> by ::_navigateUp
    private val _navigateUp = MutableLiveData(false)

    val selectedContacts: LiveData<List<SelectedContactUI>> by ::_selectedContacts
    private val _selectedContacts = MutableLiveData<List<SelectedContactUI>>(listOf())
    private val cachedSelections = mutableMapOf<ContactItem, SelectedContactUI>()

    private val newGroupMenuItem : ToolbarMenuItem by lazy {
        ToolbarItem(
            listener =this,
            id = ITEM_NEW_GROUP,
            icon = R.drawable.ic_create_group
        )
    }

    private val addContactMenuItem : ToolbarMenuItem by lazy {
        ToolbarItem(
            listener =this,
            id = ITEM_ADD_CONTACT,
            icon = R.drawable.ic_add_contact
        )
    }

    private val menuItems = listOf(newGroupMenuItem, addContactMenuItem)

    val toolbar: ToolbarUI =
        CustomToolbar(this, SpannedString("Connections"), menuItems)

    private val createGroupMenuItem : ToolbarMenuItem get() =
        ToolbarItem(
            listener = this,
            id = ITEM_CREATE_GROUP,
            label = R.string.connections_create_group,
            enabled = cachedSelections.count() in 2..MAX_GROUP_SIZE
        )

    private val createGroupMenu get() = listOf(createGroupMenuItem)

    private val initialCreateGroupToolbar: ToolbarUI =
        CustomToolbar(this, SpannedString("Add members"), createGroupMenu)

    val createGroupToolbar: LiveData<ToolbarUI> by ::_createGroupToolbar
    private val _createGroupToolbar = MutableLiveData(initialCreateGroupToolbar)

    private fun updateCreateGroupToolbar() {
        _createGroupToolbar.postValue(
            CustomToolbar(
                this,
                SpannedString("Add members (${cachedSelections.count()}/$MAX_GROUP_SIZE)"),
                createGroupMenu
            )
        )
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
            is SelectableContact -> onSelectToggled(connection.contactItem)
            is ContactItem -> onContactClicked(connection.model)
            is GroupItem -> onGroupClicked(connection.model)
        }
    }

    private fun onSelectToggled(contact: ContactItem) {
        if (memberLimitReached()) return

        viewModelScope.launch {
            cachedSelections[contact]?.let {
                deselectContact(contact)
            } ?: selectContact(contact)
            updateCreateGroupToolbar()
        }
    }

    // TODO: Implement onCheckedListener to prevent checking boxes when limit is reached
    private fun memberLimitReached(): Boolean = false

    private fun selectContact(contact: ContactItem) {
        cachedSelections[contact] = contact.selected()
        _selectedContacts.value = cachedSelections.values.toList()
    }

    private fun ContactItem.selected() : SelectedContactUI =
        SelectedContact(this, this@ConnectionsViewModel, thumbnail)

    private fun deselectContact(contact: ContactItem) {
        cachedSelections.remove(contact)?.let {
            _selectedContacts.value = cachedSelections.values.toList()
        }
    }

    override fun onContactRemoved(selection: SelectedContact) {
        viewModelScope.launch {
            deselectContact(selection.contactItem)
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
//        clearSelections()
    }

    private fun clearSelections() {
        cachedSelections.clear()
        _selectedContacts.value = listOf()
        _createGroupToolbar.value = initialCreateGroupToolbar
    }

    fun onNavigateUpHandled() {
        _navigateUp.value = false
    }

    override fun onClick(item: ToolbarMenuItem) {
        when (item.id) {
            ITEM_NEW_GROUP -> onNewGroupClicked()
            ITEM_ADD_CONTACT -> onAddContactClicked()
            ITEM_CREATE_GROUP -> onCreateGroupClicked()
        }
    }

    val createGroupDialog: LiveData<Boolean> by ::_createGroupDialog
    private val _createGroupDialog = MutableLiveData(false)

    private fun onCreateGroupClicked() {
        _createGroupDialog.value = true
    }

    fun onCreateGroupHandled() {
        _createGroupDialog.value = false
    }

    private fun onNewGroupClicked() {
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

    override fun onLettersScrolled(top: Int, bottom: Int, currentY: Float) {
        viewModelScope.launch {
            val totalHeight = abs(bottom) - abs(top)
            val relativePosition = currentY / totalHeight
            val letterPosition = (relativePosition * charList.size)
                .toInt()
                .coerceAtMost(charList.size-1)
                .coerceAtLeast(0)

            with(charList[letterPosition]) {
                updateConnectionsListPosition(this)
                _currentLetter.postValue(toString())
            }
        }
    }

    private fun updateConnectionsListPosition(char: Char) {
        listPositionMap[char]?.let {
            _scrollToPosition.postValue(it)
        }
    }

    override fun onScrollStopped() {
        _currentLetter.postValue(null)
    }

    companion object {
        private const val ITEM_NEW_GROUP = 0
        private const val ITEM_ADD_CONTACT = 1
        private const val ITEM_CREATE_GROUP = 2

        private const val MAX_GROUP_SIZE = 10
    }
}
package io.xxlabs.messenger.ui.main.chats

import android.graphics.Bitmap
import androidx.lifecycle.*
import com.dropbox.core.v2.team.GroupSelector.groupId
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.data.data.ChatWrapper
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.notifications.MessagingService.Companion.notificationCount
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.support.util.value
import io.xxlabs.messenger.support.view.BitmapResolver
import io.xxlabs.messenger.ui.main.chats.data.NewConnectionsDataSource
import io.xxlabs.messenger.ui.main.chats.newConnections.NewConnectionData
import io.xxlabs.messenger.ui.main.chats.newConnections.NewConnectionListener
import io.xxlabs.messenger.ui.main.chats.newConnections.NewConnectionUI
import io.xxlabs.messenger.ui.main.chats.search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.collections.set

class ChatsViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    private val schedulers: SchedulerProvider,
    private val newConnectionsDataSource: NewConnectionsDataSource
) : ViewModel(), ChatsListListener, NewConnectionListener, SearchResultListener {
    var subscriptions = CompositeDisposable()
    var chatsData = MutableLiveData<List<ChatWrapper>>()
    var chats = ChatObservable()
    var mediatorLiveData = MediatorLiveData<Any>()
    var acceptedContacts = daoRepo.getAllAcceptedContactsLive()
    var acceptedGroups = daoRepo.getAllAcceptedGroupsLive()

    override val searchResults: LiveData<List<SearchResultItem>> by ::_searchResults
    private val _searchResults = MutableLiveData<List<SearchResultItem>>(listOf())

    override val navigateToGroup: LiveData<Group?> by ::_navigateToGroup
    private val _navigateToGroup = MutableLiveData<Group?>(null)

    val navigateToUdSearch: LiveData<Boolean> by ::_navigateToUdSearch
    private val _navigateToUdSearch = MutableLiveData(false)

    val showCreateGroup: LiveData<Boolean> by ::_showCreateGroup
    private val _showCreateGroup = MutableLiveData(false)

    override val navigateToChat: LiveData<Contact?> by ::_navigateToChat
    private val _navigateToChat = MutableLiveData<Contact?>(null)

    val newlyAddedContacts: LiveData<List<NewConnectionUI>> by ::_newlyAddedContacts
    private val _newlyAddedContacts = MutableLiveData<List<NewConnectionUI>>(listOf())

    val chatsListUi: LiveData<ChatsListUI> by ::_chatsListUi
    private val _chatsListUi = MutableLiveData<ChatsListUI>(ChatsList(this))

    private var searchHasFocus = false
    private var showingSearchResults = false

    private val contactsCache = mutableMapOf<String, ContactData>()

    private var isPlaceHolderVisible: Boolean = true

    var cachedSearch: String? = null
        private set

    class ChatObservable {
        val chatsHashMap = HashMap<String, ChatWrapper>()

        private fun containsKey(contactId: String): Boolean {
            val map = chatsHashMap
            //Timber.v("Map: $map")
            return map.contains(contactId)
        }

        fun updateLastMessage(group: GroupData, msg: GroupMessageData?) {
            val groupId = group.groupId.toBase64String()
            if (containsKey(groupId)) {
                Timber.v("Contains key $groupId")
                val existingChat = chatsHashMap[groupId]!!
                existingChat.lastMessage = msg
                chatsHashMap[groupId] = existingChat
            } else {
                Timber.v("Does not contain key $groupId")
                chatsHashMap[groupId] = ChatWrapper(group, msg)
            }
        }

        fun updateLastMessage(contact: ContactData, msg: PrivateMessageData?) {
            val contactId = contact.userId.toBase64String()
            if (containsKey(contactId)) {
                Timber.v("Contains key $contactId")
                val existingChat = chatsHashMap[contactId]!!
                existingChat.setPhoto(contact.photo)
                existingChat.updateName(contact.displayName)
                existingChat.lastMessage = msg
                chatsHashMap[contactId] = existingChat
            } else {
                Timber.v("Does not contain key $contactId")
                chatsHashMap[contactId] = ChatWrapper(contact, msg)
            }
        }

        fun updateUnreadCount(contact: ContactData, unreadCount: Int) {
            val contactId = contact.userId.toBase64String()
            if (containsKey(contactId)) {
                Timber.v("Contains key $contactId")
                val existingChat = chatsHashMap[contactId]!!
                existingChat.setPhoto(contact.photo)
                existingChat.updateName(contact.displayName)
                existingChat.unreadCount = unreadCount
                chatsHashMap[contactId] = existingChat
            } else {
                Timber.v("Does not contain key $contactId")
                chatsHashMap[contactId] = ChatWrapper(contact, unreadCount = unreadCount)
            }
        }

        fun updateUnreadCount(group: GroupData, unreadCount: Int) {
            val contactId = group.groupId.toBase64String()
            // This group was just deleted.
            if (!containsKey(contactId) && unreadCount == 0) return

            if (containsKey(contactId)) {
                Timber.v("Contains key $contactId")
                val existingChat = chatsHashMap[contactId]!!
                existingChat.unreadCount = unreadCount
                chatsHashMap[contactId] = existingChat
            } else {
                Timber.v("Does not contain key $contactId")
                chatsHashMap[contactId] = ChatWrapper(group, unreadCount = unreadCount)
            }
        }

        fun getChatsData(): List<ChatWrapper> {
            //Timber.v("Values: $chatsHashMap")
            return chatsHashMap.values.sortedByDescending { chats ->
                chats.getTimestamp()
            }.filter { chat ->
                //Timber.v("Chat: $chat")
                val isTrue = chat.isAccepted() && chat.isNotEmpty()
                Timber.v("is accepted: ${chat.isAccepted()}")
                Timber.v("is notEmpty: ${chat.isNotEmpty()}")
                Timber.v("is true: $isTrue")
                isTrue
            }
        }

        fun deleteAll() {
            chatsHashMap.keys.forEach { key ->
                if (chatsHashMap[key]?.item is ContactData) {
                    chatsHashMap.remove(key)
                } else if (chatsHashMap[key]?.item is GroupData) {
                    chatsHashMap[key]?.lastMessage = null
                }
            }
        }

        fun delete(deletedIds: List<ByteArray>) {
            deletedIds.forEach { chatsHashMap.remove(it.toBase64String()) }
        }
    }

    init {
        mediatorLiveData.addSource(
            acceptedContacts
        ) { contacts ->
            if (contacts.isEmpty()) {
                updateChat(listOf())
            }
            Timber.v("Updated Contacts for Chats")
            //Timber.d("Contacts: $contacts")
            contacts.forEach { contact ->
                mediatorLiveData.addSource(
                    daoRepo.getLastMessageLiveData(contact.userId).distinctUntilChanged()
                ) { msg ->
                    Timber.v("Last message changed for id ${contact.userId.toBase64String()}")
                    chats.updateLastMessage(contact, msg)
                    val values = chats.getChatsData()
                    updateChat(values)
                }

                mediatorLiveData.addSource(
                    daoRepo.getUnreadCountLiveData(contact.userId).distinctUntilChanged()
                ) { unreadCount ->
                    Timber.v("unreadCount changed for id ${contact.userId.toBase64String()}")
                    chats.updateUnreadCount(contact, unreadCount)
                    val values = chats.getChatsData()
                    updateChat(values)
                }
            }
        }

        mediatorLiveData.addSource(
            acceptedGroups
        ) { groups ->
            if (groups.isEmpty()) {
                updateChat(chats.getChatsData())
            }
            Timber.v("Updated Group for Chats")
            groups.forEach { group ->
                mediatorLiveData.addSource(
                    daoRepo.getLastGroupMessageLiveData(group.groupId).distinctUntilChanged()
                ) { msg ->
                    Timber.v("Last message changed for group ${group.groupId.toBase64String()}")
                    chats.updateLastMessage(group, msg)
                    val values = chats.getChatsData()
                    updateChat(values)
                }

                mediatorLiveData.addSource(
                    daoRepo.getGroupUnreadCountLiveData(group.groupId).distinctUntilChanged()
                ) { unreadCount ->
                    Timber.v("unreadCount changed for id ${group.groupId.toBase64String()}")
                    chats.updateUnreadCount(group, unreadCount)
                    val values = chats.getChatsData()
                    updateChat(values)
                }
            }
        }

        fetchNewConnections()
    }

    private fun fetchNewConnections() {
        viewModelScope.launch {
            newConnectionsDataSource.getNewConnections().collect { newConnections ->
                val newConnectionsData = newConnections.mapNotNull {
                    getContact(it.userId)?.let { fetchedContact ->
                        NewConnectionData(
                            this@ChatsViewModel,
                            fetchedContact,
                            resolveBitmap(fetchedContact.photo)
                        )
                    }
                }
                onNewlyAddedListFetched(newConnectionsData)
            }
        }
    }

    private fun onNewlyAddedListFetched(newConnections: List<NewConnectionUI>) {
        _newlyAddedContacts.value = newConnections.sortedByDescending { it.contact.createdAt }
        updateUI()
    }

    private fun isSearchVisible(): Boolean = showingSearchResults || searchHasFocus

    private fun areNewConnectionsVisible(): Boolean =
        if (isSearchVisible()) false
        else _newlyAddedContacts.value?.isNotEmpty() ?: false

    fun setPlaceHolderVisibility(visible: Boolean) {
        isPlaceHolderVisible = visible
        updateUI()
    }

    private fun updateUI() {
        _chatsListUi.value = ChatsList(
            this,
            areNewConnectionsVisible(),
            isSearchVisible(),
            isPlaceHolderVisible && !isSearchVisible()
        )
    }

    override fun onSearchHasFocus(focus: Boolean) {
        searchHasFocus = focus
        updateUI()
    }

    override fun onSearchTextChanged(text: String?) {
        cachedSearch = text
        text?.let {
            if (it.isNotEmpty()) searchFor(it)
            else showingSearchResults = false
        } ?: run { showingSearchResults = false }
        updateUI()
    }

    private fun searchFor(text: String) {
        showingSearchResults = true
        viewModelScope.launch {
            searchChatsFor(text).combine(searchGroupsFor(text)) { matchingChats, matchingGroups ->
                if (matchingChats.isEmpty()) {
                    searchConnectionsFor(text).collect { matchingContact ->
                        val results = matchingChats + matchingGroups + matchingContact
                        _searchResults.postValue(results)
                    }
                } else {
                    val results = matchingChats + matchingGroups
                    _searchResults.postValue(results)
                }
            }.collect()
        }
    }

    private suspend fun searchConnectionsFor(text: String): Flow<List<ConnectionResult>> =
        acceptedContacts.asFlow().map { contacts ->
            contacts.filter { contact ->
                contact.run {
                    listOf(nickname, username, email, phone)
                        .joinToString(" ")
                        .contains(text)
                }
            }.map { matchingContact ->
                matchingContact.toConnectionResult()
            }
        }

    private suspend fun ContactData.toConnectionResult(): ConnectionResult =
        ConnectionResult(
            listener = this@ChatsViewModel,
            model = this,
            thumbnail = generateThumbnail()
        )

    private suspend fun searchChatsFor(text: String): Flow<List<PrivateChatResult>> =
        chatsData.asFlow().map { chats ->
            chats.filter {
                (it.item as? ContactData)?.run {
                    listOf(nickname, username, email, phone)
                        .joinToString(" ")
                        .contains(text)
                } ?: false
            }.map { matchingChat ->
                matchingChat.toPrivateChatResult()
            }
        }

    private suspend fun ChatWrapper.toPrivateChatResult(): PrivateChatResult =
        withContext(Dispatchers.Default) {
            val contact = item as ContactData
            val lastMessageContent: String
            val lastMessageTimeStamp: String
            val notificationCountText: String
            val thumbnail = contact.generateThumbnail()

            (lastMessage as PrivateMessageData).run {
                lastMessageContent = payloadWrapper.text
                notificationCountText = unreadCount.toString()
                lastMessageTimeStamp = getDateText(timestamp)

                PrivateChatResult(
                    listener = this@ChatsViewModel,
                    model = contact,
                    lastMessage = lastMessageContent,
                    thumbnail = thumbnail,
                    timestamp = lastMessageTimeStamp,
                    notificationCount = notificationCountText
                )
            }
        }

    private suspend fun getDateText(timestamp: Long): String = withContext(Dispatchers.Default) {
        val date = Date(timestamp)
        val calendar = Calendar.getInstance().apply {
            time = date
        }

        if (Utils.isToday(calendar)) Utils.getTimestampString(date.time)
        else Utils.calculateGetTimestampString(date.time, "MM/dd/yyyy")
    }

    private suspend fun searchGroupsFor(text: String): Flow<List<GroupChatResult>> {
        val matchingGroupMembers = getGroupMembers().filter {
            it.username?.contains(text) ?: false
        }
        val matchingGroupIds = matchingGroupMembers.map {
            it.groupId
        }

        return chatsData.asFlow().map { chats ->
            chats.filter {
                (it.item as? GroupData)?.run {
                    groupId in matchingGroupIds
                } ?: false
            }.map { matchingGroup ->
                matchingGroup.toGroupChatResult()
            }
        }
    }

    private suspend fun getGroupMembers(): List<GroupMember> = daoRepo.queryAllMembers().value()

    private suspend fun ChatWrapper.toGroupChatResult(): GroupChatResult {
        val group = item as GroupData
        val lastMessageContent: String
        val lastMessageTimeStamp: String
        val notificationCountText: String

        return (lastMessage as GroupMessageData).run {
            lastMessageContent = payloadWrapper.text
            notificationCountText = unreadCount.toString()
            lastMessageTimeStamp = getDateText(timestamp)

            GroupChatResult(
                listener = this@ChatsViewModel,
                model = group,
                lastMessage = lastMessageContent,
                timestamp = lastMessageTimeStamp,
                notificationCount = notificationCountText
            )
        }
    }


    private suspend fun getContact(userId: String): ContactData? =
        contactsCache[userId] ?: daoRepo.getContactByUserId(userId.fromBase64toByteArray())
            .value()
            ?.also { contactsCache[userId] = it }

    private suspend fun resolveBitmap(data: ByteArray?): Bitmap? = withContext(Dispatchers.IO) {
        BitmapResolver.getBitmap(data)
    }

    private fun updateChat(values: List<ChatWrapper>) {
        chatsData.postValue(values)
    }

    override fun onNewConnectionClicked(contact: ContactData) {
        markConnectionAsSeen(contact)
        _navigateToChat.value = contact
    }

    private fun markConnectionAsSeen(contact: ContactData) {
        viewModelScope.launch {
            daoRepo.deleteNewConnection(userId = contact.userId.toBase64String())
        }
    }

    fun onNavigateToChatHandled() {
        _navigateToChat.value = null
    }

    fun onNavigateToGroupHandled() {
        _navigateToGroup.value = null
    }

    override fun onConnectionClicked(contact: Contact) {
        _navigateToChat.value = contact as ContactData
    }

    override fun onGroupChatClicked(group: Group) {
        _navigateToGroup.value = group
    }

    override fun onAddContactClicked() {
        _navigateToUdSearch.value = true
    }

    fun onNavigateToUdHandled() {
        _navigateToUdSearch.value = false
    }

    override fun onCreateGroupClicked() {
        _showCreateGroup.value = true
    }

    fun onCreateGroupHandled() {
        _showCreateGroup.value = false
    }

    fun markAllRead() {
        subscriptions.add(
            daoRepo.markAllMessagesRead()
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribe()
        )
    }

    fun deleteChats(selectedChats: List<ChatWrapper>) {
        val contactIds = selectedChats.filter { it.item !is GroupData }
            .map { it.getItemBindingsId() }
        onDeleteChats(contactIds)
    }

    fun leaveGroups(selectedGroups: List<ChatWrapper>) {
        onLeaveGroups(selectedGroups)
    }

    private fun onDeleteChats(chatIds: List<ByteArray>) {
        subscriptions.add(
            daoRepo.deleteAllMessagesByUserId(chatIds)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onError = { it.printStackTrace() },
                    onSuccess = {
                        chats.delete(chatIds)
                        val values = chats.getChatsData()
                        updateChat(values)
                    }
                )
        )
    }

    private fun onLeaveGroups(groupChats: List<ChatWrapper>) {
        groupChats.filter { it.item is GroupData }
            .map { it.item as GroupData }
            .run { leaveGroupsIterating(this) }
    }

    private fun leaveGroupsIterating(groupsList: List<GroupData>) {
        if (groupsList.isEmpty()) return
        val group = groupsList.first()

        subscriptions.add(repo.leaveGroup(group.groupId)
            .flatMap { daoRepo.deleteGroup(group) }
            .flatMap { daoRepo.deleteAllGroupMessages(group.groupId) }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { /*_errorMessage.value = Exception(it.localizedMessage)*/ },
                onSuccess = {
                    chats.delete(listOf(group.groupId))

                    groupsList.filter { !it.groupId.contentEquals(group.groupId) }
                        .apply { leaveGroupsIterating(this) }
                }
            )
        )
    }

    fun deleteAll() {
        subscriptions.add(
            daoRepo.deleteAllMessages()
                .flatMap {
                    daoRepo.deleteAllGroupMessages()
                }
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onError = {
                        it.printStackTrace()
                    },
                    onSuccess = {
                        chats.deleteAll()
                        Timber.v("All chats were deleted successfully!")
                        chatsData.value = (listOf())
                    }
                )
        )
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}
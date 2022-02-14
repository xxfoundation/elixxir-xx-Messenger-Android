package io.xxlabs.messenger.ui.main.chats

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.data.data.ChatWrapper
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupMessageData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.data.room.model.PrivateMessageData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.extensions.toBase64String
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.set

class ChatsViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    private val schedulers: SchedulerProvider
) : ViewModel() {
    var subscriptions = CompositeDisposable()
    var chatsData = MutableLiveData<List<ChatWrapper>>()
    var chats = ChatObservable()
    var mediatorLiveData = MediatorLiveData<Any>()
    var acceptedContacts = daoRepo.getAllAcceptedContactsLive()
    var acceptedGroups = daoRepo.getAllAcceptedGroupsLive()

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
//            Timber.d("Groups: $groups")
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
    }

    private fun updateChat(values: List<ChatWrapper>) {
        chatsData.postValue(values)
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
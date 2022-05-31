package io.xxlabs.messenger.repository

import androidx.lifecycle.LiveData
import androidx.paging.Config
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.airbnb.lottie.L
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Notification
import io.reactivex.Single
import io.xxlabs.messenger.application.AppDatabase
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.membership.GroupMembershipBase
import io.xxlabs.messenger.data.data.ChatWrapper
import io.xxlabs.messenger.data.data.PayloadWrapper
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.ui.main.chats.newConnections.NewConnection
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class DaoRepository @Inject constructor(
    val db: AppDatabase,
    val schedulers: SchedulerProvider
) {
    private val contactsDao = db.contactsDao()
    private val messagesDao = db.messagesDao()
    private val groupsDao = db.groupsDao()
    private val groupMembersDao = db.groupMembersDao()
    private val groupMessagesDao = db.groupMessagesDao()
    private val newConnectionsDao = db.newConnectionsDao()

    fun getChatDetails(contact: ContactData): Single<Pair<PrivateMessageData?, Int>> {
        return getLastMessage(contact.userId)
            .materialize()
            .zipWith(
                getUnreadCount(contact.userId)
            ) { msg: Notification<PrivateMessageData>, unreadCount: Int ->
                Timber.d("Last msg ${msg.value}")
                Pair(msg.value, unreadCount)
            }.subscribeOn(schedulers.io).observeOn(schedulers.main)
    }

    fun queryAllChatsFlowable(): Flowable<MutableList<ChatWrapper>> =
        contactsDao.queryAllContactsFlowable().flatMap { contacts ->
            Flowable.fromIterable(contacts)
                .flatMapSingle { contact ->
                    getLastMessage(contact.userId)
                        .materialize()
                        .zipWith(
                            getUnreadCount(contact.userId)
                        ) { msg: Notification<PrivateMessageData>, unreadCount: Int ->
                            Timber.v("Last msg ${msg.value}")
                            ChatWrapper(contact, msg.value, unreadCount)
                        }
                }.toList().toFlowable()
        }.subscribeOn(schedulers.io).observeOn(schedulers.main)

    fun deleteAllMessagesByUserId(ids: List<ByteArray>): Single<Int> {
        return messagesDao.deleteChats(ids)
    }

    fun deleteAllMessages(): Single<Int> {
        return messagesDao.deleteAllMessages()
    }

    fun deleteAllGroupMessages(groupId: ByteArray): Single<Int> {
        return groupMessagesDao.deleteAllMessagesFromGroup(groupId)
    }

    fun insertMessage(message: PrivateMessageData): Single<Long> {
        return messagesDao.insertMessage(message)
    }

    fun updateMessage(msg: PrivateMessageData): Single<Int> {
        return messagesDao.updateMessage(msg)
    }

    fun updateMessage(msgId: Long, status: MessageStatus, serverTimestamp: Long?): Single<Int> {
        return if (serverTimestamp == null) {
            messagesDao.updateMessage(msgId, status.value)
        } else {
            messagesDao.updateMessage(msgId, status.value, serverTimestamp)
        }
    }

    fun deleteMessage(msgId: Long): Single<Int> {
        return messagesDao.deleteMessage(msgId)
    }

    fun deleteAllMessages(ids: List<Long>): Single<Int> {
        return messagesDao.deleteAllMessages(ids)
    }

    fun deleteAllMessages(contactId: ByteArray): Single<Int> {
        return messagesDao.deleteAllMessages(contactId)
    }

    fun deleteGroupMessages(ids: List<Long>): Single<Int> {
        return groupMessagesDao.deleteAllMessages(ids)
    }

    fun getMessageTimestamp(lastMessageId: Long): Single<Long> {
        return messagesDao.queryTimestamp(lastMessageId)
    }

    fun markAllMessagesRead(): Single<Int> {
        return messagesDao.markAllRead()
    }

    fun markChatRead(contactId: ByteArray): Single<Int> {
        return messagesDao.markRead(contactId).also {
            deleteNewConnection(userId = contactId.toBase64String())
        }
    }

    fun isUnread(id: Long): Single<Boolean> {
        return messagesDao.isUnread(id)
    }

    fun getMessagesCount(): LiveData<Int> {
        return messagesDao.getMessagesCount()
    }

    fun getUnreadCount(): LiveData<Int> {
        return messagesDao.getUnreadCount()
    }

    fun getUnreadCountSingle(): Single<Int> {
        return messagesDao.getUnreadCountSingle()
    }

    fun getUnreadCount(contactId: ByteArray): Single<Int> {
        return messagesDao.getUnreadCount(contactId)
    }

    fun getUnreadCountLiveData(contactId: ByteArray): LiveData<Int> {
        return messagesDao.getUnreadCountLiveData(contactId)
    }

    fun getGroupUnreadCountLiveData(contactId: ByteArray): LiveData<Int> {
        return groupMessagesDao.getUnreadCountLiveData(contactId)
    }

    fun pendingMessagesToFailed(): Single<Int> {
        return messagesDao.changeAllPendingToFailed()
    }

    fun getMessages(contactId: ByteArray): LiveData<PagedList<PrivateMessageData>> {
        val factory: DataSource.Factory<Int, PrivateMessageData> = messagesDao.queryAllMessages(contactId)
        return LivePagedListBuilder(
            factory, Config(
                pageSize = 6,
                prefetchDistance = 7,
                enablePlaceholders = false
            )
        ).build()
    }

    fun getLastMessagesLiveData(contactIds: List<ContactData>): LiveData<List<PrivateMessageData>> {
        return messagesDao.queryLastMessageLiveData(contactIds.map { it.userId })
    }

    private fun getLastMessage(contactId: ByteArray): Maybe<PrivateMessageData> {
        return messagesDao.queryLastMessage(contactId)
    }

    fun getLastMessageLiveData(): LiveData<PrivateMessageData?> {
        return messagesDao.queryLastMessageLiveData()
    }

    fun getLastMessageLiveData(contactId: ByteArray): LiveData<PrivateMessageData?> {
        return messagesDao.queryLastMessageLiveData(contactId)
    }

    fun getLastGroupMessageLiveData(groupId: ByteArray): LiveData<GroupMessageData?> {
        return groupMessagesDao.queryLastMessageLiveData(groupId)
    }

    fun getMessagesLiveData(contactId: ByteArray): DataSource.Factory<Int, PrivateMessageData> {
        return messagesDao.queryAllMessages(contactId)
    }

    fun getMessageById(messageId: Long): Single<PrivateMessageData> {
        return messagesDao.queryMessageById(messageId)
    }

    fun getContacts(usernameList: List<String>): Single<List<ContactData>> {
        return contactsDao.queryAllContacts(usernameList)
    }

    fun getAllContacts(): Single<List<ContactData>> {
        return contactsDao.queryAllContacts()
    }

    fun getAllContactsLive(): LiveData<List<ContactData>> {
        return contactsDao.getAllContactsLive()
    }

    fun getAllAcceptedContacts(): Flowable<List<ContactData>> {
        return contactsDao.getAllContactsWithStatus(RequestStatus.ACCEPTED.value)
    }

    fun getAllAcceptedContactsLive(): LiveData<List<ContactData>> {
        return contactsDao.getAllContactsWithStatusLive(RequestStatus.ACCEPTED.value)
    }

    fun addNewContact(userId: ByteArray, username: String, name: String = ""): Single<Long> {
        val contact = ContactData(
            userId = userId,
            username = username,
            nickname = name,
            status = RequestStatus.SENT.value
        )
        return contactsDao.insertContact(contact)
    }

    fun addNewContact(contact: ContactData): Single<Long> {
        return contactsDao.insertContact(contact)
    }

    fun updateContactName(temporaryContact: ContactData): Single<Int> {
        return contactsDao.updateContactName(temporaryContact.id, temporaryContact.nickname)
    }

    suspend fun updateContactNickname(contact: ContactData): Int =
        contactsDao.updateContactNickname(contact.userId, contact.nickname)


    fun searchContactByUsernameLikeness(username: String): Single<List<ContactData>> {
        return contactsDao.queryAllContactsUsername(username)
    }

    fun updateContact(contactData: ContactData): Single<Int> {
        return contactsDao.updateContact(contactData)
    }

    fun updateContactState(contactId: Long, requestStatus: RequestStatus): Single<Int> {
        return contactsDao.updateContactState(contactId, requestStatus.value)
    }

    fun updateContactState(userId: ByteArray, requestStatus: RequestStatus): Single<Int> {
        return contactsDao.updateContactState(userId, requestStatus.value).also {
            if (requestStatus == RequestStatus.ACCEPTED) saveNewlyAddedContact(userId)
        }
    }

    private fun saveNewlyAddedContact(userId: ByteArray) {
        newConnectionsDao.insert(NewConnection(userId.toBase64String()))
    }

    fun getNewConnectionsFlow() = newConnectionsDao.getNewConnections()

    fun deleteNewConnection(newConnection: NewConnection? = null, userId: String? = null) {
        try {
            when {
                newConnection != null -> newConnectionsDao.delete(newConnection)
                !userId.isNullOrBlank() -> newConnectionsDao.delete(NewConnection(userId))
            }
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    fun updateGroupState(groupId: ByteArray, requestStatus: RequestStatus): Single<Int> {
        return groupsDao.updateContactState(groupId, requestStatus.value)
    }

    fun getContactById(id: Long): Maybe<ContactData> {
        return contactsDao.queryContactById(id)
    }

    fun getContactByIdForce(id: Long): Single<ContactData> {
        return contactsDao.queryContactByIdForce(id)
    }

    fun getContactByUserId(userId: ByteArray): Maybe<ContactData> {
        return contactsDao.queryContactByUserId(userId).also {
            deleteNewConnection(userId = userId.toBase64String())
        }
    }

    fun getContactFlow(userId: ByteArray): Flow<ContactData> =
        contactsDao.getContactFlow(userId)

    fun getContactByUsername(username: String): Maybe<ContactData> {
        return contactsDao.queryContactByUsername(username)
    }

    private fun getContactByUsernameForce(username: String): Single<ContactData> {
        return contactsDao.queryContactByUsernameForce(username)
    }

    fun setContact(id: Long, marshalledContact: ByteArray): Single<Int> {
        return contactsDao.updateContact(id, marshalledContact)
    }

    fun changeContactName(id: Long, nickname: String): Single<Int> {
        return contactsDao.updateContactName(id, nickname)
    }

    fun changeContactUsername(id: Long, username: String): Single<Int> {
        return contactsDao.updateContactUsername(id, username)
    }

    fun changeContactPhoto(id: ByteArray, photo: ByteArray): Single<Int> {
        return contactsDao.updateContactPhoto(id, photo)
    }

    fun changeContactEmail(id: Long, email: String): Single<Int> {
        return contactsDao.updateContactEmail(id, email)
    }

    fun changeContactPhone(id: Long, phone: String): Single<Int> {
        return contactsDao.updateContactPhone(id, phone)
    }

    fun deleteContactFromDb(contact: ContactData): Single<Int> {
        return contactsDao.deleteContact(contact)
    }

    fun deleteContactFromDb(contactId: ByteArray): Single<Int> {
        return contactsDao.deleteContact(contactId)
    }

    fun deleteContact(
        contact: ContactData
    ): Single<Int> {
        return deleteAllMessages(contact.userId)
            .zipWith(
                deleteContactFromDb(contact),
                { t3: Int, t4: Int ->
                    t3 + t4
                })
    }

    fun deleteAll(): Single<Boolean> {
        return Single.create { emitter ->
            try {
                db.clearAllTables()
                emitter.onSuccess(true)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    fun createUserGroup(
        group: GroupBase,
        status: RequestStatus = RequestStatus.ACCEPTED
    ): Single<Long> {
        val newGroup = if (isMockVersion()) {
            val name = group.getName().decodeToString()
            if (name.endsWith("_RECEIVED")) {
                GroupData(
                    groupId = group.getID(),
                    name = group.getName().decodeToString(),
                    leader = group.getMembership()[1].getID(),
                    serial = group.serialize(),
                    status = RequestStatus.VERIFIED.value
                )
            } else {
                GroupData(
                    groupId = group.getID(),
                    name = group.getName().decodeToString(),
                    leader = group.getMembership()[0].getID(),
                    serial = group.serialize(),
                    status = RequestStatus.ACCEPTED.value
                )
            }
        } else {
            GroupData(
                groupId = group.getID(),
                name = group.getName().decodeToString(),
                leader = group.getMembership()[0].getID(),
                serial = group.serialize(),
                status = status.value
            )
        }
        return db.groupsDao().insertGroup(newGroup)
    }

    fun insertGroupMemberShip(
        groupId: ByteArray,
        membership: GroupMembershipBase,
        userId: ByteArray,
        username: String
    ): Single<Long> {
        val members = membership.getAll().map { groupMember ->
            if (groupMember.getID().contentEquals(userId)) {
                GroupMember(groupId = groupId, userId = groupMember.getID(), username = username)
            } else {
                GroupMember(groupId = groupId, userId = groupMember.getID())
            }
        }
        return Single.create { emitter ->
            try {
                db.groupMembersDao().insertMembers(members)
                emitter.onSuccess(1L)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    fun getGroup(groupId: ByteArray): Single<GroupData> {
        return groupsDao.getGroupByGroupId(groupId)
    }

    fun insertReceivedGroupMessage(
        senderId: ByteArray,
        groupId: ByteArray,
        payloadString: String?,
        timestamp: Long,
        messageId: ByteArray,
        roundUrl: String?
    ): Single<Long> {
        if (payloadString == null) {
            return Single.just(-1)
        }

        val message = GroupMessageData(
            uniqueId = messageId,
            status = MessageStatus.RECEIVED.value,
            groupId = groupId,
            payload = payloadString,
            unread = true,
            sender = senderId,
            receiver = groupId,
            timestamp = timestamp,
            roundUrl = roundUrl
        )

        return groupMessagesDao.insertMessage(message)
    }

    fun insertGroupMessage(
        senderId: ByteArray,
        groupId: ByteArray,
        msg: String?,
        timestamp: Long
    ): Single<Long> {
        if (msg.isNullOrBlank()) {
            return Single.just(-1)
        }

        val payload: String = if (isMockVersion()) {
            PayloadWrapper(msg, null).toString()
        } else {
            ChatMessage.buildCmixMsg(msg, null)
        }

        val message = GroupMessageData(
            status = MessageStatus.PENDING.value,
            groupId = groupId,
            payload = payload,
            unread = false,
            sender = senderId,
            timestamp = timestamp
        )

        return groupMessagesDao.insertMessage(message)
    }

    fun insertGroupMessage(
        msg: GroupMessageData
    ): Single<Long> {
        return groupMessagesDao.insertMessage(msg)
    }

    fun addMember(groupId: ByteArray, userId: ByteArray): Single<Long> {
        return groupMembersDao.insertMember(GroupMember(groupId = groupId, userId = userId))
    }

    fun addAllMembers(groupId: ByteArray, membersList: List<GroupMember>): Single<Boolean> {
        return Single.create { emitter ->
            try {
                membersList.forEach {
                    it.groupId = groupId
                }
                groupMembersDao.insertMembers(membersList)
                emitter.onSuccess(true)
            } catch (err: Exception) {
                Timber.v("[GROUP ADD MEMBERS] All members failed: ${err.localizedMessage}")
                emitter.onError(err)
            }
        }
    }

    fun deleteGroup(groupData: GroupData): Single<Int> {
        return groupsDao.deleteGroup(groupData)
    }

    fun acceptGroup(group: Group): Single<Int> {
        return groupsDao.acceptGroup(group.groupId)
    }

    fun getGroupMessages(groupId: ByteArray): DataSource.Factory<Int, GroupMessageData> {
        return groupMessagesDao.queryAllMessages(groupId)
    }

    fun getAllGroupsLive(): LiveData<List<GroupData>> {
        return groupsDao.getAllGroups()
    }

    fun getAllAcceptedGroupsLive(): LiveData<List<GroupData>> {
        return groupsDao.getAllAcceptedGroupsLive()
    }

    suspend fun getAllGroupRequests() = groupsDao.getAllGroupRequests()

    fun deleteAllGroupMessages(): Single<Int> {
        return groupMessagesDao.deleteAllMessages()
    }

    fun deleteAllMessagesByGroupId(groupIds: List<ByteArray>): Single<Int> {
        return groupMessagesDao.deleteAllMessagesFromGroup(groupIds)
    }

    fun updateGroupMessage(msg: GroupMessageData): Single<Int> {
        return groupMessagesDao.updateMessage(msg)
    }

    fun getAllMembers(groupId: ByteArray): Single<List<GroupMember>> {
        return groupMembersDao.queryMembers(groupId)
    }

    fun getAllMembers(): Single<List<GroupMember>> {
        return groupMembersDao.queryAllMember()
    }

    fun updateMembers(members: List<GroupMember>): Single<Int> {
        return groupMembersDao.updateMember(members)
    }

    fun updateMemberNames(contactsList: List<GroupMember>): Single<Int> {
        return Single.create { emitter ->
            try {
                Timber.v("[GROUP MEMBERS] Updating names...")
                var finished = 0
                contactsList.forEach { member ->
                    val userId = member.userId
                    val username = member.username
                    Timber.v("[GROUP MEMBERS] Updating id $userId: $username")
                    finished = groupMembersDao.updateUsername(userId, username).blockingGet()
                    Timber.v("[GROUP MEMBERS] Update count $finished")
                }
                emitter.onSuccess(finished)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: DaoRepository? = null

        fun getInstance(
            db: AppDatabase,
            schedulers: SchedulerProvider
        ): DaoRepository {
            return instance ?: synchronized(this) {
                val dao = DaoRepository(
                    db,
                    schedulers
                )
                instance = dao
                return dao
            }
        }
    }
}
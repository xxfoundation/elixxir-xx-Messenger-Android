package io.xxlabs.messenger.repository

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import io.reactivex.Maybe
import io.reactivex.Single
import io.xxlabs.messenger.application.AppDatabase
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.membership.GroupMembershipBase
import io.xxlabs.messenger.data.data.PayloadWrapper
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.ui.main.chats.newConnections.NewConnection
import kotlinx.coroutines.*
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
    
    private val scope = CoroutineScope(
        CoroutineName("DaoRepo")
                + Job()
                + Dispatchers.Default
    )

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
        return messagesDao.insertMessage(message).also {
            val senderId = message.sender.toBase64String()
            deleteNewConnection(userId = senderId)
        }
    }

    fun updateMessage(msg: PrivateMessageData): Single<Int> {
        return messagesDao.updateMessage(msg)
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

    fun markAllMessagesRead(): Single<Int> {
        return messagesDao.markAllRead()
    }

    fun markChatRead(contactId: ByteArray): Single<Int> {
        return messagesDao.markRead(contactId)
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

    fun getContacts(usernameList: List<String>): Single<List<ContactData>> {
        return contactsDao.queryAllContacts(usernameList)
    }

    fun getAllContacts(): Single<List<ContactData>> {
        return contactsDao.queryAllContacts()
    }

    fun getAllContactsLive(): LiveData<List<ContactData>> {
        return contactsDao.getAllContactsLive()
    }

    fun getAllAcceptedContactsLive(): LiveData<List<ContactData>> {
        return contactsDao.getAllContactsWithStatusLive(RequestStatus.ACCEPTED.value)
    }

    fun addNewContact(contact: ContactData): Single<Long> {
        return contactsDao.insertContact(contact)
    }

    fun updateContactName(temporaryContact: ContactData): Single<Int> {
        return contactsDao.updateContactName(temporaryContact.id, temporaryContact.nickname)
    }

    suspend fun updateContactNickname(contact: ContactData): Int =
        contactsDao.updateContactNickname(contact.userId, contact.nickname)

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
        scope.launch {
            newConnectionsDao.insert(NewConnection(userId.toBase64String()))
        }
    }

    fun getNewConnectionsFlow() = newConnectionsDao.getNewConnections()

    fun deleteNewConnection(newConnection: NewConnection? = null, userId: String? = null) {
        scope.launch {
            try {
                when {
                    newConnection != null -> newConnectionsDao.delete(newConnection)
                    !userId.isNullOrBlank() -> newConnectionsDao.delete(NewConnection(userId))
                }
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    fun updateGroupState(groupId: ByteArray, requestStatus: RequestStatus): Single<Int> {
        return groupsDao.updateContactState(groupId, requestStatus.value)
    }

    fun getContactByUserId(userId: ByteArray): Maybe<ContactData> {
        return contactsDao.queryContactByUserId(userId)
    }

    fun setContact(id: Long, marshalledContact: ByteArray): Single<Int> {
        return contactsDao.updateContact(id, marshalledContact)
    }

    fun changeContactPhoto(id: ByteArray, photo: ByteArray): Single<Int> {
        return contactsDao.updateContactPhoto(id, photo)
    }

    fun deleteContactFromDb(contact: ContactData): Single<Int> {
        return contactsDao.deleteContact(contact).also {
            deleteNewConnection(userId = contact.userId.toBase64String())
        }
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

    fun updateGroupMessage(msg: GroupMessageData): Single<Int> {
        return groupMessagesDao.updateMessage(msg)
    }

    fun queryAllMembers(): Single<List<GroupMember>> = groupMembersDao.queryAllMember()

    fun getAllMembers(groupId: ByteArray): Single<List<GroupMember>> {
        return groupMembersDao.queryMembers(groupId)
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
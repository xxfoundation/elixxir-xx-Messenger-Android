package io.xxlabs.messenger.data.room.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single
import io.xxlabs.messenger.data.room.model.GroupMessageData

@Dao
interface GroupMessagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: GroupMessageData): Single<Long>

    @Update
    fun updateMessage(message: GroupMessageData): Single<Int>

    @Query("UPDATE GroupMessages SET timestamp = :timestamp, status = :status WHERE id = :msgId")
    fun updateMessage(
        msgId: Long,
        status: Int,
        timestamp: Long
    ): Single<Int>

    @Query("UPDATE GroupMessages SET status = :status WHERE id = :id")
    fun updateMessage(
        id: Long,
        status: Int
    ): Single<Int>

    @Query("UPDATE GroupMessages SET unread = 0 WHERE unread != 0")
    fun markAllRead(): Single<Int>

    @Query("UPDATE GroupMessages SET unread = 0 WHERE sender == :contactId OR receiver == :contactId")
    fun markRead(contactId: ByteArray): Single<Int>

    @Query("SELECT GroupMessages.unread FROM GroupMessages WHERE id = :id")
    fun isUnread(id: Long): Single<Boolean>

    @Query("SELECT COUNT(GroupMessages.unread) FROM GroupMessages WHERE unread = 1")
    fun getUnreadCount(): LiveData<Int>

    @Query("SELECT COUNT(GroupMessages.unread) FROM GroupMessages WHERE (sender = :contactId OR receiver = :contactId) AND unread = 1")
    fun getUnreadCount(contactId: ByteArray): Single<Int>

    @Query("SELECT COUNT(GroupMessages.unread) FROM GroupMessages WHERE groupId = :groupId AND unread = 1")
    fun getUnreadCountLiveData(groupId: ByteArray): LiveData<Int>

    @Query("SELECT COUNT(GroupMessages.unread) FROM GroupMessages WHERE unread = 1")
    fun getUnreadCountSingle(): Single<Int>

    @Query("SELECT * FROM GroupMessages WHERE groupId == :groupId")
    fun getAllGroupMessages(groupId: ByteArray): LiveData<List<GroupMessageData>>

    @Query("SELECT COUNT(GroupMessages.id) FROM GroupMessages")
    fun getMessagesCount(): LiveData<Int>

    @Query("SELECT COUNT(GroupMessages.id) FROM GroupMessages")
    fun getMessagesCountSingle(): LiveData<Int>

    @Delete
    fun deleteMessage(message: GroupMessageData): Single<Int>

    @Query("DELETE FROM GroupMessages WHERE id = :id")
    fun deleteMessage(id: Long): Single<Int>

    @Query("DELETE FROM GroupMessages WHERE groupId IN (:groupId)")
    fun deleteAllMessagesFromGroup(groupId: List<ByteArray>): Single<Int>

    @Query("DELETE FROM GroupMessages WHERE groupId = :groupId")
    fun deleteAllMessagesFromGroup(groupId: ByteArray): Single<Int>

    @Query("DELETE FROM GroupMessages WHERE GroupMessages.id IN (:ids)")
    fun deleteAllMessages(ids: List<Long>): Single<Int>

    @Query("DELETE FROM GroupMessages WHERE GroupMessages.sender IN (:ids) OR GroupMessages.receiver IN (:ids)")
    fun deleteChats(ids: List<ByteArray>): Single<Int>

    @Query("DELETE FROM GroupMessages")
    fun deleteAllMessages(): Single<Int>

    @Query("SELECT * FROM GroupMessages WHERE groupId == :groupdId")
    fun queryAllMessagesLiveData(groupdId: ByteArray): LiveData<List<GroupMessageData>>

    @Query("SELECT * FROM GroupMessages")
    fun queryAllMessagesLiveData(): Single<List<GroupMessageData>>

    @Query("SELECT * FROM GroupMessages WHERE sender == :contactId OR receiver == :contactId ORDER BY timestamp DESC")
    fun queryAllMessages(contactId: ByteArray): DataSource.Factory<Int, GroupMessageData>

    @Query("UPDATE GroupMessages SET status = 3 WHERE status == 0")
    fun changeAllPendingToFailed(): Single<Int>

    @Query("SELECT timestamp FROM GroupMessages WHERE id = :id")
    fun queryTimestamp(id: Long): Single<Long>

    @Query("SELECT * FROM GroupMessages WHERE id = :id")
    fun queryMessageById(id: Long): Single<GroupMessageData>

    @Query("SELECT * FROM GroupMessages WHERE sender = :contactId OR receiver == :contactId ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessage(contactId: ByteArray): Maybe<GroupMessageData>

    @Query("SELECT * FROM GroupMessages WHERE sender IN (:contactsIds) OR receiver IN (:contactsIds) ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessage(contactsIds: List<ByteArray>): Single<List<GroupMessageData>>

    @Query("SELECT * FROM GroupMessages WHERE groupId = :groupId ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessageLiveData(groupId: ByteArray): LiveData<GroupMessageData?>

    @Query("SELECT * FROM GroupMessages ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessageLiveData(): LiveData<GroupMessageData?>

    @Query("SELECT * FROM GroupMessages WHERE sender IN (:contactsIds) OR receiver IN (:contactsIds) ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessageLiveData(contactsIds: List<ByteArray>): LiveData<List<GroupMessageData>>
}
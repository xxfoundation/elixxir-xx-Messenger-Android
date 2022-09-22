package io.xxlabs.messenger.data.room.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single
import io.xxlabs.messenger.data.room.model.PrivateMessageData

@Dao
interface MessagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: PrivateMessageData): Single<Long>

    @Update
    fun updateMessage(message: PrivateMessageData): Single<Int>

    @Query("UPDATE Messages SET timestamp = :timestamp, status = :status WHERE id = :msgId")
    fun updateMessage(
        msgId: Long,
        status: Int,
        timestamp: Long
    ): Single<Int>

    @Query("UPDATE Messages SET status = :status WHERE id = :id")
    fun updateMessage(
        id: Long,
        status: Int
    ): Single<Int>

    @Query("UPDATE Messages SET unread = 0 WHERE unread != 0")
    fun markAllRead(): Single<Int>

    @Query("UPDATE Messages SET unread = 0 WHERE sender == :contactId OR receiver == :contactId")
    fun markRead(contactId: ByteArray): Single<Int>

    @Query("SELECT Messages.unread FROM Messages WHERE id = :id")
    fun isUnread(id: Long): Single<Boolean>

    @Query("SELECT COUNT(Messages.unread) FROM Messages WHERE unread = 1")
    fun getUnreadCount(): LiveData<Int>

    @Query("SELECT COUNT(Messages.unread) FROM Messages WHERE (sender = :contactId OR receiver = :contactId) AND unread = 1")
    fun getUnreadCount(contactId: ByteArray): Single<Int>

    @Query("SELECT COUNT(Messages.unread) FROM Messages WHERE (sender = :contactId OR receiver = :contactId) AND unread = 1")
    fun getUnreadCountLiveData(contactId: ByteArray): LiveData<Int>

    @Query("SELECT COUNT(Messages.unread) FROM Messages WHERE unread = 1")
    fun getUnreadCountSingle(): Single<Int>

    @Query("SELECT COUNT(Messages.id) FROM Messages")
    fun getMessagesCount(): LiveData<Int>

    @Query("SELECT COUNT(Messages.id) FROM Messages")
    fun getMessagesCountSingle(): LiveData<Int>

    @Delete
    fun deleteMessage(message: PrivateMessageData): Single<Int>

    @Query("DELETE FROM Messages WHERE id = :id")
    fun deleteMessage(id: Long): Single<Int>

    @Query("DELETE FROM Messages WHERE receiver = :userId OR sender = :userId")
    fun deleteAllMessages(userId: ByteArray): Single<Int>

    @Query("DELETE FROM Messages WHERE Messages.id IN (:ids)")
    fun deleteAllMessages(ids: List<Long>): Single<Int>

    @Query("DELETE FROM Messages WHERE Messages.sender IN (:ids) OR Messages.receiver IN (:ids)")
    fun deleteChats(ids: List<ByteArray>): Single<Int>

    @Query("DELETE FROM Messages")
    fun deleteAllMessages(): Single<Int>

    @Query("SELECT * FROM Messages WHERE sender == :contactId OR receiver == :contactId")
    fun queryAllMessagesLiveData(contactId: ByteArray): LiveData<List<PrivateMessageData>>

    @Query("SELECT * FROM Messages")
    fun queryAllMessagesLiveData(): Single<List<PrivateMessageData>>

    @Query("SELECT * FROM Messages WHERE sender == :contactId OR receiver == :contactId ORDER BY timestamp DESC")
    fun queryAllMessages(contactId: ByteArray): DataSource.Factory<Int, PrivateMessageData>

    @Query("UPDATE Messages SET status = 3 WHERE status == 0")
    fun changeAllPendingToFailed(): Single<Int>

    @Query("SELECT timestamp FROM Messages WHERE id = :id")
    fun queryTimestamp(id: Long): Single<Long>

    @Query("SELECT * FROM Messages WHERE id = :id")
    fun queryMessageById(id: Long): Single<PrivateMessageData>

    @Query("SELECT * FROM Messages WHERE sender = :contactId OR receiver == :contactId ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessage(contactId: ByteArray): Maybe<PrivateMessageData>

    @Query("SELECT * FROM Messages WHERE sender IN (:contactsIds) OR receiver IN (:contactsIds) ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessage(contactsIds: List<ByteArray>): Single<List<PrivateMessageData>>

    @Query("SELECT * FROM Messages WHERE sender = :contactId OR receiver == :contactId ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessageLiveData(contactId: ByteArray): LiveData<PrivateMessageData?>

    @Query("SELECT * FROM Messages ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessageLiveData(): LiveData<PrivateMessageData?>

    @Query("SELECT * FROM Messages WHERE sender IN (:contactsIds) OR receiver IN (:contactsIds) ORDER BY timestamp DESC LIMIT 1")
    fun queryLastMessageLiveData(contactsIds: List<ByteArray>): LiveData<List<PrivateMessageData>>
}
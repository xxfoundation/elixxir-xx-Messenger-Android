package io.xxlabs.messenger.data.room.dao

import androidx.room.*
import io.xxlabs.messenger.data.room.model.RequestData
import kotlinx.coroutines.flow.Flow

@Dao
interface RequestsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: RequestData): Long

    @Update
    suspend fun update(request: RequestData): Int

    @Delete
    suspend fun delete(request: RequestData): Int

    @Query("SELECT * FROM Requests")
    fun getAllRequests(): Flow<List<RequestData>>

    @Query("SELECT * FROM Requests WHERE requestId IN (SELECT userId FROM Contacts)")
    fun getContactRequests(): Flow<List<RequestData>>

    @Query("SELECT * FROM Requests WHERE requestId IN (SELECT userId FROM Contacts)")
    fun getContactRequestsOnce(): List<RequestData>

    @Query("SELECT * FROM Requests WHERE requestId IN (SELECT groupId FROM Groups)")
    fun getGroupInvitations(): Flow<List<RequestData>>

    @Query("SELECT * FROM Requests WHERE requestId = :requestId")
    suspend fun getRequest(requestId: ByteArray): List<RequestData>

    @Query("SELECT * FROM Requests WHERE unread = :unread")
    fun unreadRequestsFlow(unread: Boolean = true): Flow<List<RequestData>>
}
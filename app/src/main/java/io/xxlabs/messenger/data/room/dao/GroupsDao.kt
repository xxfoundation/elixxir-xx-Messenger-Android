package io.xxlabs.messenger.data.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import io.reactivex.Single
import io.xxlabs.messenger.data.room.model.GroupData

@Dao
interface GroupsDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertGroup(group: GroupData): Single<Long>

    @Update
    fun updateGroup(group: GroupData): Single<Int>

    @Delete
    fun deleteGroup(group: GroupData): Single<Int>

    @Query("DELETE FROM Groups WHERE id = :id")
    fun deleteGroupById(id: Long): Single<Int>

    @Query("SELECT * FROM Groups WHERE groupId = :groupId LIMIT 1")
    fun getGroupByGroupId(groupId: ByteArray): Single<GroupData>

    @Query("SELECT * FROM Groups")
    fun getAllGroups(): LiveData<List<GroupData>>

    @Query("SELECT * FROM Groups WHERE status == 2")
    fun getAllAcceptedGroupsLive(): LiveData<List<GroupData>>

    @Query("UPDATE Groups SET status = 2 WHERE groupId == :groupId")
    fun acceptGroup(groupId: ByteArray): Single<Int>
}
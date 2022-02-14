package io.xxlabs.messenger.data.room.dao

import androidx.room.*
import io.reactivex.Single
import io.xxlabs.messenger.data.room.model.GroupMember


@Dao
interface GroupMembersDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMember(member: GroupMember): Single<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertMembers(membersList: List<GroupMember>)

    @Update
    fun updateMember(member: GroupMember): Single<Int>

    @Update
    fun updateMember(members: List<GroupMember>): Single<Int>

    @Delete
    fun deleteMember(member: GroupMember): Single<Int>

    @Query("DELETE FROM GroupMembers WHERE member = :userId")
    fun deleteMember(userId: ByteArray): Single<Int>

    @Query("SELECT * FROM GroupMembers")
    fun queryAllMember(): Single<List<GroupMember>>

    @Query("SELECT * FROM GroupMembers WHERE groupId = :groupId")
    fun queryMembers(groupId: ByteArray): Single<List<GroupMember>>

    @Query("UPDATE GroupMembers SET username = :username WHERE member = :memberId")
    fun updateUsername(memberId: ByteArray, username: String?): Single<Int>
}
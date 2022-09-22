package io.xxlabs.messenger.data.room.dao

import androidx.room.*
import io.xxlabs.messenger.ui.main.chats.newConnections.NewConnection
import kotlinx.coroutines.flow.Flow

@Dao
interface NewConnectionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(newConnection: NewConnection): Long

    @Delete
    fun delete(newConnection: NewConnection): Int

    @Query("SELECT * FROM NewConnections")
    fun getNewConnections(): Flow<List<NewConnection>>
}
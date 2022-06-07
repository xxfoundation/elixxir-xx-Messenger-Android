package io.xxlabs.messenger.ui.main.chats.data

import io.xxlabs.messenger.ui.main.chats.newConnections.NewConnection
import kotlinx.coroutines.flow.Flow

interface NewConnectionsDataSource {
    suspend fun getNewConnections(): Flow<List<NewConnection>>
    fun deleteNewConnection(newConnection: NewConnection)
}
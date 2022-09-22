package io.xxlabs.messenger.ui.main.chats.data

import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.ui.main.chats.newConnections.NewConnection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewConnectionsRepository @Inject constructor(
    private val daoRepository: DaoRepository
) : NewConnectionsDataSource {

    override suspend fun getNewConnections(): Flow<List<NewConnection>> =
        daoRepository.getNewConnectionsFlow()

    override fun deleteNewConnection(newConnection: NewConnection) =
        daoRepository.deleteNewConnection(newConnection)
}
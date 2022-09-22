package io.xxlabs.messenger.requests.data.group

import io.xxlabs.messenger.repository.DaoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object InvitationMigrator {

    /**
     * Creates and stores [GroupInvitationData] from existing groups that haven't
     * been accepted yet.
     */
    suspend fun performMigration(
        invitationsDataSource: GroupRequestsRepository,
        daoRepository: DaoRepository
    ) =  withContext(Dispatchers.IO) {
        val groupsToMigrate = daoRepository.getAllGroupRequests()
        for (group in groupsToMigrate) {
            launch {
                invitationsDataSource.save(GroupInvitationData(group, false))
            }
        }
    }
}
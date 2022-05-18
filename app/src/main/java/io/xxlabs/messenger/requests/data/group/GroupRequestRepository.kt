package io.xxlabs.messenger.requests.data.group

import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.RequestData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.requests.bindings.BindingsInvitationsMediator
import io.xxlabs.messenger.requests.data.LocalRequestsDataSource
import io.xxlabs.messenger.requests.data.RequestDataSource
import io.xxlabs.messenger.requests.model.GroupInvitation
import io.xxlabs.messenger.support.util.value
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class GroupRequestsRepository @Inject constructor(
    private val daoRepository: DaoRepository,
    private val localDataSource: LocalRequestsDataSource,
    private val invitationsService: BindingsInvitationsMediator
) : RequestDataSource<GroupInvitation> {

    private val scope =  CoroutineScope(
        CoroutineName("GroupInvitationsRepo")
                + Job()
                + Dispatchers.IO
    )

    override val unreadCount: Flow<Int> = localDataSource.unreadCount

    override suspend fun getRequests(): Flow<List<GroupInvitation>> =
        localDataSource.getGroupInvitations().map { requestDataList ->
            requestDataList.map { requestData ->
                val groupData = daoRepository
                    .getGroup(requestData.requestId)
                    .value()
                GroupInvitationData(groupData, requestData.unread)
            }
        }

    override fun save(request: GroupInvitation) {
        localDataSource.addRequest(RequestData.from(request))
    }

    override fun markAsSeen(request: GroupInvitation) {
        scope.launch {
            localDataSource.getRequest(request.requestId)
                ?.copy(unread = false)
                ?.run {
                    localDataSource.updateRequest(this)
                }
        }
    }

    private fun update(request: GroupInvitation, status: RequestStatus) {
        scope.launch {
            daoRepository.updateGroupState(request.model.groupId, status).value()
        }
    }

    override suspend fun accept(request: GroupInvitation): Boolean = withContext(Dispatchers.IO) {
        if (daoRepository.acceptGroup(request.model).value() > 0) {
            delete(request)
            true
        } else {
            update(request, RequestStatus.CONFIRM_FAIL)
            false
        }
    }

    override fun reject(request: GroupInvitation) {
        update(request, RequestStatus.HIDDEN)
    }

    override fun delete(request: GroupInvitation) {
        scope.launch {
            localDataSource.getRequest(request.requestId)?.apply {
                localDataSource.deleteRequest(this)
            }
        }
    }

    override fun send(request: GroupInvitation) {
        scope.launch {
            if (request.requestStatus == RequestStatus.CONFIRM_FAIL) accept(request)
        }
    }

    override fun resetResentRequests() {
        scope.launch {
            getRequests().take(2).collectLatest { invitationsList ->
                val resentList = invitationsList.filter {
                    it.requestStatus == RequestStatus.RESENT
                }
                for (resent in resentList) {
                    update(resent, RequestStatus.SENT)
                }
            }
        }
    }
}

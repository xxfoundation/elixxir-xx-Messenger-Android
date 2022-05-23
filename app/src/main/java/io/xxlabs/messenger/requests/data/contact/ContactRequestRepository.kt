package io.xxlabs.messenger.requests.data.contact

import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.RequestStatus.*
import io.xxlabs.messenger.data.room.model.RequestData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.requests.bindings.ContactRequestsService
import io.xxlabs.messenger.requests.data.LocalRequestsDataSource
import io.xxlabs.messenger.requests.data.RequestDataSource
import io.xxlabs.messenger.requests.model.ContactRequest
import io.xxlabs.messenger.requests.model.GroupInvitation
import io.xxlabs.messenger.support.util.value
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class ContactRequestsRepository @Inject constructor(
    private val daoRepository: DaoRepository,
    private val localDataSource: LocalRequestsDataSource,
    private val requestsService: ContactRequestsService
) : RequestDataSource<ContactRequest> {

    private val scope =  CoroutineScope(
        CoroutineName("ContactRequestsRepo")
                + Job()
                + Dispatchers.IO
    )

    override val unreadCount: Flow<Int> = localDataSource.unreadCount

    override suspend fun getRequests(): Flow<List<ContactRequest>> =
        localDataSource.getContactRequests().map { requestDataList ->
            requestDataList.mapNotNull { requestData ->
                val contactData = daoRepository
                    .getContactByUserId(requestData.requestId)
                    .value()
                contactData?.let {
                    ContactRequestData(it, requestData.unread)
                }
            }
        }

    override fun save(request: ContactRequest) {
        localDataSource.addRequest(RequestData.from(request))
    }

    override fun markAsSeen(request: ContactRequest) {
        scope.launch {
            localDataSource.getRequest(request.requestId)
                ?.copy(unread = false)
                ?.run {
                    localDataSource.updateRequest(this)
                }
        }
    }

    override suspend fun accept(request: ContactRequest): Boolean = withContext(Dispatchers.IO) {
        if (requestsService.acceptContactRequest(request)) {
            delete(request)
            true
        } else {
            update(request, CONFIRM_FAIL)
            false
        }
    }

    override fun delete(request: ContactRequest) {
        scope.launch {
            localDataSource.getRequest(request.requestId)?.apply {
                localDataSource.deleteRequest(this)
            }
        }
    }

    private fun update(request: ContactRequest, status: RequestStatus) {
        scope.launch {
            daoRepository.updateContactState(request.model.userId, status).value()
        }
    }

    override fun reject(request: ContactRequest) {
        update(request, HIDDEN)
    }

    override fun send(request: ContactRequest) {
        scope.launch {
            when (request.requestStatus) {
                RESET_FAIL, RESET_SENT -> resetSession(request)
                SENT -> resendRequest(request)
                CONFIRM_FAIL -> accept(request)
                SENDING -> sendRequest(request)
            }
        }
    }

    private fun resetSession(request: ContactRequest) {
        if (requestsService.resetSession(request.model)) update(request, RESET_SENT)
        else update(request, RESET_FAIL)
    }

    private suspend fun sendRequest(request: ContactRequest) {
        if (requestsService.sendContactRequest(request)) update(request, SENT)
        else update(request, SEND_FAIL)
    }

    private suspend fun resendRequest(request: ContactRequest) {
        if (requestsService.sendContactRequest(request)) {
            update(request, RESENT)
        } else update(request, SEND_FAIL)
    }

    override fun resetResentRequests() {
        scope.launch {
            getRequests().take(2).collectLatest { requestsList ->
                val resentList = requestsList.filter {
                    it.requestStatus == RESENT
                }
                for (resent in resentList) {
                    update(resent, SENT)
                }
            }
        }
    }
}
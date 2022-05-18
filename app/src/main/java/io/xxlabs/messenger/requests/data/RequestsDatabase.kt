package io.xxlabs.messenger.requests.data

import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.application.AppDatabase
import io.xxlabs.messenger.data.room.model.RequestData
import io.xxlabs.messenger.support.extensions.toBase64String
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

class RequestsDatabase @Inject constructor(
    val db: AppDatabase
) : LocalRequestsDataSource {
    private val requestsDao = db.requestsDao()

    private val scope =  CoroutineScope(
        CoroutineName("RequestsDB")
                + Job()
                + Dispatchers.IO
    )

    override val unreadCount: Flow<Int> by ::_unreadCount
    private val _unreadCount: MutableStateFlow<Int> = MutableStateFlow(0)

    init {
        updateUnreadCount()
        if (BuildConfig.DEBUG) listRequests()
    }

    private fun listRequests() {
        scope.launch {
            requestsDao.getAllRequests().collect { requests ->
                for (request in requests) {
                    Timber.d("Found request: ${request.requestId.toBase64String()}")
                }
            }

            getContactRequests().collect { contactRequests ->
                for (request in contactRequests) {
                    Timber.d("Found contact request: ${request.requestId.toBase64String()}")
                }
            }
        }
    }

    private fun updateUnreadCount() {
        scope.launch {
            requestsDao.unreadRequestsFlow().collectLatest {
                _unreadCount.emit(it.count())
            }
        }
    }

    override suspend fun getContactRequests(): Flow<List<RequestData>> =
        requestsDao.getContactRequests()
            .stateIn(scope, SharingStarted.Eagerly, listOf())

    override suspend fun getGroupInvitations(): Flow<List<RequestData>> =
        requestsDao.getGroupInvitations()
            .stateIn(scope, SharingStarted.Eagerly, listOf())


    override fun addRequest(request: RequestData) {
        scope.launch { requestsDao.insert(request) }
    }

    override fun updateRequest(request: RequestData) {
        scope.launch { requestsDao.update(request) }
    }

    override fun deleteRequest(request: RequestData) {
        scope.launch { requestsDao.delete(request) }
    }

    override suspend fun getRequest(requestId: ByteArray): RequestData? =
        withContext(scope.coroutineContext) {
            requestsDao.getRequest(requestId).firstOrNull()
        }
}
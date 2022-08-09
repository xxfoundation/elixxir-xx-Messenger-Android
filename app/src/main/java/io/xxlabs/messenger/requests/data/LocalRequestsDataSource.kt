package io.xxlabs.messenger.requests.data

import io.xxlabs.messenger.data.room.model.RequestData
import io.xxlabs.messenger.requests.model.ContactRequest
import io.xxlabs.messenger.requests.model.GroupInvitation
import kotlinx.coroutines.flow.Flow

interface LocalRequestsDataSource {
    val unreadCount: Flow<Int>

    suspend fun getContactRequestsOnce(): List<RequestData>
    suspend fun getContactRequests(): Flow<List<RequestData>>
    suspend fun getGroupInvitations(): Flow<List<RequestData>>
    suspend fun getRequest(requestId: ByteArray): RequestData?

    fun addRequest(request: RequestData)
    fun updateRequest(request: RequestData)
    fun deleteRequest(request: RequestData)
}
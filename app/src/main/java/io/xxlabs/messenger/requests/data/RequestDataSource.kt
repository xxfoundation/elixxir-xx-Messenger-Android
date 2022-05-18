package io.xxlabs.messenger.requests.data

import io.xxlabs.messenger.requests.model.Request
import kotlinx.coroutines.flow.Flow

interface RequestDataSource<T: Request> {
    val unreadCount: Flow<Int>
    suspend fun getRequests(): Flow<List<T>>
    fun save(request: T)
    fun markAsSeen(request: T)
    suspend fun accept(request: T): Boolean
    fun reject(request: T)
    fun delete(request: T)
    fun send(request: T)
    fun resetResentRequests()
}

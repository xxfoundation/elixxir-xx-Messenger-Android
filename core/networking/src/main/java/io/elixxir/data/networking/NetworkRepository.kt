package io.elixxir.data.networking

interface NetworkRepository {
    suspend fun initializeNetwork(): Result<Unit>
}
package io.elixxir.data.userdiscovery

interface UserRepository {

    suspend fun registerUsername(username: String): Result<Unit>
}
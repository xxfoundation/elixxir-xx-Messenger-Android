package io.elixxir.data.user

interface UserRepository {

    suspend fun registerUsername(username: String): Result<Unit>
}
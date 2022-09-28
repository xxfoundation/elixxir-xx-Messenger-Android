package io.elixxir.data.session

import io.elixxir.data.session.model.SessionState
import io.elixxir.xxclient.cmix.CMix

interface SessionRepository {
    fun getSessionState(): SessionState
    suspend fun decryptSessionPassword(): Result<ByteArray>
    suspend fun getOrCreateSession(): Result<CMix>
    suspend fun restoreSession()
    suspend fun deleteSession(): Result<Unit>
}
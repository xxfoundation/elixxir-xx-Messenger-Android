package io.elixxir.data.session

import io.elixxir.data.session.model.SessionState

interface SessionRepository {
    fun getSessionState(): SessionState
    fun createSession()
    fun restoreSession()
    fun deleteSession()
}
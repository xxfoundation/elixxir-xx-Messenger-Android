package io.elixxir.data.session.data

import io.elixxir.data.session.SessionRepository
import io.elixxir.data.session.model.SessionState

internal class SessionDataSource : SessionRepository {
    override fun getSessionState(): SessionState {
        TODO("Not yet implemented")
    }

    override fun createSession() {
        TODO("Not yet implemented")
    }

    override fun restoreSession() {
        TODO("Not yet implemented")
    }

    override fun deleteSession() {
        TODO("Not yet implemented")
    }
}
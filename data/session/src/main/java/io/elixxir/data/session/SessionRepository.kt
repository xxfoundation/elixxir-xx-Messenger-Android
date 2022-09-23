package io.elixxir.data.session

interface SessionRepository {
    fun createSession()
    fun restoreSession()
    fun deleteSession()
}
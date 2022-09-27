package io.elixxir.data.session.data

import io.elixxir.data.session.SessionRepository
import io.elixxir.data.session.model.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionDataSource @Inject internal constructor()  : SessionRepository {
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

    private fun getOrCreateSession() {
        scope.launch(Dispatchers.IO) {
            val appFolder = repo.createSessionFolder(context)
            try {
                repo.newClient(appFolder, sessionPassword)
                preferences.lastAppVersion = BuildConfig.VERSION_CODE
                connectToCmix()
            } catch (err: Exception) {
                err.printStackTrace()
                displayError(err.toString())
            }
        }
    }
}
package io.elixxir.data.session.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.elixxir.core.common.Config
import io.elixxir.core.common.util.resultOf
import io.elixxir.data.session.SessionRepository
import io.elixxir.data.session.model.SessionState
import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.cmix.CMix
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class SessionDataSource @Inject internal constructor(
    @ApplicationContext private val context: Context,
    private val bindings: Bindings,
    private val keyStore: KeyStoreManager,
    config: Config,
) : SessionRepository, Config by config {

    private var cmix: CMix? = null
    private val sessionFolder : File =
        File(context.filesDir, SESSION_FOLDER_PATH).apply {
            log("Session folder location: $absolutePath")
        }

    override fun getSessionState(): SessionState =
        if (sessionFolder.exists()) SessionState.ExistingUser
        else SessionState.NewUser

    override suspend fun decryptSessionPassword(): Result<ByteArray> =
        keyStore.decryptPassword()

    override suspend fun getOrCreateSession(): Result<CMix> = resultOf {
        if (getSessionState() == SessionState.NewUser) createSession()
        else getSession()
    }

    private suspend fun createSession(): CMix {
        keyStore.generatePassword().getOrThrow()
        val appFolder = createSessionFolder()

        return bindings.loadCmix(
            sessionFileDirectory = appFolder.path,
            sessionPassword = decryptSessionPassword().getOrThrow(),
            cmixParams = bindings.defaultCmixParams
        )
    }

    private suspend fun loadCmix(): CMix = bindings.loadCmix(
        sessionFileDirectory = sessionFolder.path,
        sessionPassword = decryptSessionPassword().getOrThrow(),
        cmixParams = bindings.defaultCmixParams
    ).also {
        cmix = it
    }

    private suspend fun createSessionFolder(): File = withContext(dispatcher) {
        deleteSession().getOrThrow()

        sessionFolder.apply {
            mkdir()
            log("Bindings folder was successfully created at: $absolutePath")
        }
    }

    private suspend fun getSession(): CMix = cmix ?: loadCmix()

    override suspend fun restoreSession() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSession(): Result<Unit> {
        return resultOf {
            sessionFolder.apply {
                if (exists()) {
                    log("Session from previous installation was found.")
                    log("It contains ${listFiles()?.size ?: 0} files.")
                    log("Deleting!")
                    deleteRecursively()
                }
            }
            Result.success(Unit)
        }
    }

    companion object {
        private const val SESSION_FOLDER_PATH = "xxmessenger/session"
    }
}
package io.xxlabs.messenger.backup.cloud.sftp.login

import io.xxlabs.messenger.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.SecurityUtils
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

interface SshClient {
    suspend fun connect(credentials: SshCredentials): SSHClient
    suspend fun disconnect()
}

object Ssh : SshClient {

    private var client: SSHClient? = null
    private var cachedCredentials: SshCredentials? = null

    /**
     * Attempt an remote connection with the provided [SshCredentials].
     * Returns an [SSHClient] reference if successful.
     */
    override suspend fun connect(credentials: SshCredentials): SSHClient = suspendCoroutine { continuation ->
        Timber.d("Attempting SSH connection.")
        client?.let {
            if (it.isConnectedWith(credentials)) {
                Timber.d("Already connected & authenticated with server.")
                continuation.resume(it)
                return@suspendCoroutine
            }
        }

        try {
            // BouncyCastle is deprecated in Android P+
            SecurityUtils.setRegisterBouncyCastle(false)
            val ssh = SSHClient(Config).apply {
//                if (BuildConfig.DEBUG) {
//                    addHostKeyVerifier(PromiscuousVerifier())
//                } else {
//                    addHostKeyVerifier(UserConsentVerifier())
//                }
                addHostKeyVerifier(PromiscuousVerifier())
                connect(credentials.host, credentials.port.toInt())
            }

            try {
                Timber.d("Connected. Authenticating...")
                ssh.authPassword(credentials.username, credentials.password)
            } catch (e: Exception) {
                Timber.d("Failed to authenticate: ${e.message}")
                continuation.resumeWithException(e)
                return@suspendCoroutine
            }

            client = ssh
            cachedCredentials = credentials
            Timber.d("Successfully connected & authenticated!")
            continuation.resume(ssh)
            return@suspendCoroutine
        } catch (e: Exception) {
            Timber.d("Failed to connect: ${e.message}")
            continuation.resumeWithException(e)
            return@suspendCoroutine
        }
    }

    private fun SSHClient.isConnectedWith(credentials: SshCredentials): Boolean =
        isConnected && cachedCredentials == credentials

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Disconnecting from server.")
                client?.disconnect()
            } catch (e: Exception) {
                Timber.d("Exceptiuon thrown while disconnecting: ${e.message}")
                Timber.d("Caught exception closing SSH: ${e.message}")
            } finally {
                Timber.d("Successfully disconnected from server.")
                client = null
            }
        }
    }
}
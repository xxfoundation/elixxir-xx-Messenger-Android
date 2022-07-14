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

private const val devHost = "192.168.1.206"
private const val devPort = 22

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
        client?.let {
            if (it.isConnectedWith(credentials)) {
                continuation.resume(it)
                return@suspendCoroutine
            }
        }

        try {
            // BouncyCastle is deprecated in Android P+
            SecurityUtils.setRegisterBouncyCastle(false)
            val ssh = SSHClient(Config).apply {
                if (BuildConfig.DEBUG) {
                    addHostKeyVerifier(PromiscuousVerifier())
                    connect(devHost, devPort)
                } else {
                    addHostKeyVerifier(UserConsentVerifier())
                    connect(credentials.host, credentials.port.toInt())
                }
            }

            try {
                ssh.authPassword(credentials.username, credentials.password)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }

            client = ssh
            cachedCredentials = credentials
            continuation.resume(ssh)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    private fun SSHClient.isConnectedWith(credentials: SshCredentials): Boolean =
        isConnected && cachedCredentials == credentials

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                client?.disconnect()
            } catch (e: Exception) {
                Timber.d("Caught exception closing SSH: ${e.message}")
            } finally {
                client = null
            }
        }
    }
}
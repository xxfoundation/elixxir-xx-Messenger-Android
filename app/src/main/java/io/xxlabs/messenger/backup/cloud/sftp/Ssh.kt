package io.xxlabs.messenger.backup.cloud.sftp

import io.xxlabs.messenger.BuildConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.SecurityUtils
import net.schmizz.sshj.transport.TransportException
import net.schmizz.sshj.userauth.UserAuthException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val devHost = "192.168.1.206"
private const val devPort = 22

object Ssh {

    private var client: SSHClient? = null
    private var cachedCredentials: SshCredentials? = null

    /**
     * Attempt an remote connection with the provided [SshCredentials].
     * Returns an [SSHClient] reference if successful.
     */
    suspend fun connect(credentials: SshCredentials): SSHClient = suspendCoroutine { continuation ->
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
                    connect(devHost, devPort)
                } else {
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
}
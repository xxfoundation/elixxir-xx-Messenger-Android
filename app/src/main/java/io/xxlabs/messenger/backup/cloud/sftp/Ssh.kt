package io.xxlabs.messenger.backup.cloud.sftp

import io.xxlabs.messenger.BuildConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.SecurityUtils
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val devHost = "192.168.1.206"
private const val devPort = 22

/**
 * Attempt an remote connection with the provided [SshCredentials].
 * Returns an [SSHClient] reference if successful.
 */
suspend fun connect(credentials: SshCredentials): SSHClient = suspendCoroutine { continuation ->
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
        continuation.resume(ssh)
    } catch (e: Exception) {
        continuation.resumeWithException(e)
    }
}
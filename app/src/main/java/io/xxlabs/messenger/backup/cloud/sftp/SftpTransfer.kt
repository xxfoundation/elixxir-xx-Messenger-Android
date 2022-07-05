package io.xxlabs.messenger.backup.cloud.sftp

import io.xxlabs.messenger.backup.bindings.BACKUP_FILE_NAME
import net.schmizz.sshj.SSHClient
import timber.log.Timber

interface SftpClient {
    fun downloadLatestBackup()
    fun uploadBackup()
}

class SftpTransfer(private val credentials: SftpCredentials) : SftpClient {
    private val path: String = ""

    override fun downloadLatestBackup() {
        val ssh = SSHClient().apply {
            loadKnownHosts()
            connect(credentials.host, credentials.port.toInt())
        }
        try {
            ssh.authPassword(credentials.username, credentials.password)
            val sftp = ssh.newSFTPClient()
            try {
                sftp.get(BACKUP_FILE_NAME, path)
            } catch (e: Exception) {
                Timber.d("Error downloading latest backup: ${e.message}")
            } finally {
                sftp.close()
            }
        } catch (e: Exception) {
            Timber.d("Error downloading latest backup: ${e.message}")
        } finally {
            ssh.disconnect()
        }
    }

    override fun uploadBackup() {
        TODO("Not yet implemented")
    }
}
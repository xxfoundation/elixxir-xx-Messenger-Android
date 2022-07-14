package io.xxlabs.messenger.backup.cloud.sftp

import io.xxlabs.messenger.backup.bindings.AccountArchive
import io.xxlabs.messenger.backup.bindings.BACKUP_FILE_NAME
import io.xxlabs.messenger.backup.cloud.BACKUP_DIRECTORY_NAME
import io.xxlabs.messenger.backup.cloud.sftp.login.Ssh
import io.xxlabs.messenger.backup.cloud.sftp.login.SshClient
import io.xxlabs.messenger.backup.cloud.sftp.login.ui.SshCredentials
import io.xxlabs.messenger.backup.model.BackupSnapshot
import io.xxlabs.messenger.filetransfer.FileSize
import kotlinx.coroutines.*
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.xfer.FileSystemFile
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val UPLOAD_PATH = "/$BACKUP_DIRECTORY_NAME/temp_$BACKUP_FILE_NAME"
const val BACKUP_PATH = "/$BACKUP_DIRECTORY_NAME/$BACKUP_FILE_NAME"

interface SftpClient {
    suspend fun download(path: String): Pair<BackupSnapshot, AccountArchive>?
    suspend fun upload(backup: File): FileSize
}

class SftpTransfer(
    private val credentials: SshCredentials,
    private val sshClient: SshClient = Ssh
) : SftpClient {
    private val scope =  CoroutineScope(
        CoroutineName("SftpTransfer")
                + Job()
                + Dispatchers.Default
    )

    override suspend fun download(
        path: String
    ): Pair<BackupSnapshot, AccountArchive>? = withContext(scope.coroutineContext) {
        try {
            sshClient.connect(credentials).newSFTPClient().run {
                if (backupExists()) {
                    val backupFile = fetchBackup(this, path)
                    Pair(backupFile.snapshot(), backupFile.readData())
                } else {
                    null
                }
            }
        } finally {
            sshClient.disconnect()
        }
    }

    private fun SFTPClient.backupExists(): Boolean {
        return statExistence(BACKUP_PATH)?.let {
            it.size > 0
        } ?: false
    }

    private suspend fun fetchBackup(
        sftp: SFTPClient,
        path: String
    ): FileSystemFile = suspendCoroutine { continuation ->
        try {
            val backupFile = FileSystemFile(path)
            sftp.get(BACKUP_FILE_NAME, backupFile)
            continuation.resume(backupFile)
        } catch(e: Exception) {
            continuation.resumeWithException(e)
        } finally {
            sftp.close()
        }
    }

    override suspend fun upload(backup: File): FileSize = withContext(scope.coroutineContext) {
        try {
            val sftp = sshClient.connect(credentials).newSFTPClient()
            val backupFile = FileSystemFile(backup)
            with (sftp) {
                put(backupFile, UPLOAD_PATH)
                deletePreviousBackup()
                FileSize(backupFile.length)
            }
        } finally {
            sshClient.disconnect()
        }
    }

    private fun SFTPClient.deletePreviousBackup() {
        statExistence(BACKUP_PATH)?.let {
            rm(BACKUP_PATH)
            rename(UPLOAD_PATH, BACKUP_PATH)
        }
    }
}
private data class SftpBackupData(
    val name: String,
    override val date: Long,
    override val sizeBytes: Long
) : BackupSnapshot {

    companion object Factory {
        fun from(file: FileSystemFile) = with(file) {
            SftpBackupData(name, lastModifiedTime, length)
        }
    }
}

private fun FileSystemFile.snapshot(): BackupSnapshot = SftpBackupData.from(this)

private suspend fun FileSystemFile.readData(): AccountArchive = withContext(Dispatchers.IO) {
    inputStream.use {
        AccountArchive(inputStream.readBytes())
    }
}
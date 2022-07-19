package io.xxlabs.messenger.backup.cloud.sftp.transfer

import io.xxlabs.messenger.backup.bindings.AccountArchive
import io.xxlabs.messenger.backup.bindings.BACKUP_FILE_NAME
import io.xxlabs.messenger.backup.cloud.BACKUP_DIRECTORY_NAME
import io.xxlabs.messenger.backup.cloud.sftp.login.Ssh
import io.xxlabs.messenger.backup.cloud.sftp.login.SshClient
import io.xxlabs.messenger.backup.cloud.sftp.login.SshCredentials
import io.xxlabs.messenger.backup.model.BackupSnapshot
import io.xxlabs.messenger.filetransfer.FileSize
import kotlinx.coroutines.*
import net.schmizz.sshj.sftp.Response
import net.schmizz.sshj.sftp.SFTPClient
import net.schmizz.sshj.sftp.SFTPException
import net.schmizz.sshj.xfer.FileSystemFile
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val BACKUP_PATH = "$BACKUP_DIRECTORY_NAME/$BACKUP_FILE_NAME"

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
        try {
            return statExistence(BACKUP_PATH)?.size?.let {
                Timber.d("Previous backup at path $BACKUP_PATH found.")
                true
            } ?: false
        } catch (e: Exception) {
            (e as? SFTPException)?.run {
                if (statusCode == Response.StatusCode.NO_SUCH_FILE) {
                    Timber.d("$BACKUP_PATH was not found on server.")
                }
            }
        }
        return false
    }

    private suspend fun fetchBackup(
        sftp: SFTPClient,
        path: String
    ): FileSystemFile = suspendCoroutine { continuation ->
        try {
            val backupFile = FileSystemFile(path)
            sftp.get(BACKUP_PATH, backupFile)
            Timber.d("File successfully fetched.")
            continuation.resume(backupFile)
        } catch(e: Exception) {
            Timber.d("Exception when fetching file: ${e.message}")
            continuation.resumeWithException(e)
        } finally {
            Timber.d("Closing SFTP client.")
            sftp.close()
        }
    }

    override suspend fun upload(backup: File): FileSize = withContext(scope.coroutineContext) {
        try {
            val sftp = sshClient.connect(credentials).newSFTPClient()
            val backupFile = FileSystemFile(backup).apply {
                lastModifiedTime = System.currentTimeMillis() / 1000
            }
            with (sftp) {
                if (backupExists()) deletePreviousBackup()
                else makeDirectory()

                Timber.d("Uploading file to $BACKUP_PATH...")
                put(backupFile, BACKUP_PATH)
                Timber.d("Successfully uploaded file to $BACKUP_PATH.")
                FileSize(backupFile.length)
            }
        } finally {
            sshClient.disconnect()
        }
    }

    private fun SFTPClient.deletePreviousBackup() {
        Timber.d("Deleting previous backup...")
        rm(BACKUP_PATH)
        Timber.d("Successfully deleted.")

    }

    private fun SFTPClient.makeDirectory() {
        Timber.d("Creating new directory $BACKUP_DIRECTORY_NAME")
        mkdir(BACKUP_DIRECTORY_NAME)
    }
}
private data class SftpBackupData(
    val name: String,
    override val date: Long,
    override val sizeBytes: Long
) : BackupSnapshot {

    companion object Factory {
        fun from(file: FileSystemFile) = with(file) {
            SftpBackupData(name, lastModifiedTime * 1000, length)
        }
    }
}

private fun FileSystemFile.snapshot(): BackupSnapshot = SftpBackupData.from(this)

private suspend fun FileSystemFile.readData(): AccountArchive = withContext(Dispatchers.IO) {
    inputStream.use {
        AccountArchive(inputStream.readBytes())
    }
}
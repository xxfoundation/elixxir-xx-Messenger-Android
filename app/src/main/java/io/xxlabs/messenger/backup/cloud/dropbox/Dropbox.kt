package io.xxlabs.messenger.backup.cloud.dropbox

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DeleteResult
import com.dropbox.core.v2.files.FileMetadata
import com.dropbox.core.v2.files.ListFolderErrorException
import com.dropbox.core.v2.files.WriteMode
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.bindings.AccountArchive
import io.xxlabs.messenger.backup.bindings.BACKUP_FILE_NAME
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.cloud.BACKUP_DIRECTORY_NAME
import io.xxlabs.messenger.backup.cloud.CloudStorage
import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.backup.data.restore.RestoreEnvironment
import io.xxlabs.messenger.backup.model.BackupLocation
import io.xxlabs.messenger.backup.model.BackupSnapshot
import io.xxlabs.messenger.support.appContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Encapsulates Dropbox API.
 */
class Dropbox private constructor(
    private val backupService: BackupService,
    private val preferences: BackupPreferencesRepository
) : CloudStorage(backupService) {

    private var credential: DbxCredential? = null
        get() {
            if (field == null) {
                field = DbxCredential.Reader.readFully(preferences.dbxCredential ?: return null)
            }
            return field
        }
    private var dbxInstance: DbxClientV2? = null
        get() {
            if (field == null) {
                val config = DbxRequestConfig.newBuilder(CLIENT_IDENTIFIER).build()
                field = DbxClientV2(config, credential ?: return null)
            }
            return field
        }

    private val authHandler: DropboxAuthHandler by lazy { DropboxAuthHandler(authResultCallback) }
    override val location: BackupLocation =
        BackupLocationData(
            R.drawable.ic_dropbox,
            appContext().getString(R.string.backup_service_dropbox),
            ::requiresSignIn,
            ::authBackgroundsApp,
            ::signOut,
            ::isEnabled
        ) {
            _authResultCallback = it
            authHandler
        }

    override fun isEnabled(): Boolean = preferences.isDropboxEnabled

    private var dbxBackupData: DropboxBackupData? = null

    private fun requiresSignIn(): Boolean {
       return when (preferences.dbxCredential) {
           null -> true
           else -> {
               scope.launch { accessDropboxAppData() }
               false
           }
       }
    }

    private fun authBackgroundsApp(): Boolean = true

    override fun onAuthResultSuccess() {
        scope.launch {
            updateCredentialCache()
            accessDropboxAppData()
            withContext(Dispatchers.Main) {
                _authResultCallback?.onSuccess()
            }
        }
    }

    private fun updateCredentialCache() {
        preferences.dbxCredential = Auth.getDbxCredential()?.toString()
    }

    private suspend fun accessDropboxAppData() = withContext(Dispatchers.IO) {
        dbxInstance?.run {
            try {
                dbxBackupData = getBackup()
                updateLastBackup(dbxBackupData)
            } catch (e: Exception) {
                if (e is ListFolderErrorException) createBackupFolder()
                else clearCredentialCache()
                Timber.d(e)
            }
        }
    }

    private fun clearCredentialCache() {
        preferences.dbxCredential = null
    }

    private suspend fun createBackupFolder() = withContext(Dispatchers.IO) {
        dbxInstance?.files()?.createFolderV2("/$BACKUP_DIRECTORY_NAME")
    }

    override suspend fun onRestore(environment: RestoreEnvironment) {
        log("Downloading backup")
        updateProgress(25, 100)
        dbxBackupData?.run {
            val backupData = fetchBackup(this)
            if (backupData.isNotEmpty()) {
                updateProgress(75, 100)
                AccountArchive(backupData).restoreUsing(environment)
            }
        }
    }

    private suspend fun fetchBackup(backup: DropboxBackupData): ByteArray = withContext(Dispatchers.IO) {
        dbxInstance?.runCatching {
            download(backup).also {
                updateProgress(50, 100)
                log("Successfully downloaded ${it.size} bytes.")
            }
        }
    }?.getOrDefault(byteArrayOf()) ?: byteArrayOf()

    override fun backupNow() {
        if (!isEnabled()) return

        scope.launch {
            updateProgress()
            uploadBackup()
        }
    }

    private suspend fun uploadBackup() = withContext(Dispatchers.IO) {
        dbxInstance?.run {
            try {
                File(backupService.backupFilePath).let { file ->
                    if (file.exists()) {
                        updateProgress(25)
                        scope.launch {
                            deleteBackup()
                            upload(file).apply {
                                Timber.d("Uploaded $name. Id: $id. Size: $size.")
                                dbxBackupData = DropboxBackupData.from(this)
                                updateLastBackup(dbxBackupData)
                                updateProgress(100)
                            }
                        }
                    } else {
                        Timber.d("No backup found.")
                    }
                }
            } catch (e: Exception) {
                Timber.d(e)
                updateProgress(error = e)
            }
        } ?: {
            with("Backup failed. Please try again.") {
                Timber.d(this)
                updateProgress(error = java.lang.Exception(this))
            }
        }
    }

    private fun signOut() {
        scope.launch {
            dbxInstance?.auth()?.tokenRevoke()
            clearCredentialCache()
        }
    }

    companion object {
        @Volatile
        private var instance: Dropbox? = null
        const val CLIENT_IDENTIFIER = "xxMessengerAndroid/${BuildConfig.VERSION_CODE}"

        fun getInstance(backupService: BackupService, preferences: BackupPreferencesRepository): Dropbox =
            instance ?: Dropbox(backupService, preferences).also { instance = it }
    }
}

private data class DropboxBackupData(
    val id: String,
    val name: String,
    override val date: Long,
    override val sizeBytes: Long,
    val pathLower: String
): BackupSnapshot {

    companion object Factory{
        fun from(metadata: FileMetadata) = with(metadata) {
            DropboxBackupData(id, name, clientModified.time, size, pathLower)
        }
    }
}

private fun DbxClientV2.upload(file: File): FileMetadata =
    files()
        .uploadBuilder("/$BACKUP_DIRECTORY_NAME/$BACKUP_FILE_NAME")
        .withMode(WriteMode.OVERWRITE)
        .uploadAndFinish(file.inputStream())

private fun DbxClientV2.download(backup: DropboxBackupData): ByteArray {
    val outputStream = ByteArrayOutputStream()
    files()
        .download(backup.pathLower)
        .download(outputStream)
    return outputStream.use { it.toByteArray() }
}

private fun DbxClientV2.getBackup(): DropboxBackupData? {
    var contents = files().listFolder("/$BACKUP_DIRECTORY_NAME")
    var backupData: DropboxBackupData? = null
    while (true) {
        for (entry in contents.entries) {
            Timber.d("Found ${entry.name}. Metadata: ${entry.pathLower}")
            if (entry.name == BACKUP_FILE_NAME) {
                backupData = DropboxBackupData.from(entry as FileMetadata)
                break
            }
        }

        if (!contents.hasMore) break
        contents = files().listFolderContinue(contents.cursor)
    }
    return backupData
}

private fun DbxClientV2.deleteBackup(): DeleteResult? {
    var contents = files().listFolder("/$BACKUP_DIRECTORY_NAME")
    var result: DeleteResult? = null
    while (true) {
        for (entry in contents.entries) {
            Timber.d("Found ${entry.name}. Metadata: ${entry.pathLower}")
            if (entry.name == BACKUP_FILE_NAME) {
                result = files().deleteV2(entry.pathLower)
            }
        }

        if (!contents.hasMore) break
        contents = files().listFolderContinue(contents.cursor)
    }
    return result
}
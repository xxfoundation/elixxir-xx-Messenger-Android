package io.xxlabs.messenger.backup.cloud.drive

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.FileList
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.bindings.*
import io.xxlabs.messenger.backup.cloud.BACKUP_DIRECTORY_NAME
import io.xxlabs.messenger.backup.cloud.CloudStorage
import io.xxlabs.messenger.backup.data.backup.BackupPreferencesRepository
import io.xxlabs.messenger.backup.data.restore.RestoreEnvironment
import io.xxlabs.messenger.backup.model.*
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.appContext
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.util.*
import com.google.api.services.drive.model.File as DriveFile

private const val QUERY_BACKUP_FILE = "name = '$BACKUP_FILE_NAME'"
private const val QUERY_APP_FOLDER = "'appDataFolder' in parents"
private const val QUERY_FOLDERS = "mimeType = 'application/vnd.google-apps.folder'"
private const val QUERY_BACKUP_DIRECTORY = "name = '$BACKUP_DIRECTORY_NAME'"
private const val SPACES_APPDATA = "appDataFolder"
private const val FIELDS_BACKUP_FILE = "nextPageToken, files(id, name, size, modifiedTime)"
private const val FIELDS_BACKUP_FOLDER = "files(id, name)"
private const val FIELDS_CREATE_FILE = "id, name, size, modifiedTime, parents"
private const val TYPE_BINARY_CONTENT = "application/octet-stream"

/**
 * Encapsulates Google Drive API.
 */
class GoogleDrive private constructor(
    private val backupService: BackupService,
    private val preferences: BackupPreferencesRepository
) : CloudStorage(backupService) {

    private val authHandler: GoogleAuthHandler by lazy { GoogleAuthHandler(authResultCallback) }
    override val location: BackupLocation =
        BackupLocationData(
            R.drawable.ic_google_drive,
            appContext().getString(R.string.backup_service_google_drive),
            ::signInRequired,
            ::authBackgroundsApp,
            ::signOut,
            ::isEnabled
        ) {
            _authResultCallback = it
            authHandler
        }

    private var driveBackupData: DriveBackupData? = null
    private var driveInstance: Drive? = null
        get() {
            if (field == null) {
                field = authenticateWithDrive()?.let { getDriveInstance(it) }
            }
            return field
        }


    private fun signInRequired(): Boolean = true
    private fun authBackgroundsApp(): Boolean = false

    override fun isEnabled(): Boolean = preferences.isGoogleDriveEnabled

    private fun authenticateWithDrive(): GoogleAccountCredential? =
        GoogleSignIn.getLastSignedInAccount(appContext())?.account?.let { account ->
            val credential = GoogleAccountCredential.usingOAuth2(
                appContext(), listOf(DriveScopes.DRIVE_APPDATA)
            )
            credential.apply { selectedAccount = account }
        }

    private fun getDriveInstance(credential: GoogleAccountCredential): Drive? =
        Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            JacksonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName(appContext().getString(R.string.xx_app_name))
        .build()

    override suspend fun onRestore(environment: RestoreEnvironment) {
        updateProgress(25, 100)
        driveBackupData?.run {
            val backupData = fetchBackup(id)
            if (backupData.isNotEmpty()) {
                updateProgress(75, 100)
                AccountArchive(backupData).restoreUsing(environment)
            }
        }
    }

    private suspend fun fetchBackup(fileId: String): ByteArray = withContext(Dispatchers.IO) {
        driveInstance?.runCatching {
            downloadBackup(fileId)
        }?.getOrDefault(byteArrayOf()) ?: byteArrayOf()
    }

    private fun Drive.downloadBackup(fileId: String): ByteArray {
        log("Downloading backup")

        val outputStream = ByteArrayOutputStream()
        files().get(fileId).executeMediaAndDownloadTo(outputStream)
        return outputStream.use {
            updateProgress(50, 100)
            it.toByteArray().apply {
                log("Successfully downloaded $size bytes.")
            }
        }
    }

    override fun backupNow() {
        if (!isEnabled()) return

        updateProgress()
        File(backupService.backupFilePath).let { file ->
            if (file.exists()) {
                updateProgress(25)
                scope.launch {
                    accessDriveAppData(Drive::queryBackupFolder)
                        ?.uploadToBackupFolder(file)
                }
            } else {
                with("No backup found.") {
                    Timber.d(this)
                    updateProgress(error = Exception(this))
                }
            }
        }
    }

    private suspend fun List<DriveFile>.uploadToBackupFolder(backup: File) {
        if (isNotEmpty()) uploadBackup(backup, first().id)
        else {
            Timber.d("Creating backup folder in Drive.")
            updateProgress(50)
            createDriveBackupFolder()?.run {
                Timber.d("Backup folder $this created.")
                uploadBackup(backup, this)
            }
        }
    }

    private fun createDriveBackupFolder() : String? {
        val backupFolder = DriveFile().apply {
            name = BACKUP_DIRECTORY_NAME
            mimeType = "application/vnd.google-apps.folder"
            parents = Collections.singletonList("appDataFolder")
        }
        return driveInstance?.run {
            files().create(backupFolder)
                .setFields("id, name, parents")
                .execute()
                .id
        }
    }

    private suspend fun uploadBackup(
        backup: File,
        backupFolderId: String
    ): Boolean = driveInstance?.let { drive ->
        withContext(Dispatchers.IO) {
            updateProgress(75)
            try {
                drive.deletePreviousBackups()

                val driveFile = createUploadFile(backup.name, backupFolderId)
                val fileContent = createUploadContent(backup)

                drive.upload(driveFile, fileContent).run {
                    onSuccessfulUpload()
                    true
                }
            } catch (e: Exception) {
                Timber.d(e)
                updateProgress(error = e)
                false
            }
        }
    } ?: false.also {
        with("Backup failed. Please try again.") {
            Timber.d(this)
            updateProgress(error = Exception(this))
        }
    }

    private fun DriveFile.onSuccessfulUpload() {
        driveBackupData = DriveBackupData.fromDriveFile(this@onSuccessfulUpload)
        updateLastBackup(driveBackupData)
        Timber.d("Backup successful: $id")
        updateProgress(100)
    }

    private fun createUploadFile(fileName: String, folderId: String): DriveFile =
        DriveFile().apply {
            name = fileName
            parents = Collections.singletonList(folderId)
        }

    private fun createUploadContent(file: File): FileContent =
        FileContent(TYPE_BINARY_CONTENT, file)

    private suspend fun accessDriveAppData(
        query: Drive.() -> Drive.Files.List
    ) : List<DriveFile>? = driveInstance?.let { drive ->
        withContext(Dispatchers.IO) {
            val filesList = mutableListOf<DriveFile>()
            drive.query().addResultsTo(filesList)
            filesList.logResults()
        }
    }

    override fun onAuthResultSuccess() {
        scope.launch {
            accessDriveAppData(Drive::queryBackupFile)?.apply {
                if (isNotEmpty()) {
                    driveBackupData = DriveBackupData.fromDriveFile(first())
                    updateLastBackup(driveBackupData)
                }
                withContext(Dispatchers.Main) {
                    _authResultCallback?.onSuccess()
                }
            }
        }
    }

    private fun signOut() = authHandler.signOut()

    companion object {
        @Volatile
        private var instance: GoogleDrive? = null

        fun getInstance(backupService: BackupService, preferences: BackupPreferencesRepository): GoogleDrive =
            instance ?: GoogleDrive(backupService, preferences).also { instance = it }
    }
}

private data class DriveBackupData(
    val id: String,
    val name: String,
    override val date: Long,
    override val sizeBytes: Long
): BackupSnapshot {

    companion object Factory {
        fun fromDriveFile(file: DriveFile) = with(file) {
            DriveBackupData(id, name, modifiedTime.value, getSize())
        }
    }
}

private fun Drive.queryBackupFile(): Drive.Files.List =
    files().list().apply {
        q = QUERY_BACKUP_FILE
        spaces = SPACES_APPDATA
        fields = FIELDS_BACKUP_FILE
        pageToken = this.pageToken
    }

private fun Drive.queryBackupFolder(): Drive.Files.List =
    files().list().apply {
        q = "$QUERY_APP_FOLDER and $QUERY_FOLDERS and $QUERY_BACKUP_DIRECTORY"
        spaces = SPACES_APPDATA
        fields = FIELDS_BACKUP_FOLDER
        pageToken = this.pageToken
    }

private fun Drive.Files.List.addResultsTo(filesList: MutableList<DriveFile>) {
    var pageToken: String? = null
    do {
        try {
            val result = execute()
            pageToken = filesList.addAll(result)
        } catch (e: Exception) {
            Timber.d(e)
        }
    } while (pageToken != null)
}

private fun MutableList<DriveFile>.addAll(fileList: FileList): String? {
    for (file in fileList.files) {
        Timber.d("name=${file.name} id=${file.id}")
        add(file)
    }
    return fileList.nextPageToken
}

private fun MutableList<DriveFile>.logResults(): MutableList<DriveFile> {
    if (isNotEmpty()) Timber.d("Backup found: ${first().id}")
    else Timber.d("No backup found.")
    return this
}

private fun Drive.deletePreviousBackups() {
    val backupList = mutableListOf<DriveFile>()
    queryBackupFolder().addResultsTo(backupList)
    for (backup in backupList) {
        files().delete(backup.id)
    }
}

private fun Drive.upload(driveFile: DriveFile, fileContent: FileContent) : DriveFile =
    files().create(driveFile, fileContent).apply {
        fields = FIELDS_CREATE_FILE
    }.execute()
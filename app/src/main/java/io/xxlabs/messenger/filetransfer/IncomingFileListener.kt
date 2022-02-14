package io.xxlabs.messenger.filetransfer

import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import data.proto.CMIXText
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.PrivateMessageData
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.toBase64String
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InvalidObjectException
import kotlin.jvm.Throws

/**
 * Listens for incoming files and saves them to the device.
 */
class IncomingFileListener (
    private val repo: BaseRepository,
    private val fileTransfer: FileTransferRepository
): IncomingFileCallback {

    private val scope = CoroutineScope(
        CoroutineName("IncomingFileListener") +
                Job() +
                Dispatchers.IO
    )

    override fun onFileTransferReceived(file: IncomingFile) = trackDownloadProgress(file)

    private fun trackDownloadProgress(file: IncomingFile) {
        Timber.d("[File Transfer] Starting download of transfer ID ${file.transferId.tid.toBase64String()}")
        val incomingFileMessage = createIncomingFileMessage(file)
        saveMessageToDb(file, incomingFileMessage)
    }

    private fun createIncomingFileMessage(fileInfo: IncomingFile): PrivateMessageData {
        val messagePayload = createMessagePayload("Incoming file")

        return PrivateMessageData(
            uniqueId = fileInfo.transferId.tid,
            status = MessageStatus.RECEIVED.value,
            timestamp = System.currentTimeMillis(),
            unread = true,
            sender = fileInfo.sender.id,
            receiver = repo.getUserId(),
            payload = messagePayload,
            fileType = fileInfo.fileType.toString(),
            transferProgress = 0
        )
    }

    private fun createMessagePayload(messageText: String): String =
        CMIXText.newBuilder().apply {
            text = messageText
        }.build().toByteArray().toBase64String()

    private fun saveMessageToDb(file: IncomingFile, message: PrivateMessageData) {
        (repo as? ClientRepository)?.run {
            daoRepo.insertMessage(message)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .subscribeBy(
                    onError = { err ->
                        // TODO: Handle edge case where message cannot be saved
                        Timber.e("Error on inserting message ${err.localizedMessage}")
                    },
                    onSuccess = { msgId ->
                        Timber.v("Insert message id: $msgId")
                        val savedMessage = message.copy(id = msgId)
                        onMessageSaved(file, savedMessage)
                    }
                )
        }
    }

    private fun onMessageSaved(file: IncomingFile, message: PrivateMessageData) {
        // Create a reference to the download progress.
        val downloadProgress = DownloadProgress(
            message,
            file,
            ::onProgress,
            ::onDownloadComplete
        )

        // Register the listener and transferId to receive progress updates
        fileTransfer.registerReceiveProgressCallback(
            file.transferId,
            downloadProgress,
        )
    }

    /**
     * Called when an [IncomingFile] receives a progress update.
     */
    private fun onProgress(message: PrivateMessageData, progress: Long) {
        val updatedProgressMessage = message.copy(
            transferProgress = progress
        )
        updateMessageInDb(updatedProgressMessage)
    }

    private fun updateMessageInDb(message: PrivateMessageData) {
        (repo as? ClientRepository)?.run {
            daoRepo.updateMessage(message)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .subscribeBy(
                    onError = { err ->
                        Timber.e("Error updating message ${err.localizedMessage}")
                    },
                    onSuccess = { msgId ->
                        Timber.v("Updated message id: $msgId")
                    }
                )
        }
    }

    /**
     * Called when an [IncomingFile] download has completed.
     */
    private fun onDownloadComplete(download: DownloadProgress) {
        runBlocking(scope.coroutineContext) {
            val fileData = receiveFile(download.id)
            composeReceivedFile(fileData, download)
        }
    }

    private suspend fun receiveFile(transferId: TransferId): FileData =
        fileTransfer.receive(transferId)

    @Throws(InvalidObjectException::class)
    private suspend fun composeReceivedFile(
        fileData: FileData,
        download: DownloadProgress
    ) {
        val uri = when (download.fileInfo.fileType) {
            FileType.IMAGE -> saveToPicturesDirectory(fileData, download.fileInfo)
            FileType.VIDEO -> saveToPicturesDirectory(fileData, download.fileInfo)
            FileType.AUDIO -> saveToMusicDirectory(fileData, download.fileInfo)
            FileType.DOCUMENT -> saveToDocumentsDirectory(fileData, download.fileInfo)
            FileType.OTHER -> saveToDownloadsDirectory(fileData, download.fileInfo)
        }

        val messagePayload = createMessagePayload("Received file")
        val updatedMessage = download.message.copy(
            payload = messagePayload,
            fileUri = uri.toString(),
            transferProgress = 100
        )
        updateMessageInDb(updatedMessage)
    }

    private suspend fun saveToPicturesDirectory(
        fileData: FileData,
        fileInfo: IncomingFile
    ): Uri = withContext(Dispatchers.IO) {
        val image = File(
            appContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "${fileInfo.fileName}.${fileInfo.fileExtension}"
        )

        // TODO: Gracefully handle case where file already exists
        if (image.exists()) image.delete()

        runCatching {
            val outputStream = FileOutputStream(image.path)
            outputStream.use { stream ->
                stream.write(fileData.rawBytes)
            }
        }

        FileProvider.getUriForFile(
            appContext(),
            AUTHORITY,
            image
        )
    }

    private suspend fun saveToMusicDirectory(
        fileData: FileData,
        fileInfo: IncomingFile
    ): Uri = withContext(Dispatchers.IO) {
        val audio = File(
            appContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC),
            "${fileInfo.fileName}.${fileInfo.fileExtension}"
        )

        // TODO: Gracefully handle case where file already exists
        if (audio.exists()) audio.delete()

        runCatching {
            val outputStream = FileOutputStream(audio.path)
            outputStream.use { stream ->
                stream.write(fileData.rawBytes)
            }
        }

        FileProvider.getUriForFile(
            appContext(),
            AUTHORITY,
            audio
        )
    }

    private suspend fun saveToDocumentsDirectory(
        fileData: FileData,
        fileInfo: IncomingFile
    ): Uri = withContext(Dispatchers.IO) {
        val document = File(
            appContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "${fileInfo.fileName}.${fileInfo.fileExtension}"
        )

        // TODO: Gracefully handle case where file already exists
        if (document.exists()) document.delete()

        runCatching {
            val outputStream = FileOutputStream(document.path)
            outputStream.use { stream ->
                stream.write(fileData.rawBytes)
            }
        }

        FileProvider.getUriForFile(
            appContext(),
            AUTHORITY,
            document
        )
    }

    private suspend fun saveToDownloadsDirectory(
        fileData: FileData,
        fileInfo: IncomingFile
    ): Uri = withContext(Dispatchers.IO) {
        val download = File(
            appContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "${fileInfo.fileName}.${fileInfo.fileExtension}"
        )

        // TODO: Gracefully handle case where file already exists
        if (download.exists()) download.delete()

        runCatching {
            val outputStream = FileOutputStream(download.path)
            outputStream.use { stream ->
                stream.write(fileData.rawBytes)
            }
        }

        FileProvider.getUriForFile(
            appContext(),
            AUTHORITY,
            download
        )
    }

    companion object {
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
    }
}

private data class DownloadProgress(
    val message: PrivateMessageData,
    val fileInfo: IncomingFile,
    val onProgress: (message: PrivateMessageData, progress: Long) -> Unit,
    val onComplete: (download: DownloadProgress) -> Unit,
) : ReceivedFileProgressCallback {

    val id: TransferId = TransferId(message.uniqueId)

    override fun onProgressUpdate(
        isComplete: Boolean,
        chunksReceived: Long,
        chunksTotal: Long,
        error: Exception?
    ) {
        if (isComplete) {
            Timber.d("[File Transfer] ${message.uniqueId.toBase64String()} complete: $chunksTotal chunks")
            onComplete(this)
        } else {
            Timber.d("[File Transfer] ${message.uniqueId.toBase64String()} $chunksReceived/$chunksTotal")
//            val progress = chunksReceived / chunksTotal
//            onProgress(message, progress)
        }
        // TODO: Cancel download on error, allow retry
    }
}
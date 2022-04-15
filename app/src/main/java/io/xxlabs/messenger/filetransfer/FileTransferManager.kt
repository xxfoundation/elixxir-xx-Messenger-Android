package io.xxlabs.messenger.filetransfer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import bindings.*
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.media.MediaProviderActivity
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.toBase64String
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * A [FileTransfer] facade.
 */
class FileTransferManager(repo: BaseRepository) : FileTransferRepository {

    private val callback: IncomingFileCallback = IncomingFileListener(repo, this)

    private val manager: FileTransfer =
        Bindings.newFileTransferManager(
            ClientRepository.clientWrapper.client,
            IncomingFileTransferCallback(callback),
            FileTransferParams.create()
        )

    override val maxFilePreviewSize: FileSize = FileSize(manager.maxFilePreviewSize)
    override val maxFileNameByteLength: Long = manager.maxFileNameByteLength
    override val maxFileTypeByteLength: Long = manager.maxFileTypeByteLength
    override val maxFileSize: FileSize = FileSize(manager.maxFileSize)

    @Throws(Exception::class)
    override fun send(content: OutgoingFile): SentFile = runBlocking(Dispatchers.IO) {
        var fileName = ""

        // Get the file name.
        appContext().contentResolver.query(
            content.uri, null, null, null, null
        )?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            fileName = cursor.getString(nameIndex)
        }

        val fileExtension = fileName
            .substring(fileName.lastIndexOf('.') + 1)
            .lowercase()

        if (fileExtension.contains("gif", true)) {
            throw Exception("GIFs are currently not supported")
        }
        
        fileName = "xx_${System.currentTimeMillis()}".apply {
            // Truncate the file name
            substring(0, length.coerceAtMost(maxFileNameByteLength.toInt()))
        }

        // Get the raw bytes of the file.
        val fileData = FileData(
            appContext().contentResolver.openInputStream(content.uri)
                ?.buffered()
                ?.readBytes()
                ?: throw Exception("File is empty or couldn't be accessed.")
        )

        val sentFile = SentFile(
            transferId = TransferId(ByteArray(0)),
            fileName,
            fileExtension,
            data = fileData,
            preview = null,
            recipient = content.recipient,
            progressCallback = content.progressCallback,
            uri = content.uri
        )

        when (sentFile.fileType) {
            FileType.IMAGE -> compressAndSendImage(sentFile)
            else -> sendFile(sentFile)
        }
    }

    private fun compressAndSendImage(sentFile: SentFile): SentFile = runBlocking(Dispatchers.IO) {
        // Copy image to cache with required jpeg file extension.
        val savedImage = File(
            appContext().filesDir,
            "${sentFile.fileName}.jpeg"
        )
        val outputStream = FileOutputStream(savedImage.path)

        if (!sentFile.fileExtension.contains("jpeg", true)) {
            // Convert this image to .jpeg
            with(sentFile.data.rawBytes) {
                val bitmap = BitmapFactory.decodeByteArray(this, 0, size)
                outputStream.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
                }
            }
        } else {
            outputStream.use { stream ->
                stream.write(sentFile.data.rawBytes)
            }
        }

        val savedImageUri =  FileProvider.getUriForFile(
            appContext(),
            AUTHORITY,
            savedImage
        )
        val savedFileData = FileData(
                appContext().contentResolver.openInputStream(savedImageUri)
                        ?.buffered()
                        ?.readBytes()
                        ?: throw Exception("File is empty or couldn't be accessed.")
        )

        // Use JPEG compression provided by Bindings.
        val compressedImageData = Bindings.compressJpeg(savedFileData.rawBytes)
        // Create a SentFile with the compressed file data.
        val compressedSentFile = sentFile.copy(
            data = FileData(compressedImageData),
            fileExtension = "jpeg",
            uri = savedImageUri
        )
        enforceFileSize(compressedSentFile)
        // Send the image hold on to the returned TransferId.
        val transferId = send(compressedSentFile)
        // Return a copy of the SentFile with the TransferId.
        compressedSentFile.copy(transferId = transferId)
    }

    private fun sendFile(sentFile: SentFile): SentFile {
        enforceFileSize(sentFile)
        val transferId = send(sentFile)
        return sentFile.copy(transferId = transferId)
    }

    private fun enforceFileSize(sentFile: SentFile) {
        val fileSize = sentFile.data.rawBytes.size.toLong()
        if (FileSize(fileSize).kb > maxFileSize.kb) throw Exception(
            "Max file size exceeded.\n" +
                    "Selected file: ${FileSize(fileSize).bytes} bytes\n" +
                    "Max allowed size: ${maxFileSize.bytes} bytes"
        )
    }

    @Throws(Exception::class)
    private fun send(content: SentFile): TransferId {
        // TODO: Save the TransferId to DB.
        return TransferId(manager.send(
            content.fileName,
            content.fileExtension,
            content.data.rawBytes,
            content.recipient.id,
            RETRY_COUNT,
            content.preview?.rawBytes,
            SendProgressCallback(content.progressCallback),
            PROGRESS_POLL_INTERVAL_MS
        ))
    }

    @Throws(Exception::class)
    override fun registerSendProgressCallback(
        transferId: TransferId,
        callback: SentFileProgressCallback,
    ) = manager.registerSendProgressCallback(
        transferId.tid,
        SendProgressCallback(callback),
        PROGRESS_POLL_INTERVAL_MS
    )

    @Throws(Exception::class)
    override fun resend(transferId: TransferId) {
        // Removed from latest bindings
//        manager.resend(transferId.tid)
    }

    @Throws(Exception::class)
    override fun closeSend(transferId: TransferId) = manager.closeSend(transferId.tid)

    @Throws(Exception::class)
    override fun registerReceiveProgressCallback(
        transferId: TransferId,
        callback: ReceivedFileProgressCallback,
    ) = manager.registerReceiveProgressCallback(
        transferId.tid,
        ReceiveProgressCallback(callback),
        PROGRESS_POLL_INTERVAL_MS
    )

    @Throws(Exception::class)
    override suspend fun receive(transferId: TransferId): FileData {
        Timber.d("[File Transfer] Attempting to receive transfer ID ${transferId.tid.toBase64String()}")
       return FileData(manager.receive(transferId.tid))
    }

    companion object {
        // Update the progress of pending transfers (in/out) every 1s
        private const val PROGRESS_POLL_INTERVAL_MS = 1000L
        // Retry each failed part 1 + RETRY_COUNT times
        private const val RETRY_COUNT = 2.0F
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
    }
}

class FileTransferParams private constructor() {

    companion object Factory {
        fun create(maxThroughput: Int = 150_000): String {
//            return "{\"MaxThroughput\":$maxThroughput}"
            return ""
        }
    }
}

private class IncomingFileTransferCallback(
    private val callback: IncomingFileCallback
) : IncomingFileCallback by callback, FileTransferReceiveFunc {

    override fun receiveCallback(
        tid: ByteArray,
        fileName: String,
        fileType: String,
        sender: ByteArray,
        size: Long,
        preview: ByteArray?
    ) = callback.onFileTransferReceived(
        IncomingFile(
            TransferId(tid),
            fileName,
            fileType,
            Sender(sender),
            size,
            preview?.let { PreviewData(it) }
        )
    )
}

private class SendProgressCallback(
    private val callback: SentFileProgressCallback
) : SentFileProgressCallback by callback, FileTransferSentProgressFunc {

    override fun sentProgressCallback(
        isComplete: Boolean,
        chunksSent: Long,
        chunksDelivered: Long,
        chunksTotal: Long,
        filePartTracker: FilePartTracker,
        error: java.lang.Exception?
    ) = callback.onProgressUpdate(
        isComplete, chunksSent, chunksDelivered, chunksTotal, error
    )
}

private class ReceiveProgressCallback(
    private val callback: ReceivedFileProgressCallback
): ReceivedFileProgressCallback by callback, FileTransferReceivedProgressFunc {

    override fun receivedProgressCallback(
        isComplete: Boolean,
        chunksReceived: Long,
        chunksTotal: Long,
        filePartTracker: FilePartTracker?,
        error: java.lang.Exception?
    ) = callback.onProgressUpdate(isComplete, chunksReceived, chunksTotal, error)
}
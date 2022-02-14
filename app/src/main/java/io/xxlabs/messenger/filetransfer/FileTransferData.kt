package io.xxlabs.messenger.filetransfer

import android.net.Uri

@JvmInline
value class FileData(val rawBytes: ByteArray)

@JvmInline
value class PreviewData(val rawBytes: ByteArray)

@JvmInline
value class Recipient(val id: ByteArray)

@JvmInline
value class Sender(val id: ByteArray)

data class IncomingFile(
    override val transferId: TransferId,
    override val fileName: String,
    override val fileExtension: String,
    val sender: Sender,
    val size: Long,
    override val preview: PreviewData?,
) : TransferredFile

data class OutgoingFile(
    val uri: Uri,
    val recipient: Recipient,
    val progressCallback: SentFileProgressCallback
)

data class SentFile(
    override val transferId: TransferId,
    override val fileName: String,
    override val fileExtension: String,
    val data: FileData,
    override val preview: PreviewData?,
    val recipient: Recipient,
    val progressCallback: SentFileProgressCallback,
    val uri: Uri = Uri.EMPTY
) : TransferredFile
package io.xxlabs.messenger.filetransfer

interface FileTransferRepository {
    val maxFilePreviewSize: FileSize
    val maxFileNameByteLength: Long
    val maxFileTypeByteLength: Long
    val maxFileSize: FileSize

    @Throws(Exception::class)
    fun send(content: OutgoingFile): SentFile

    @Throws(Exception::class)
    fun registerSendProgressCallback(
        transferId: TransferId,
        callback: SentFileProgressCallback
    )

    @Throws(Exception::class)
    fun resend(transferId: TransferId)

    @Throws(Exception::class)
    fun closeSend(transferId: TransferId)

    @Throws(Exception::class)
    fun registerReceiveProgressCallback(
        transferId: TransferId,
        callback: ReceivedFileProgressCallback
    )

    @Throws(Exception::class)
    suspend fun receive(transferId: TransferId): FileData
}

@JvmInline
value class TransferId(val tid: ByteArray)

@JvmInline
value class FileSize(val bytes: Long) {
    val kb: Long
        get() = bytes / 1_000

    val mb: Long
        get() = bytes / 1_000_000
}
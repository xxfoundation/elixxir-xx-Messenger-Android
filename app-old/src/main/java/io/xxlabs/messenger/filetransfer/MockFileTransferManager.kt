package io.xxlabs.messenger.filetransfer

class MockFileTransferManager() : FileTransferRepository {
    override val maxFilePreviewSize = FileSize(4_000)
    override val maxFileNameByteLength: Long = 32
    override val maxFileTypeByteLength: Long = 8
    override val maxFileSize = FileSize(4_000_000)

    override fun send(content: OutgoingFile): SentFile {
        TODO("Not yet implemented")
    }

    override fun registerSendProgressCallback(
        transferId: TransferId,
        callback: SentFileProgressCallback
    ) {
        TODO("Not yet implemented")
    }

    override fun resend(transferId: TransferId) {
        TODO("Not yet implemented")
    }

    override fun closeSend(transferId: TransferId) {
        TODO("Not yet implemented")
    }

    override fun registerReceiveProgressCallback(
        transferId: TransferId,
        callback: ReceivedFileProgressCallback
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun receive(transferId: TransferId): FileData {
        TODO("Not yet implemented")
    }
}
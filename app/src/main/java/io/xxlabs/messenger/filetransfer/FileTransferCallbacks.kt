package io.xxlabs.messenger.filetransfer

interface IncomingFileCallback {
    /**
     * Called every time an incoming file transfer is received.
     */
    fun onFileTransferReceived(file: IncomingFile)
}

interface ReceivedFileProgressCallback {
    /**
     * Called every time the progress of the incoming file is updated.
     */
    fun onProgressUpdate(
        isComplete: Boolean,
        chunksReceived: Long,
        chunksTotal: Long,
        error: Exception?
    )
}

interface SentFileProgressCallback {
    /**
     * Called every time the progress of the outgoing file is updated.
     */
    fun onProgressUpdate(
        isComplete: Boolean,
        chunksSent: Long,
        chunksDelivered: Long,
        chunksTotal: Long,
        error: Exception?
    )
}
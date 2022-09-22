package io.xxlabs.messenger.filetransfer

interface TransferredFile {
    val transferId: TransferId
    val fileName: String
    val fileExtension: String
    val preview: PreviewData?
}

val TransferredFile.fileType: FileType
    get() = when {
        isAudio() -> FileType.AUDIO
        isVideo() -> FileType.VIDEO
        isImage() -> FileType.IMAGE
        isDocument() -> FileType.DOCUMENT
        else -> FileType.OTHER
    }

fun TransferredFile.isImage(): Boolean {
    return when(fileExtension) {
        "jpg", "jpeg", "png", "gif", "heif" -> true
        else -> false
    }
}

fun TransferredFile.isVideo(): Boolean {
    return when(fileExtension) {
        "mp4", "hevc" -> true
        else -> false
    }
}

fun TransferredFile.isAudio(): Boolean {
    return when(fileExtension) {
        "mp3", "3gp", "wav", "m4a" -> true
        else -> false
    }
}

fun TransferredFile.isDocument(): Boolean {
    return when(fileExtension) {
        "txt", "doc", "ppt", "pdf" -> true
        else -> false
    }
}
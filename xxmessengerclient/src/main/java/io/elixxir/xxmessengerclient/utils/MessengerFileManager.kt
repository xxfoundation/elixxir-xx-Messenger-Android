package io.elixxir.xxmessengerclient.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date

class MessengerFileManager(private val appDirectory: String) {
    fun isDirectoryEmpty(path: String): Boolean {
        return with (File(appDirectory, path)) {
            listFiles()?.isEmpty() ?: !exists()
        }
    }

    fun removeItem(path: String) {
        File(appDirectory, path).apply {
            if (exists()) deleteRecursively()
        }
    }

    fun createDirectory(path: String) {
        File(appDirectory, path).mkdir()
    }

    fun saveFile(path: String, data: ByteArray) {
        val file = File(appDirectory, path.substringAfterLast("/"))
        FileOutputStream(file).use {
            it.write(data)
        }
    }

    fun loadFile(path: String): ByteArray {
        val file = File(appDirectory, path)
        var data: ByteArray
        FileInputStream(file).use {
            data = it.readBytes()
        }

        return data
    }

    fun modifiedTime(path: String): Date {
        return Date(File(appDirectory, path).lastModified())
    }
}
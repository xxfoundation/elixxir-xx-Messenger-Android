package io.elixxir.xxmessengerclient.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Date

interface MessengerFileManager {
    fun doesDirectoryExist(path: String): Boolean
    fun isDirectoryEmpty(path: String): Boolean
    fun removeItem(path: String)
    fun createDirectory(path: String)
    fun saveFile(path: String, data: ByteArray)
    fun loadFile(path: String): ByteArray
    fun modifiedTime(path: String): Date
}

class AndroidFileManager(private val appDirectory: String) : MessengerFileManager {

    override fun doesDirectoryExist(path: String): Boolean {
        return File(appDirectory, path).exists()
    }

    override fun isDirectoryEmpty(path: String): Boolean {
        return with (File(appDirectory, path)) {
            listFiles()?.isEmpty() ?: !exists()
        }
    }

    override fun removeItem(path: String) {
        File(appDirectory, path).apply {
            if (exists()) deleteRecursively()
        }
    }

    override fun createDirectory(path: String) {
        File(appDirectory, path).mkdir()
    }

    override fun saveFile(path: String, data: ByteArray) {
        val file = File(appDirectory, path.substringAfterLast("/"))
        FileOutputStream(file).use {
            it.write(data)
        }
    }

    override fun loadFile(path: String): ByteArray {
        val file = File(appDirectory, path)
        var data: ByteArray
        FileInputStream(file).use {
            data = it.readBytes()
        }

        return data
    }

    override fun modifiedTime(path: String): Date {
        return Date(File(appDirectory, path).lastModified())
    }
}
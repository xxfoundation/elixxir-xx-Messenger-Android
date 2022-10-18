package io.elixxir.xxmessengerclient.utils

import android.content.Context
import java.util.Date

class MessengerFileManager(private val context: Context) {
    fun isDirectoryEmpty(path: String): Boolean {
        return false
    }

    fun removeItem(path: String) {
        TODO()
    }

    fun createDirectory(path: String) {
        TODO()
    }

    fun saveFile(path: String, data: ByteArray) {
        TODO()
    }

    fun loadFile(path: String): ByteArray {
        TODO()
    }

    fun modifiedTime(path: String): Date {
        TODO()
    }
}
package io.xxlabs.messenger.backup.data.restore

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Json model object for backup data
 */
data class BackupReport(
    @SerializedName("RestoredContacts")
    val contacts: Array<String> = arrayOf(),
    @SerializedName("Params")
    val extrasJson: String? = ExtrasJson().toString()
) {
    private val userData: ExtrasJson? by lazy { ExtrasJson.from(extrasJson) }

    val userName: String? get() = userData?.userName
    val nameStringified: String? get() = userName?.run {
        if (isNotEmpty()) "U$this"
        else this
    }

    val userEmail: String? get() = userData?.userEmail
    val emailStringified: String? get() = userEmail?.run {
        if (isNotEmpty()) "E$this"
        else this
    }

    val userPhone: String? get() = userData?.userPhone
    val phoneStringified: String? get() = userPhone?.run {
        if (isNotEmpty()) "P$this"
        else this
    }

    fun getContacts(): ByteArray = Gson().toJson(contacts).encodeToByteArray()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BackupReport

        if (!contacts.contentEquals(other.contacts)) return false
        if (extrasJson != other.extrasJson) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contacts.contentHashCode()
        result = 31 * result + extrasJson.hashCode()
        return result
    }

    companion object {
        suspend fun unmarshall(data: ByteArray, saveToPath: String?): BackupReport =
            withContext(Dispatchers.IO) {
                saveToPath?.let { saveToFile(data, it) }
                val reader = data.inputStream().reader()
                Gson().fromJson(reader, BackupReport::class.java)
            }

        private suspend fun saveToFile(data: ByteArray, path: String): File =
            withContext(Dispatchers.IO) {
                val backupFile = File(path).apply {
                    if (this.exists()) delete().also { Timber.d("Previous backup removed") }
                }

                runCatching {
                    val outputStream = FileOutputStream(backupFile.path)
                    outputStream.use { stream ->
                        stream.write(data)
                    }.also { Timber.d("Backup saved to $path.") }
                }

                backupFile
            }
    }
}

/**
 * Json model object for [BackupReport.extrasJson]. Backs up front-end data.
 */
data class ExtrasJson(
    @SerializedName("username")
    val userName: String? = "",
    @SerializedName("email")
    val userEmail: String? = "",
    @SerializedName("phone")
    val userPhone: String? = "",
) {

    override fun toString(): String = Gson().toJson(this)

    companion object {
        fun from(jsonString: String?) = Gson().fromJson(jsonString, ExtrasJson::class.java)
    }
}
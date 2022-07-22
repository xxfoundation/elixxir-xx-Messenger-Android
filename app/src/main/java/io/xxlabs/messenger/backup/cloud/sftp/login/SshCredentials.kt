package io.xxlabs.messenger.backup.cloud.sftp.login

import com.google.gson.Gson
import java.io.Serializable

data class SshCredentials(
    val host: String,
    val port: String,
    val username: String,
    val password: String
) : Serializable {

    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): SshCredentials =
            Gson().fromJson(json, SshCredentials::class.java)
    }
}
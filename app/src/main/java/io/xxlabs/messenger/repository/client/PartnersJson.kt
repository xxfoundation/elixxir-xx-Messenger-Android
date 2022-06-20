package io.xxlabs.messenger.repository.client

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun unmarshallPartners(data: ByteArray): Array<String> =
    withContext(Dispatchers.IO) {
        val reader = data.inputStream().reader()
        Gson().fromJson(reader, Array<String>::class.java)
    }
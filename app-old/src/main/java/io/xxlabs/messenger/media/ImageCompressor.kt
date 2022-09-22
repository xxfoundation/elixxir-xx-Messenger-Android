package io.xxlabs.messenger.media

import android.content.Context
import java.io.File

interface ImageCompressor {
    suspend fun compress(context: Context, image: File, destination: File? = null): File
}
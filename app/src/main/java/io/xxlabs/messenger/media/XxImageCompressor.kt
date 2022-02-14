package io.xxlabs.messenger.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import timber.log.Timber
import java.io.File

class XxImageCompressor : ImageCompressor {

    override suspend fun compress(
        context: Context,
        image: File,
        destination: File?
    ): File {

        Timber.d("Size before: ${image.length()} bytes")

        val result = destination?.let {
            Compressor.compress(context, image) {
                default()
                destination(destination)
            }
        } ?: Compressor.compress(
            context,
            image
        )

        Timber.d("Size after: ${result.length()} bytes")

        return result
    }
}
package io.xxlabs.messenger.media

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.misc.DebugLogger.Companion.isExternalStorageWritable
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object ImageSaver {

    private val defaultFileName: String
        get() {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            return "xxm_$timeStamp.jpeg"
        }

    // Get the directory for the user's public pictures directory.
    private val publicAlbumStorageDir: File
        get() {
            // Get the directory for the user's public pictures directory.
            val file = File(
                appContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "xx messenger"
            )
            if (!file.mkdirs()) {
                Timber.i("Could not create pictures directory")
            }
            return file
        }

    /**
     * Save an image to the MediaStore (Android Q+) or external files directory.
     */
    fun saveImage(
        bitmap: Bitmap,
        imageFileName: String = defaultFileName,
        onSuccess: (String) -> Unit = ::displaySuccessToast,
        onError: (String?) -> Unit = ::displayErrorToast
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(bitmap, imageFileName, onSuccess, onError)
            } else {
                saveToExternalFilesDir(bitmap, imageFileName, onSuccess, onError)
            }
        } catch (e: Exception) {
            onError("This image was sent from an older version and cannot be saved.")
        }
    }

    private fun saveToExternalFilesDir(
        bitmap: Bitmap,
        imageFileName: String,
        onSuccess: (String) -> Unit,
        onError: (String?) -> Unit
    ) {
        var savedImagePath: String?
        val storageDir: File

        if (isExternalStorageWritable) {
            storageDir = publicAlbumStorageDir
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val outputStream: OutputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                galleryAddPic(savedImagePath, onSuccess)
            } catch (e: Exception) {
                onError(null)
            }
        }
    }

    private fun saveToMediaStore(
        bitmap: Bitmap,
        displayName: String,
        onSuccess: (String) -> Unit,
        onError: (String?) -> Unit,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        mimeType: String = "image/jpg",
    ) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }

        var uri: Uri? = null

        runCatching {
            with(appContext().contentResolver) {
                insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.also {
                    uri = it // Keep uri reference so it can be removed on failure

                    openOutputStream(it)?.use { stream ->
                        if (!bitmap.compress(format, 95, stream))
                            throw IOException("Failed to save bitmap.")
                    } ?: throw IOException("Failed to open output stream.")

                } ?: throw IOException("Failed to create new MediaStore record.")
                uri?.path?.apply { onSuccess(this) }
            }
        }.getOrElse {
            uri?.let { orphanUri ->
                // Don't leave an orphan entry in the MediaStore
                appContext().contentResolver.delete(orphanUri, null, null)
            }
            onError(null)
        }
    }

    private fun galleryAddPic(
        imagePath: String,
        onSuccess: (String) -> Unit
    ) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imagePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        appContext().sendBroadcast(mediaScanIntent)
        onSuccess(imagePath)
    }

    private fun displaySuccessToast(saveLocation: String) {
        val message = "Saved to $saveLocation"
        Toast.makeText(
            appContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayErrorToast(errorMsg: String? = "Error saving image") {
        Toast.makeText(
            appContext(),
            errorMsg,
            Toast.LENGTH_LONG
        ).show()
    }
}
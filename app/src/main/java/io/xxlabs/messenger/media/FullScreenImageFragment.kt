package io.xxlabs.messenger.media

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.FragmentFullscreenImageBinding
import io.xxlabs.messenger.ui.base.BaseFragment
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class FullScreenImageFragment : BaseFragment() {

    private lateinit var binding: FragmentFullscreenImageBinding

    private val uri: String by lazy {
        FullScreenImageFragmentArgs.fromBundle(requireArguments()).imageUri
    }

    private val imageUI: FullScreenImageUI by lazy {
        FullScreenImage(uri, ::saveImage)
    }

    // Get the directory for the user's public pictures directory.
    private val publicAlbumStorageDir: File
        get() {
            // Get the directory for the user's public pictures directory.
            val file = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "xx messenger"
            )
            if (!file.mkdirs()) {
                Timber.i("Could not create pictures directory")
            }
            return file
        }

    /* Checks if external storage is available for read and write */
    private val isExternalStorageWritable: Boolean
        get() {
            val state = Environment.getExternalStorageState()
            return Environment.MEDIA_MOUNTED == state
        }

    private val bitmap: Bitmap by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(
                requireContext().contentResolver,
                Uri.parse(uri)
            )
            ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(
                requireContext().contentResolver,
                Uri.parse(uri)
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_fullscreen_image,
            container,
            false
        )

        binding.lifecycleOwner = viewLifecycleOwner
        binding.ui = imageUI
        binding.mediaImageView.apply {
            Glide.with(requireContext())
                .load(uri)
                .into(this)
        }
        return binding.root
    }

    private fun saveImage() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "xxm_$timeStamp.jpeg"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(bitmap, imageFileName)
            } else {
                saveToExternalFilesDir(bitmap, imageFileName)
            }
        } catch (e: Exception) {
            showError("This image was sent from an older version and cannot be saved.")
        }
    }

    private fun saveToExternalFilesDir(
        bitmap: Bitmap,
        imageFileName: String
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
                galleryAddPic(savedImagePath)
            } catch (e: Exception) {
                displayErrorToast()
            }
        }
    }

    private fun saveToMediaStore(
        bitmap: Bitmap,
        displayName: String,
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
            with(requireContext().contentResolver) {
                insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)?.also {
                    uri = it // Keep uri reference so it can be removed on failure

                    openOutputStream(it)?.use { stream ->
                        if (!bitmap.compress(format, 95, stream))
                            throw IOException("Failed to save bitmap.")
                    } ?: throw IOException("Failed to open output stream.")

                } ?: throw IOException("Failed to create new MediaStore record.")
                uri?.path?.apply { displaySuccessToast(this) }
            }
        }.getOrElse {
            uri?.let { orphanUri ->
                // Don't leave an orphan entry in the MediaStore
                requireContext().contentResolver.delete(orphanUri, null, null)
            }
            displayErrorToast()
        }
    }

    private fun galleryAddPic(imagePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imagePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        requireContext().sendBroadcast(mediaScanIntent)
        displaySuccessToast(imagePath)
    }

    private fun displaySuccessToast(saveLocation: String) {
        val message = "Saved to $saveLocation"
        Toast.makeText(
            context,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun displayErrorToast() {
        Toast.makeText(
            context,
            "Error saving image",
            Toast.LENGTH_LONG
        ).show()
    }
}

private data class FullScreenImage(
    override val imageUri: String,
    private val saveImage: () -> Unit,
) : FullScreenImageUI {

    override fun onDownloadClicked() = saveImage()
}
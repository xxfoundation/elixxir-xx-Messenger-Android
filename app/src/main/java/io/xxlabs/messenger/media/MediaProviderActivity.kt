package io.xxlabs.messenger.media

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.ui.base.BaseKeystoreActivity
import java.io.File

/**
 * Superclass for Activities that perform actions that require sensitive permissions
 * and pass the action result to a [MediaCallback] or [MicrophoneCallback],
 * depending on the action.
 */
abstract class MediaProviderActivity :
    BaseKeystoreActivity(),
    CameraProvider,
    MicrophoneProvider,
    DeviceStorageProvider
{
    /* Network follower flag */

    /**
     * Overrides the app's default behavior of stopping the network follower
     * when no longer in foreground.
     */
    protected var shouldStopNetworkFollower: Boolean = true

    /* MediaProvider callbacks */

    private var callback: MediaCallback? = null
    private var micCallback: MicrophoneCallback? = null

    private var mimeTypes: List<String> = listOf()
    private var multipleSelections: Boolean = false

    /* Permission request launchers */

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startCamera()
        else callback = null
    }

    private val videoPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startVideo()
        else callback = null
    }

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) micCallback?.onMicrophonePermissionGranted()
        micCallback = null
    }

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) browseFiles()
        else callback = null
    }

    // Holds a Uri reference to the last video or photo created
    private var latestMediaUri: Uri = Uri.EMPTY

    /* Take photo */

    private val tempPhotoUri: Uri
        get() {
            val tempFile = File.createTempFile(
                FILENAME,
                ".jpg",     // TODO: Remove hardcoded file extension
                getExternalFilesDir(Environment.DIRECTORY_PICTURES))

            return FileProvider.getUriForFile(
                applicationContext,
                AUTHORITY,
                tempFile
            )
        }

    private val startCamera: () -> Unit = {
        latestMediaUri = tempPhotoUri
        cameraLauncher.launch(latestMediaUri)

        shouldStopNetworkFollower = false
    }

    private val cameraResultCallback = ActivityResultCallback<Boolean> { isSuccess ->
        if (isSuccess) {
            callback?.onFilesSelected(listOf(latestMediaUri))
        }
        callback = null
        shouldStopNetworkFollower = true
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
        cameraResultCallback
    )

    /* Record video */

    private val videoResultCallback = ActivityResultCallback<Bitmap> { videoThumbnail ->
        videoThumbnail?.let {
            callback?.onFilesSelected(listOf(latestMediaUri))
        }
        callback = null
        shouldStopNetworkFollower = true
    }

    private val videoLauncher = registerForActivityResult(
        ActivityResultContracts.TakeVideo(),
        videoResultCallback
    )

    private val tempVideoUri: Uri
        get() {
            val tempFile = File.createTempFile(
                FILENAME,
                ".mp4",    // TODO: Remove hardcoded file extension
                getExternalFilesDir(Environment.DIRECTORY_PICTURES))

            return FileProvider.getUriForFile(
                applicationContext,
                AUTHORITY,
                tempFile
            )
        }

    private val startVideo: () -> Unit = {
        latestMediaUri = tempVideoUri
        videoLauncher.launch(latestMediaUri)

        shouldStopNetworkFollower = false
    }

    /* Browse for files */

    private val browseFiles: () -> Unit = {
        if (multipleSelections) manyFileBrowserLauncher.launch("*/*")
        else singleFileBrowserLauncher.launch("*/*")

        shouldStopNetworkFollower = false
    }

    /* Browse for single file */

    private val singleFileBrowserResultCallback = ActivityResultCallback<Uri> { uri ->
        uri?.let {
            callback?.onFilesSelected(listOf(uri))
        }
        callback = null
        shouldStopNetworkFollower = true
    }

    private val singleFileBrowserLauncher = registerForActivityResult(
        object : ActivityResultContracts.GetContent() {

            override fun createIntent(context: Context, input: String): Intent {
                return super.createIntent(context, input).apply {
                    type = mimeTypes.joinToString(", ")
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
                }
            }
        },
        singleFileBrowserResultCallback
    )

    /* Browse for many files */

    private val manyFileBrowserResultCallback = ActivityResultCallback<List<Uri>> { uriList ->
        uriList?.let {
            callback?.onFilesSelected(uriList)
        }
        callback = null
        shouldStopNetworkFollower = true
    }

    private val manyFileBrowserLauncher = registerForActivityResult(
        object : ActivityResultContracts.GetMultipleContents() {

            override fun createIntent(context: Context, input: String): Intent {
                return super.createIntent(context, input).apply {
                    type = mimeTypes.joinToString(", ")
                    putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes.toTypedArray())
                }
            }
        },
        manyFileBrowserResultCallback
    )

    /* Sensitive requests */

    override fun startCamera(callback: MediaCallback, forVideo: Boolean) {
        if (this.callback != null) return

        this.callback = callback

        val cameraPermission = Manifest.permission.CAMERA
        when (PermissionChecker.checkSelfPermission(this, cameraPermission)) {
            PermissionChecker.PERMISSION_GRANTED -> {
                if (forVideo) startVideo()
                else startCamera()
            }
            else -> {
                if (forVideo) videoPermissionLauncher.launch(cameraPermission)
                else cameraPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    override fun requestRecordAudioPermission(micCallback: MicrophoneCallback) {
        if (this.micCallback != null) return

        this.micCallback = micCallback

        val recordAudioPermission = Manifest.permission.RECORD_AUDIO
        when (PermissionChecker.checkSelfPermission(this, recordAudioPermission)) {
            PermissionChecker.PERMISSION_GRANTED -> {
                micCallback.onMicrophonePermissionGranted()
                this.micCallback = null
            }
            else -> recordAudioPermissionLauncher.launch(recordAudioPermission)
        }
    }

    override fun selectFiles(
        callback: MediaCallback,
        mimeTypes: List<String>,
        multipleSelections: Boolean
    ) {
        if (this.callback != null) return

        this.callback = callback
        this.mimeTypes = mimeTypes
        this.multipleSelections = multipleSelections

        val storagePermission = Manifest.permission.READ_EXTERNAL_STORAGE
        when (PermissionChecker.checkSelfPermission(this, storagePermission)) {
            PermissionChecker.PERMISSION_GRANTED -> browseFiles()
            else -> storagePermissionLauncher.launch(storagePermission)
        }
    }

    companion object {
        private val FILENAME get() = "xxm_${System.currentTimeMillis()}"
        private const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
    }
}
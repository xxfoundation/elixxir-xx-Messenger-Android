package io.xxlabs.messenger.support.util

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import timber.log.Timber
import java.io.File


object FileUtils {
    var TAG = "TAG"
    private const val PRIMARY_VOLUME_NAME = "primary"

    fun getDirectoryPath(
        con: Context,
        treeUri: Uri?
    ): String? {
        if (treeUri == null) return null
        var volumePath = getVolumePath(
            getVolumeIdFromTreeUri(
                treeUri
            ), con
        )
            ?: return File.separator
        if (volumePath.endsWith(File.separator)) volumePath =
            volumePath.substring(0, volumePath.length - 1)
        var documentPath =
            getDocumentPathFromTreeUri(
                treeUri
            )
        if (documentPath!!.endsWith(File.separator)) documentPath =
            documentPath.substring(0, documentPath.length - 1)
        return if (documentPath.isNotEmpty()) {
            if (documentPath.startsWith(File.separator)) volumePath + documentPath else volumePath + File.separator + documentPath
        } else volumePath
    }

    private fun getVolumePath(
        volumeId: String?,
        context: Context
    ): String? {
        return try {
            val mStorageManager =
                context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumeClazz =
                Class.forName("android.os.storage.StorageVolume")
            val getVolumeList =
                mStorageManager.javaClass.getMethod("getVolumeList")
            val getUuid = storageVolumeClazz.getMethod("getUuid")
            val getPath = storageVolumeClazz.getMethod("getPath")
            val isPrimary = storageVolumeClazz.getMethod("isPrimary")
            val result = getVolumeList.invoke(mStorageManager)
            val length = java.lang.reflect.Array.getLength(result!!)
            for (i in 0 until length) {
                val storageVolumeElement = java.lang.reflect.Array.get(result, i)
                val uuid = getUuid.invoke(storageVolumeElement) as String?
                val primary =
                    isPrimary.invoke(storageVolumeElement) as Boolean

                // primary volume?
                if (primary && PRIMARY_VOLUME_NAME == volumeId) return getPath.invoke(
                    storageVolumeElement
                ) as String

                // other volumes?
                if (uuid != null && uuid == volumeId) return getPath.invoke(storageVolumeElement) as String
            }
            // not found.
            null
        } catch (ex: Exception) {
            null
        }
    }

    private fun getVolumeIdFromTreeUri(treeUri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        Timber.v("DocId: $docId")
        val split = docId.split(":").toTypedArray()
        return if (split.isNotEmpty()) {
            split[0]
        } else {
            null
        }
    }

    private fun getDocumentPathFromTreeUri(treeUri: Uri): String? {
        val docId = DocumentsContract.getTreeDocumentId(treeUri)
        Timber.v("DocId: $docId")
        val split: Array<String?> = docId.split(":").toTypedArray()
        return if (split.size >= 2 && split[1] != null) split[1] else File.separator
    }


    fun checkPermissionDo(
        fragment: Fragment,
        requestCode: Int,
        f: () -> Unit,
        isCamera: Boolean = false
    ) {
        if (isCamera) {
            if (canWrite(fragment.requireContext()) && canTakePic(fragment.requireContext())) {
                f()
            } else {
                requestCameraPermission(fragment, requestCode)
            }
        } else {
            if (canWrite(fragment.requireContext())) {
                f()
            } else {
                requestWritePermission(fragment, requestCode)
            }
        }
    }

    fun checkPermissionDo(
        activity: Activity,
        requestCode: Int,
        f: () -> Unit,
        isCamera: Boolean = false
    ) {
        if (isCamera) {
            if (canTakePic(activity)) {
                f()
            } else {
                requestCameraPermission(activity, requestCode)
            }
        } else {
            if (canWrite(activity)) {
                f()
            } else {
                requestWritePermission(activity, requestCode)
            }
        }
    }

    private fun requestCameraPermission(fragment: Fragment, requestCode: Int) {
        Handler(Looper.getMainLooper()).post {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCode
            )
        }
    }

    private fun requestCameraPermission(activity: Activity, requestCode: Int) {
        Handler(Looper.getMainLooper()).post {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCode
            )
        }
    }

    private fun requestWritePermission(fragment: Fragment, requestCode: Int) {
        Handler(Looper.getMainLooper()).post {
            fragment.requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCode
            )
        }
    }

    private fun requestWritePermission(activity: Activity, requestCode: Int) {
        Handler(Looper.getMainLooper()).post {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCode
            )
        }
    }

    private fun canWrite(context: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun canTakePic(context: Context): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED)
    }

    fun getFilePath(context: Context, uri: Uri): String? {
        // ExternalStorageProvider
        val mExternalStorageAvailable: Boolean
        val mExternalStorageWriteable: Boolean
        val state: String = Environment.getExternalStorageState()

        when {
            Environment.MEDIA_MOUNTED == state -> {
                // We can read and write the media
                mExternalStorageWriteable = true
                mExternalStorageAvailable = mExternalStorageWriteable
            }
            Environment.MEDIA_MOUNTED_READ_ONLY == state -> {
                // We can only read the media
                mExternalStorageAvailable = true
                mExternalStorageWriteable = false
            }
            else -> {
                // Something else is wrong. It may be one of many other states, but all we need
                //  to know is we can neither read nor write
                mExternalStorageWriteable = false
                mExternalStorageAvailable = mExternalStorageWriteable
            }
        }

        if (!mExternalStorageAvailable || !mExternalStorageWriteable) {
            return "-1"
        }
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(
                    uri
                )
            ) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":", "/").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return if (mExternalStorageAvailable) {
                        if (split.size > 2) {
                            Environment.getExternalStorageDirectory()
                                .toString() + "/" + split[1] + "/" + split[2]
                        } else {
                            Environment.getExternalStorageDirectory()
                                .toString() + "/" + split[1]
                        }
                    } else {
                        if (split.size > 2) {
                            context.getExternalFilesDir(split[1]).toString() + "/" + split[2]
                        } else {
                            context.getExternalFilesDir(null).toString() + "/" + split[1]
                        }
                    }
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(
                    uri
                )
            ) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(
                    context,
                    contentUri,
                    null,
                    null
                )
            } else if (isMediaDocument(
                    uri
                )
            ) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                when (type) {
                    "image" -> {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    }
                    "video" -> {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    }
                    "audio" -> {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(
                    context,
                    contentUri,
                    selection,
                    selectionArgs
                )
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(
                    uri
                )
            ) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.contentResolver.query(
                uri!!, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}
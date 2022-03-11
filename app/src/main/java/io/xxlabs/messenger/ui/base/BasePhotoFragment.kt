package io.xxlabs.messenger.ui.base

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.dialog.PopupActionDialog
import io.xxlabs.messenger.support.extensions.toast
import io.xxlabs.messenger.support.util.FileUtils
import io.xxlabs.messenger.support.view.BitmapResolver
import kotlinx.android.synthetic.main.fragment_contact_details.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

abstract class BasePhotoFragment(val layout: Int = R.layout.fragment_contact_details) :
    BaseFragment() {
    protected lateinit var root: View
    lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment with the ProductGrid theme
        root = inflater.inflate(layout, container, false)
        return root
    }

    protected fun clearPhoto(view: ImageView) {
        Glide.with(requireActivity()).clear(view)
        view.setBackgroundResource(0)

        if (view.id == R.id.contactDetailsPhoto) {
            contactDetailsPhotoDefault.visibility = View.VISIBLE
            contactDetailsPhoto.visibility = View.GONE
            view.setBackgroundResource(R.drawable.component_circular_bg)
            view.backgroundTintList =
                ContextCompat.getColorStateList(view.context, R.color.brand_light)
        } else {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    view.context,
                    R.color.brand_light
                )
            )
        }
    }

    protected fun loadPhoto(view: ImageView, photo: Bitmap?, fullSize: Boolean = false) {
        if (fullSize) {
            Glide.with(requireContext())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .override(Resources.getSystem().displayMetrics.widthPixels)
                .load(photo).into(bitmapTarget(view))
        } else {
            Glide.with(requireContext())
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .load(photo)
                .circleCrop()
                .into(bitmapTarget(view))
        }
    }

    private fun bitmapTarget(view: ImageView) = object : CustomTarget<Bitmap>() {
        override fun onLoadFailed(errorDrawable: Drawable?) {
            view.setImageDrawable(errorDrawable)
        }

        override fun onResourceReady(
            resource: Bitmap,
            transition: Transition<in Bitmap>?
        ) {
            view.setImageBitmap(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            try {
                Glide.with(this@BasePhotoFragment).clear(view)
                view.setImageDrawable(null)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    abstract fun changeContactPhoto(photo: Bitmap)
    abstract fun onImageNotSelectedOrRevoked()

    protected fun getBitmapArray(photo: Bitmap): ByteArray {
        val blob = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 80, blob)
        return blob.toByteArray()
    }

    protected fun bitmapArrayToBitmap(photo: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(
            photo, 0, photo.size
        )
    }

    protected fun getResizedBitmap(
        image: Bitmap,
        maxSize: Int = image.width + image.height
    ): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    private fun loadImageFromResult(resultData: Intent? = null, isFromCamera: Boolean = false) {
        try {
            if (isFromCamera) {
                Timber.v("Current photo path: $currentPhotoPath")
                loadBitmap(BitmapFactory.decodeFile(currentPhotoPath))
            } else {
                val pickedImage = resultData?.data
                Timber.v("Picked image path: $pickedImage")
                if (pickedImage != null) {
                    val bitmap = BitmapResolver.getBitmap(pickedImage)
                    loadBitmap(bitmap)
                }
            }
        } catch (err: Exception) {
            err.printStackTrace()
            showError("Error while saving contact photo, try again.")
        }
    }

    private fun loadBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            showError("Malformed image, try again.")
        } else {
            Timber.v("Bitmap loaded!")
            changeContactPhoto(bitmap)
        }
    }

    protected fun requestPermissionChoosePhoto() {
        FileUtils.checkPermissionDo(
            this,
            REQUEST_CODE_WRITE_EXTERNAL_STORAGE,
            { openGallery() })
    }

    protected fun requestPermissionTakePhoto() {
        FileUtils.checkPermissionDo(
            this,
            REQUEST_CODE_CAMERA, { takePhoto() }, true
        )
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = File(requireContext().filesDir, "temp")
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    protected fun takePhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    showError("An error occurred while opening your camera: ${ex.localizedMessage}")
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = getUriForFile(
                        requireContext(),
                        authority,
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, RESULT_CAMERA)
                }
            }
        }
    }

    protected fun openGallery() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(
            photoPickerIntent,
            RESULT_LOAD_IMAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE || requestCode == REQUEST_CODE_CAMERA) {
            Timber.v("Permission result for request code = $requestCode")
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Timber.v("Is granted")
                if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
                    openGallery()
                } else {
                    takePhoto()
                }
            } else {
                val permission = if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                } else {
                    Manifest.permission.CAMERA
                }

                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        permission
                    )
                ) {
                    PopupActionDialog.getInstance(
                        requireContext(),
                        icon = R.drawable.ic_alert_rounded,
                        titleText = "Permission Required",
                        subtitleText = "In order to use this feature, the permission is required. If you deny the permissions, you will be unable to select a photo from your gallery until allowing it again.",
                        positiveBtnText = "Try Again",
                        onClickPositive = {
                            requestPermissionChoosePhoto()
                        },
                        negativeBtnText = "Don't ask again",
                        onClickNegative = {
                            findNavController().navigateUp()
                        }
                    )
                } else {
                    context?.toast("In order to use this feature, the permission is required. Enable it on your settings.")
                    openSettings()
                }
                Timber.v("Is revoked")
                showError("This permission is required in order use this feature.")
                onImageNotSelectedOrRevoked()
            }
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if ((requestCode == RESULT_LOAD_IMAGE)) {
            if (resultCode == Activity.RESULT_OK) {
                loadImageFromResult(resultData)
            } else {
                showError("Image was not selected.")
                onImageNotSelectedOrRevoked()
            }
        } else if ((requestCode == RESULT_CAMERA)) {
            if (resultCode == Activity.RESULT_OK) {
                loadImageFromResult(isFromCamera = true)
            } else {
                showError("Image was not properly taken.")
            }
        }
    }

    abstract fun initComponents(root: View)

    companion object {
        const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 100
        const val REQUEST_CODE_CAMERA = 200
        const val RESULT_CAMERA = 300
        const val RESULT_LOAD_IMAGE = 400
        const val authority = "io.xxlabs.messenger.fileprovider"
    }
}
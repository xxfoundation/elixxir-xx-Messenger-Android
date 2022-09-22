package io.xxlabs.messenger.ui.main.contacts

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.support.dialog.PhotoSelectorDialog
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.ui.base.BasePhotoFragment
import kotlinx.android.synthetic.main.fragment_photo_selection.*
import javax.inject.Inject

class PhotoSelectorFragment : BasePhotoFragment(R.layout.fragment_photo_selection) {
    private var bottomMenu: PhotoSelectorDialog? = null
    private var photoBitmap: Bitmap? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulers: SchedulerProvider
    lateinit var navController: NavController
    var isCropMode = false

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navController = findNavController()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermissionChoosePhoto()
        initComponents(view)
    }

    override fun initComponents(root: View) {
        photoSelectorToolbar.setInsets(topMask = WindowInsetsCompat.Type.systemBars())

        val photo = preferences.userPicture.fromBase64toByteArray()
        refreshPicture(photo)

        bindListeners()
    }

    private fun refreshPicture(photo: ByteArray) {
        if (!photo.contentEquals(byteArrayOf())) {
            photoBitmap = bitmapArrayToBitmap(photo)
            loadPhoto(photoSelectorPhoto, photoBitmap, true)
        } else {
            photoBitmap = null
            val avatar = getDefault().toBitmap(1000, 1000)
            loadPhoto(photoSelectorPhoto, avatar)
        }
    }

    private fun getDefault(): Drawable {
        return ContextCompat.getDrawable(requireContext(), R.drawable.ic_avatar_alt)!!
    }

    private fun bindListeners() {
        photoSelectorPhotoCropConfirm.setOnClickListener {
            onConfirmCrop(photoSelectorPhotoCrop.croppedImage)
        }

        photoSelectorRotateBtn.setOnClickListener {
            photoSelectorPhotoCrop.rotateImage(90)
        }

        photoSelectorBackBtn.setOnClickListener {
            if (isCropMode) {
                setViewMode()
            } else {
                findNavController().navigateUp()
            }
        }

        photoSelectorEditBtn.setOnClickListener {
            openBottomSheetMenu()
        }
    }

    private fun deletePhoto() {
        PopupActionBottomDialogFragment.getInstance(
            titleText = "Are you sure you want to delete your photo?",
            subtitleText = "",
            positiveBtnText = "Yes",
            negativeBtnText = "No, Don't Delete",
            onClickPositive = {
                loadPhoto(
                    photoSelectorPhoto,
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_avatar_alt)
                        ?.toBitmap(1000, 1000),
                    true
                )
                clearUserPicturePreferences()
                photoBitmap = null
            },
            onClickNegative = { },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "deletePhotoMenu")
    }

    private fun clearUserPicturePreferences() {
        preferences.userPicture = ""
    }

    override fun changeContactPhoto(photo: Bitmap) {
        photoSelectorPhotoCrop.setImageBitmap(photo)
        setUserPicturePreferences(photo)
        findNavController().navigateUp()
        //setCropMode()
    }

    override fun onImageNotSelectedOrRevoked() {
        navController.navigateUp()
    }

    private fun onConfirmCrop(bitmap: Bitmap?) {
        if (bitmap != null) {
            photoBitmap = bitmap
            setUserPicturePreferences(bitmap)
            loadPhoto(photoSelectorPhoto, bitmap, true)
        } else {
            showError("Crop error! Try again.")
        }
        setViewMode()
    }

    private fun setUserPicturePreferences(bitmap: Bitmap) {
        preferences.userPicture = getBitmapArray(bitmap).toBase64String()
    }

    private fun setCropMode() {
        isCropMode = true
        photoSelectorPhotoCrop.visibility = View.VISIBLE
        photoSelectorPhotoCropConfirm.visibility = View.VISIBLE
        photoSelectorRotateBtn.visibility = View.VISIBLE
        photoSelectorPhotoHolder.visibility = View.INVISIBLE
        photoSelectorEditBtn.visibility = View.INVISIBLE
    }

    private fun setViewMode() {
        isCropMode = false
        photoSelectorPhotoCrop.visibility = View.INVISIBLE
        photoSelectorPhotoCropConfirm.visibility = View.INVISIBLE
        photoSelectorRotateBtn.visibility = View.INVISIBLE
        photoSelectorPhotoHolder.visibility = View.VISIBLE
        photoSelectorEditBtn.visibility = View.VISIBLE
    }

    private fun openBottomSheetMenu() {
        photoSelectorEditBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        if (bottomMenu == null || bottomMenu?.isVisible == false) {
            bottomMenu = PhotoSelectorDialog.getInstance(
                onClickDelete = if (photoBitmap != null) {
                    { deletePhoto() }
                } else {
                    null
                },
                onClickTakePhoto = { requestPermissionTakePhoto() },
                onClickChoose = { requestPermissionChoosePhoto() },
                onClickCancel = { }
            )
            bottomMenu?.show(childFragmentManager, "individualChatBottomMenu")
        } else {
            bottomMenu?.dismiss()
        }
    }
}
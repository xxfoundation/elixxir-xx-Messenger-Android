package io.xxlabs.messenger.ui.main.ud.profile

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.util.DialogUtils
import io.xxlabs.messenger.ui.base.BaseProfileRegistrationFragment
import io.xxlabs.messenger.ui.dialog.confirm.showConfirmDialog
import kotlinx.android.synthetic.main.fragment_ud_profile.*

/**
 * Fragment representing the login screen for elixxir.
 */
class UdProfileFragment : BaseProfileRegistrationFragment(false) {
    private lateinit var udProfileViewModel: UdProfileViewModel
    var originalBg: Drawable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_ud_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        udProfileViewModel = ViewModelProvider(this, viewModelFactory)
            .get(UdProfileViewModel::class.java)

        initComponents(view)
        watchForChanges()
    }

    override fun initComponents(root: View) {
        udProfileTitle.text = udProfileViewModel.getProfileNickname()
        val photo = preferences.userPicture.fromBase64toByteArray()
        if (!photo.contentEquals(byteArrayOf())) {
            val bitmap = bitmapArrayToBitmap(photo)
            udProfilePhotoDefault.visibility = View.GONE
            udProfilePhoto.setImageBitmap(bitmap)
        }

        udProfileBackBtn.setOnSingleClickListener {
            snackBar.dismiss()
            findNavController().navigateUp()
        }

        udProfilePhotoHolder.setOnSingleClickListener {
            snackBar.dismiss()
            DialogUtils.createPopupDialogFragment(
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_alert_rounded),
                titleText = "Alert",
                subtitleText = "This avatar will only be visible to you",
                positiveBtnText = "Ok",
                onClickPositive = { findNavController().navigateSafe(R.id.action_global_photo_selector) },
                negativeBtnText = "Not now",
                isCancelable = false
            ).show(childFragmentManager, "avatarDialog")
        }

        udProfileViewModel.initUdData()
    }

    override fun changeContactPhoto(photo: Bitmap) {
        udProfilePhotoDefault.visibility = View.GONE
        val resizedBitmap = getResizedBitmap(photo, 150)
        loadPhoto(udProfilePhoto, resizedBitmap, true)
        preferences.userPicture = getBitmapArray(resizedBitmap).toBase64String()
    }

    override fun onImageNotSelectedOrRevoked() {

    }

    private fun watchForChanges() {
        udProfileViewModel.errHandling.observe(viewLifecycleOwner, { err ->
            if (err != null) {
                showError(err, isBindingError = true)
            }
        })

        udProfileViewModel.usernameField.observe(viewLifecycleOwner, { username ->
            udProfileUsername.text = username
        })

        udProfileViewModel.emailField.observe(viewLifecycleOwner, { email ->
            if (email.isNullOrBlank()) {
                udProfileEmail.text = getString(R.string.profile_none_provided)
                udProfileEmailBtn.apply {
                    setOnSingleClickListener { addEmail() }
                    setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_add
                        ),
                        null, null, null
                    )
                    setText(R.string.label_add)
                }
                udProfileEmailBtn.contentDescription = "ud.profile.email.btn.add"
            } else {
                udProfileEmail.text = email
                udProfileEmailBtn.apply {
                    setOnSingleClickListener { showConfirmDeleteDialog(FactType.EMAIL) }
                    setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_remove
                        ),
                        null, null, null
                    )
                    setText(R.string.label_remove)
                }
                udProfileEmailBtn.contentDescription = "ud.profile.email.btn.remove"
            }
        })

        udProfileViewModel.phoneField.observe(viewLifecycleOwner, { phone ->
            udProfilePhoneBtn.contentDescription = "ud.profile.phone.btn.remove"
            if (phone.isNullOrBlank()) {
                udProfilePhone.text = getString(R.string.profile_none_provided)
                udProfilePhoneBtn.apply {
                    setOnSingleClickListener { addPhone() }
                    setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_add
                        ),
                        null, null, null
                    )
                    setText(R.string.label_add)
                }
                udProfilePhoneBtn.contentDescription = "ud.profile.phone.btn.add"
            } else {
                udProfilePhone.text = phone
                udProfilePhoneBtn.apply {
                    setOnSingleClickListener { showConfirmDeleteDialog(FactType.PHONE) }
                    setCompoundDrawablesWithIntrinsicBounds(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_remove
                        ),
                        null, null, null
                    )
                    setText(R.string.label_remove)
                }
                udProfilePhoneBtn.contentDescription = "ud.profile.phone.btn.remove"
            }
        })
    }

    override fun onUdLoaded() {

    }

    override fun onShowDialog(dialogFragment: DialogFragment) {

    }

    private fun showConfirmDeleteDialog(factToDelete: FactType) {
        when (factToDelete) {
            FactType.EMAIL -> confirmDeleteEmail()
            FactType.PHONE -> confirmDeletePhone()
        }
    }

    private fun confirmDeleteEmail() {
        val title = getString(R.string.confirm_delete_fact_dialog_title, "Email")
        val body = getString(R.string.confirm_delete_fact_dialog_body, "email", "email")
        val button = getString(R.string.confirm_delete_fact_dialog_button, "Email")
        showConfirmDialog(
            title,
            body,
            button,
            ::onDeleteEmail
        )
    }

    private fun onDeleteEmail() {
        onRemoveFact(FactType.EMAIL)
    }

    private fun confirmDeletePhone() {
        val title = getString(R.string.confirm_delete_fact_dialog_title, "Phone Number")
        val body = getString(R.string.confirm_delete_fact_dialog_body, "phone number", "phone number")
        val button = getString(R.string.confirm_delete_fact_dialog_button, "Phone Number")
        showConfirmDialog(
            title,
            body,
            button,
            ::onDeletePhone
        )
    }

    private fun onDeletePhone() {
        onRemoveFact(FactType.PHONE)
    }

    override fun onRemoveFact(factToDelete: FactType) {
        udProfileViewModel.removeFact(factToDelete)
    }

    override fun onEnterCode() {
        currentInputPopupDialog?.btnBack?.visibility = View.VISIBLE
        currentInputPopupDialog?.btnBack?.setOnClickListener {
            if (isCurrentDialogEmail) {
                currentInputPopupDialog?.dismissAllowingStateLoss()
                createEmailDialog()
            } else {
                currentInputPopupDialog?.dismissAllowingStateLoss()
                createPhoneDialog()
            }
        }
    }

    override fun onEmailSuccess() {
        dismissDialogAfterSuccess()
        udProfileViewModel.refreshData()
    }

    override fun onPhoneSuccess() {
        dismissDialogAfterSuccess()
        udProfileViewModel.refreshData()
    }

    override fun onSkipEmail() {

    }

    override fun onSkipPhone() {

    }
}

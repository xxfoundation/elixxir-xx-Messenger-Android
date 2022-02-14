package io.xxlabs.messenger.ui.base

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.util.DialogUtils
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.MainActivity
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_contact_details.*
import timber.log.Timber
import javax.inject.Inject

abstract class BaseContactDetailsFragment : BasePhotoFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var schedulers: SchedulerProvider
    lateinit var navController: NavController
    lateinit var networkViewModel: NetworkViewModel
    protected lateinit var contactsViewModel: ContactsViewModel
    protected lateinit var contactDetailsViewModel: ContactDetailsViewModel
    protected lateinit var currContact: ContactData

    abstract fun onPhotoChanged(photo: ByteArray)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        navController = findNavController()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsViewModel =
            ViewModelProvider(
                requireActivity(),
                viewModelFactory
            ).get(ContactsViewModel::class.java)

        networkViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(NetworkViewModel::class.java)

        contactDetailsViewModel =
            ViewModelProvider(
                this,
                viewModelFactory
            ).get(ContactDetailsViewModel::class.java)

        initComponents(view)
    }

    override fun initComponents(root: View) {
        bindToolbar()
        bindProfileStatus()
        contactDetailsRenameBtn.visibility = View.GONE
        contactDetailsPhotoHolder.setOnClickListener {
            openPhotoDialog()
        }
    }

    private fun openPhotoDialog() {
        DialogUtils.createPopupDialogFragment(
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_alert_rounded),
            titleText = "Alert",
            subtitleText = "This avatar will only be visible to you",
            positiveBtnText = "Ok",
            onClickPositive = { requestPermissionChoosePhoto() },
            negativeBtnText = "Not now",
            positiveBtnColor = R.color.red,
            isCancelable = false
        ).show(childFragmentManager, "avatarDialog")
    }

    protected open fun toolbarBgColor(): Int = R.color.neutral_off_white

    protected open fun toolbarBackButtonColor(): Int = R.color.neutral_active

    private fun bindToolbar() {
        toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
        toolbarGeneric.background =
            ContextCompat.getDrawable(requireContext(), toolbarBgColor())
        toolbarGenericBackBtn.setTint(toolbarBackButtonColor())
        toolbarGenericLine.visibility = View.GONE
        toolbarGenericBackBtn.setOnSingleClickListener {
            navController.navigateUp()
        }
    }

    private fun bindProfileStatus() {
        contactDetailsLoading.hide()
    }

    protected fun bindListeners(contactId: ByteArray) {
        contactDetailsNotificationBtn.setOnClickListener {
            it.disableWithAlpha()
            contactDetailsViewModel.currContact?.let {
                when (currContact.status) {
                    RequestStatus.SEND_FAIL.value -> {
                        Timber.v("Resending request auth channel...")
                        contactsViewModel.updateAndRequestAuthChannel(currContact.marshaled!!)
                        if (preferences.areInAppNotificationsOn) {
                            (requireActivity() as MainActivity).createSnackMessage("Sending a new request")
                        }
                    }
                    RequestStatus.CONFIRM_FAIL.value -> {
                        Timber.v("Resending confirm auth channel...")
                        contactsViewModel.confirmAuthenticatedChannel(currContact)
                        if (preferences.areInAppNotificationsOn) {
                            (requireActivity() as MainActivity).createSnackMessage("Sending a new confirmation")
                        }
                    }
                    else -> {

                    }
                }
            }
        }

        toolbarGenericBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        contactDetailsSendMessageBtn.setOnClickListener {
            val bundle = bundleOf("contact_id" to contactId)
            navController.navigateSafe(R.id.action_global_chat, bundle)
        }

        contactSendMessageInfoButton.setOnClickListener {
            showInfoDialog(
                R.string.send_message_info_dialog_title,
                R.string.send_message_info_dialog_body,
                mapOf(
                    getString(R.string.send_message_info_dialog_link_text)
                            to getString(R.string.send_message_info_dialog_link_url)
                )
            )
        }

        contactProfileClearChat.setOnClickListener {
            clearChat()
        }

        contactProfileDeleteBtn.setOnClickListener {
            deleteContact()
        }
    }

    protected fun clearData() {
        contactDetailsViewModel.currContact = null
        contactDetailsName.text = null
        clearPhoto(contactDetailsPhoto)
        contactDetailsPhotoDefault.visibility = View.VISIBLE
    }

    private fun clearChat() {
        PopupActionBottomDialogFragment.getInstance(
            titleText = "Are you sure you want to delete?",
            subtitleText = "This will clear all chat messages only for you. It can\'t be undone",
            positiveBtnText = "Yes, Delete",
            negativeBtnText = "No, Don't Delete",
            onClickPositive = {
                contactDetailsViewModel.deleteContactChat(currContact)
            },
            onClickNegative = { },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "chatsMenuDeleteDialog")
    }

    protected open fun deleteContact() {
        PopupActionBottomDialogFragment.getInstance(
            titleText = "Are you sure you want to delete this contact?",
            subtitleText = "This will be removing this contact and all associated conversations. It can\'t be undone.",
            positiveBtnText = "Yes, Delete",
            negativeBtnText = "No, Don't Delete",
            onClickPositive = {
                contactDetailsViewModel.deleteContact(currContact)
            },
            onClickNegative = { },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "deleteMenuDeleteDialog")
    }

    protected fun setProfileRequestLayoutVisibility(contactStatus: Int) {
        if (contactStatus == RequestStatus.SEND_FAIL.value
            || contactStatus == RequestStatus.CONFIRM_FAIL.value
        ) {
            contactDetailsNotificationLayout.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars())
            contactDetailsNotificationLayout.visibility = View.VISIBLE
            contactDetailsNotificationLayout.backgroundTintList =
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.accent_danger
                )
            contactDetailsNotificationText.text = "Your request failed to send."

        } else if (contactStatus == RequestStatus.SENT.value) {
            contactDetailsNotificationLayout.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars())
            contactDetailsNotificationLayout.visibility = View.VISIBLE
            contactDetailsNotificationLayout.backgroundTintList =
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.neutral_off_white
                )
            contactDetailsNotificationIcon.setTint(R.color.neutral_weak)
            contactDetailsNotificationText.text = "Pending"
            contactDetailsNotificationText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.neutral_body
                )
            )
            contactDetailsNotificationBtn.visibility = View.GONE
        } else {
            contactDetailsNotificationLayout.visibility = View.GONE
        }

        if (contactStatus != RequestStatus.ACCEPTED.value) {
            contactDetailsBottomButtonsLayout.visibility = View.GONE
        } else {
            contactDetailsBottomButtonsLayout.visibility = View.VISIBLE
            contactDetailsBottomButtonsLayout.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars())
        }
    }

    protected fun setContactData(contactBindings: ContactWrapperBase) {
        setData(contactBindings)
        setPhoto()
        setNames(contactBindings)
    }

    private fun setData(contactBindings: ContactWrapperBase) {
        val username = contactBindings.getUsernameFact()
        val email = contactBindings.getEmailFact()
        val phone = contactBindings.getPhoneFact()

        if (username.isNotBlank()) {
            contactDetailsUsername.text = username
        } else {
            contactDetailsUsernameHeader.visibility = View.GONE
            contactDetailsUsername.visibility = View.GONE
        }

        if (email != null && email.isNotBlank()) {
            contactDetailsEmail.text = email
        } else {
            contactDetailsEmailHeader.visibility = View.GONE
            contactDetailsEmail.visibility = View.GONE
        }

        if (phone != null && phone.isNotBlank()) {
            contactDetailsPhone.text = phone
        } else {
            contactDetailsPhoneHeader.visibility = View.GONE
            contactDetailsPhone.visibility = View.GONE
        }
    }

    private fun setPhoto() {
        clearPhoto(contactDetailsPhoto)
        contactDetailsPhotoDefault.visibility = View.VISIBLE
    }

    private fun setNames(contactBindings: ContactWrapperBase) {
        contactDetailsName.text = contactBindings.getDisplayName()
    }

    protected fun setContactData(contact: ContactData) {
        setData(contact)
        setPhoto(contact)
        setNames(contact)
    }

    private fun setData(contact: ContactData) {
        val username = contact.username
        val email = contact.email
        val phone = contact.phone

        if (username.isNotBlank()) {
            contactDetailsUsername.text = username
        } else {
            contactDetailsUsernameHeader.visibility = View.GONE
            contactDetailsUsername.visibility = View.GONE
        }

        if (email.isNotBlank()) {
            contactDetailsEmail.text = email
        } else {
            contactDetailsEmailHeader.visibility = View.GONE
            contactDetailsEmail.visibility = View.GONE
        }

        if (phone.isNotBlank()) {
            contactDetailsPhone.text = phone
        } else {
            contactDetailsPhoneHeader.visibility = View.GONE
            contactDetailsPhone.visibility = View.GONE
        }
    }

    private fun setPhoto(contact: ContactData) {
        if (contact.photo != null) {
            contactDetailsPhotoDefault.visibility = View.INVISIBLE
            val photo = bitmapArrayToBitmap(contact.photo!!)

            loadPhoto(contactDetailsPhoto, photo, true)
        } else {
            clearPhoto(contactDetailsPhoto)
            contactDetailsPhotoDefault.visibility = View.VISIBLE
        }
    }

    private fun setNames(contact: ContactData) {
        contactDetailsName.text = contact.displayName
    }

    override fun changeContactPhoto(photo: Bitmap) {
        contactDetailsPhotoDefault.visibility = View.INVISIBLE
        val resizedBitmap = getResizedBitmap(photo, 1024)
        val byteArrayPhoto = getBitmapArray(resizedBitmap)
        currContact.photo = byteArrayPhoto
        contactDetailsPhoto.visibility = View.VISIBLE

        loadPhoto(contactDetailsPhoto, photo, true)
        onPhotoChanged(byteArrayPhoto)
    }
}
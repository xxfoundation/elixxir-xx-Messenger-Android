package io.xxlabs.messenger.ui.main.contacts.profile

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.ui.dialog.warning.WarningDialogUI
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.ui.base.BaseContactDetailsFragment
import io.xxlabs.messenger.ui.dialog.info.showInfoDialog
import io.xxlabs.messenger.ui.main.MainActivity
import io.xxlabs.messenger.ui.main.contacts.delete.DeleteConnectionDialog
import io.xxlabs.messenger.ui.main.contacts.delete.DeleteConnectionDialogUI
import kotlinx.android.synthetic.main.fragment_contact_details.*

class ContactProfileFragment: BaseContactDetailsFragment() {
    override fun onPhotoChanged(photo: ByteArray) {
        contactDetailsViewModel.setCurrentPhoto(photo)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        watchForObservables()
    }

    override fun toolbarBgColor(): Int = R.color.neutral_body
    override fun toolbarBackButtonColor(): Int = R.color.neutral_white

    override fun onImageNotSelectedOrRevoked() { }

    private fun init() {
        val contactId = arguments?.getByteArray("contact_id")
        if (contactId == null) {
            findNavController().navigateUp()
            return
        } else {
            contactDetailsViewModel.getContactInfo(contactId)
            bindListeners(contactId)
        }

        contactDetailsBottomButtonsLayout.visibility = View.VISIBLE
        contactDetailsBottomAcceptLayout.visibility = View.GONE
    }

    private fun watchForObservables() {
        contactDetailsViewModel.contactData.observe(viewLifecycleOwner, Observer { contact ->
            if (contact == null) {
                clearData()
            } else {
                currContact = contact
                setContactData(contact)
                setProfileRequestLayoutVisibility(contact.status)

                when (contact.nickNameNotSet()) {
                    true -> {
                        contactDetailsNickname.text = getString(R.string.not_provided)
                        contactDetailsAddNickname.visibility = View.VISIBLE
                        contactDetailsEditNickname.visibility = View.GONE
                    }
                    false -> {
                        contactDetailsNickname.text = contact.nickname
                        contactDetailsAddNickname.visibility = View.GONE
                        contactDetailsEditNickname.visibility = View.VISIBLE
                    }
                }
                contactDetailsAddNickname.setOnSingleClickListener { rename() }
                contactDetailsEditNickname.setOnSingleClickListener { rename() }
            }
        })

        contactDetailsViewModel.deletedChat.observe(viewLifecycleOwner, Observer { deletedChat ->
            if (deletedChat is DataRequestState.Success) {
                (requireActivity() as MainActivity).createSnackMessage("Chat was cleared!", true)
                contactDetailsViewModel.deletedChat.postValue(DataRequestState.Completed())
            } else if (deletedChat is DataRequestState.Error) {
                showError("Error while cleaning up the chat. Try again: ${deletedChat.error.localizedMessage}")
            }
        })

        contactDetailsViewModel.deletedContact.observe(viewLifecycleOwner, Observer { deletedChat ->
            if (deletedChat is DataRequestState.Success) {
                (requireActivity() as MainActivity).createSnackMessage("Contact was removed!", true)
                contactDetailsViewModel.deletedContact.postValue(DataRequestState.Completed())
                navController.navigateUp()
            } else if (deletedChat is DataRequestState.Error) {
                showError("Error while cleaning up the chat. Try again: ${deletedChat.error.localizedMessage}")
            }
        })
    }

    private fun ContactData.nickNameNotSet(): Boolean =
        nickname.isEmpty() || nickname == username

    private fun rename() {
        PopupActionBottomDialogFragment.getInstance(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_contact_rounded),
            titleText = "Add Nickname",
            positiveBtnText = "Save",
            positiveBtnColor = R.color.brand_dark,
            isInputDialog = true,
            defaultInput = currContact.displayName,
            isIncognito = preferences.isIncognitoKeyboardEnabled,
            textValidation = {
                return@getInstance it.length > 3
            },
            onClickPositive = { name ->
                currContact.nickname = name
                contactDetailsViewModel.updateName(name)
            }, isCancellable = true
        ).show(childFragmentManager, "createNameDialog")
    }

    override fun deleteContact() {
        showDeleteConnectionDialog()
    }

    private fun showDeleteConnectionDialog() {
        val infoDialogUI = InfoDialogUI.create(
            getString(R.string.confirm_delete_connection_dialog_title),
            getString(R.string.confirm_delete_connection_dialog_body, currContact.displayName),
            null
        ) { }
        val confirmDialogUI = WarningDialogUI.create(
            infoDialogUI,
            getString(R.string.confirm_delete_connection_dialog_button),
            ::onDeleteConnection
        )
        val deleteConnectionUI = DeleteConnectionDialogUI.create(
            confirmDialogUI,
            ::onDeleteConnectionInfoClicked
        )
        DeleteConnectionDialog.newInstance(deleteConnectionUI)
            .show(requireActivity().supportFragmentManager, null)
    }

    private fun onDeleteConnection() {
        contactDetailsViewModel.deleteContact(currContact)
    }

    private fun onDeleteConnectionInfoClicked() {
        showInfoDialog(
            R.string.deleting_connection_info_dialog_title,
            R.string.deleting_connection_info_dialog_body,
            null
        )
    }
}
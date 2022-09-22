package io.xxlabs.messenger.ui.main.contacts.invitation

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.view.LooperCircularProgressBar
import io.xxlabs.messenger.ui.base.BaseContactDetailsFragment
import kotlinx.android.synthetic.main.fragment_contact_details.*
import timber.log.Timber

class ContactInvitation : BaseContactDetailsFragment() {
    lateinit var progress: LooperCircularProgressBar
    override fun onPhotoChanged(photo: ByteArray) {
        contactDetailsViewModel.setCurrentPhoto(photo)
    }

    override fun toolbarBgColor(): Int = R.color.neutral_body
    override fun toolbarBackButtonColor(): Int = R.color.neutral_white

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress = LooperCircularProgressBar(requireContext(), false)
        init()
        watchForObservers()
    }

    override fun onImageNotSelectedOrRevoked() {

    }

    fun init() {
        val contactId = arguments?.getByteArray("contact_id")
        if (contactId == null) {
            findNavController().navigateUp()
            return
        } else {
            contactDetailsViewModel.getContactInfo(contactId)
        }
    }

    fun fillClient(contact: ContactData) {
        currContact = contact
        setContactData(contact)
        contactProfileBottomLayout.visibility = View.GONE
        contactDetailsAcceptHelperText.visibility = View.VISIBLE
        contactDetailsBottomButtonsLayout.visibility = View.GONE
        contactDetailsKeyContainer.visibility = View.GONE
        contactDetailsNotificationLayout.visibility = View.GONE
        contactDetailsTopButtonsLayout.visibility = View.GONE
        contactDetailsUsernameHeader.visibility = View.GONE
        contactDetailsUsername.visibility = View.GONE

        contactDetailsBtnAccept.setOnSingleClickListener {
            createName(contact)
        }

        contactDetailsBtnReject.setOnSingleClickListener {
            contactsViewModel.rejectContact(contact)
        }
    }

    private fun watchForObservers() {
        contactDetailsViewModel.contactData.observe(viewLifecycleOwner, { contact ->
            if (contact != null) {
                fillClient(contact)
            }
        })

        contactsViewModel.newConfirmRequestSent.observe(viewLifecycleOwner, Observer { result ->
            Timber.v("Trying to send  new confirm request")
            when (result) {
                is DataRequestState.Start -> {
                    progress.show()
                }
                is DataRequestState.Error -> {
                    showError(result.error, isBindingError = true)
                    completeInvitation()
                }

                is DataRequestState.Success -> {
                    Handler(Looper.getMainLooper()).postDelayed({
                        completeInvitation()
                        findNavController().navigate(R.id.action_contact_invitation_pop)
                    }, 1000)
                }
                else -> {
                    progress.hide()
                }
            }
        })
    }

    private fun createName(contact: ContactData) {
        PopupActionBottomDialogFragment.getInstance(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_contact_rounded),
            titleText = "Create a Contact",
            positiveBtnText = "Save Contact",
            positiveBtnColor = R.color.brand_dark,
            isInputDialog = true,
            defaultInput = contact.username,
            isIncognito = preferences.isIncognitoKeyboardEnabled,
            textValidation = {
                return@getInstance it.length > 3
            },
            onClickPositive = { name ->
                contactDetailsViewModel.updateName(name)
                contactsViewModel.confirmAuthenticatedChannel(contact)
            }, isCancellable = true
        ).show(childFragmentManager, "createNameDialog")
    }

    private fun completeInvitation() {
        contactsViewModel.newConfirmRequestSent.postValue(DataRequestState.Completed())
    }
}
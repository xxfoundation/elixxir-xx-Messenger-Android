package io.xxlabs.messenger.ui.main.contacts.success

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperMock
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.util.DialogUtils
import io.xxlabs.messenger.support.view.LooperCircularProgressBar
import io.xxlabs.messenger.ui.base.BaseContactDetailsFragment
import kotlinx.android.synthetic.main.fragment_contact_details.*
import timber.log.Timber

class ContactSuccessFragment : BaseContactDetailsFragment() {
    lateinit var progress: LooperCircularProgressBar
    override fun onPhotoChanged(photo: ByteArray) {
        currContact.photo = photo
    }

    override fun toolbarBgColor(): Int = R.color.neutral_body
    override fun toolbarBackButtonColor(): Int = R.color.neutral_white

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progress = LooperCircularProgressBar(requireContext(), false)
        init()
        watchForObservers()
    }

    override fun onImageNotSelectedOrRevoked() {

    }

    fun init() {
        arguments?.getString("contact").also { rawByte ->
            if (rawByte != null) {
                val contactWrapper =
                    contactDetailsViewModel.generateContact(rawByte.toByteArray(Charsets.ISO_8859_1))
                Timber.d("Scanner contact facts: ${contactWrapper?.getStringifiedFacts()}")
                fillClient(contactWrapper!!)
            } else {
                findNavController().navigateUp()
                return
            }
        }
    }

    fun fillClient(contactWrapper: ContactWrapperBase) {
        setContactData(contactWrapper)
        contactProfileBottomLayout.visibility = View.GONE
        contactDetailsUsernameHeader.visibility = View.GONE
        contactDetailsUsername.visibility = View.GONE
        contactDetailsAcceptHelperText.visibility = View.VISIBLE
        contactDetailsBottomButtonsLayout.visibility = View.GONE
        contactDetailsKeyContainer.visibility = View.GONE
        contactDetailsNotificationLayout.visibility = View.GONE
        contactDetailsTopButtonsLayout.visibility = View.GONE
        contactDetailsBtnReject.visibility = View.GONE
        contactDetailsAcceptText.text = "Request Contact"
        contactDetailsBtnAccept.text = "Request"

        currContact = ContactData()
        currContact.marshaled = contactWrapper.marshal()
        currContact.userId = contactWrapper.getId()
        currContact.username = contactWrapper.getUsernameFact()
        currContact.nickname = contactWrapper.getNameFact() ?: ""
        currContact.email = contactWrapper.getEmailFact().orEmpty()
        currContact.phone = contactWrapper.getPhoneFact().orEmpty()
        if (isMockVersion()) {
            contactWrapper as ContactWrapperMock
            currContact.status = contactWrapper.contact.status
        }

        contactDetailsBtnAccept.setOnSingleClickListener {
            Timber.v("Facts from user: ${contactWrapper.getStringifiedFacts()}")
            if (contactDetailsViewModel.doesUserExist(currContact.userId).id != -1L) {
                showError("The contact has already been added or there is already an active request.")
            } else {
                createName(currContact)
            }
        }
    }

    private fun watchForObservers() {
        contactDetailsViewModel.searchState.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is DataRequestState.Start -> {
                    progress.show()
                }
                is DataRequestState.Error -> {
                    progress.hide()
                    contactDetailsViewModel.searchState.postValue(DataRequestState.Completed())
                    showError(result.error, isBindingError = true)
                }

                is DataRequestState.Success -> {
                    contactsViewModel. updateAndRequestAuthChannel(result.data)
                }
                else -> {
                    progress.hide()
                }
            }
        })

        contactsViewModel.newAuthRequestSent.observe(viewLifecycleOwner, { result ->
            when (result) {
                is SimpleRequestState.Error -> {
                    progress.hide()
                    contactDetailsViewModel.searchState.postValue(DataRequestState.Completed())
                    result.error?.let { showError(it, isBindingError = true) }
                }

                is SimpleRequestState.Success -> {
                    contactDetailsViewModel.searchState.postValue(DataRequestState.Completed())
                    onRequestSuccess()
                }
                else -> {
                    progress.hide()
                }
            }
        })
    }

    private fun onRequestSuccess() {
        DialogUtils.createPopupDialogFragment(
            icon = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_check_circle
            ),
            titleText = "Success",
            subtitleText = "Your request has been successfully sent!",
            positiveBtnText = "View Requests",
            positiveBtnColor = R.color.brand_dark,
            negativeBtnText = "Continue Adding",
            onClickPositive = {
                val bundle = bundleOf("selectedTab" to 1)
                navController.navigateSafe(R.id.action_contact_view_requests, bundle)
            },
            onClickNegative = {
                navController.navigateUp()
            }
        ).show(childFragmentManager, "contactSuccessDialog")
    }

    private fun createName(contact: ContactData) {
        PopupActionBottomDialogFragment.getInstance(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_contact_rounded),
            titleText = "Create a Contact",
            positiveBtnText = "Save Contact",
            positiveBtnColor = R.color.brand_dark,
            textValidation = {
                return@getInstance it.length > 3
            },
            defaultInput = contact.username,
            isInputDialog = true,
            isIncognito = preferences.isIncognitoKeyboardEnabled,
            onClickPositive = { name ->
                currContact.nickname = name
                contactDetailsViewModel.searchForContact(currContact, contact.marshaled!!)
            }, isCancellable = true
        ).show(childFragmentManager, "createNameDialog")
    }
}
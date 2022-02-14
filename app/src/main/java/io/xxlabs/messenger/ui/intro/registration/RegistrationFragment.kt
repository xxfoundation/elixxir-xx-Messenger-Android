package io.xxlabs.messenger.ui.intro.registration

import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.datatype.input.RegistrationInputState
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.ui.base.BaseProfileRegistrationFragment
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.MainViewModel
import io.xxlabs.messenger.ui.main.ud.modal.ProfileInputPopupDialog
import kotlinx.android.synthetic.main.fragment_register.*
import timber.log.Timber


/**
 * Fragment representing the registration screen for xx.
 */
class RegistrationFragment : BaseProfileRegistrationFragment(true) {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var step: RegistrationStep

    private var lastFieldFocus: View? = null
    private var lastClickTime: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun changeContactPhoto(photo: Bitmap) {

    }

    override fun onImageNotSelectedOrRevoked() {

    }

    override fun onUdLoaded() {

    }

    override fun onShowDialog(dialogFragment: DialogFragment) {
        val dialog = dialogFragment as ProfileInputPopupDialog
        if (isRegistration) {
            dialog.btnBack.visibility = View.GONE
        }
    }

    override fun onRemoveFact(factToDelete: FactType) {

    }

    override fun onEnterCode() {
        currentInputPopupDialog?.btnBack?.visibility = View.VISIBLE
        currentInputPopupDialog?.btnBack?.setOnClickListener {
            if (preferences.registrationStep == RegistrationStep.REGISTRATION_EMAIL_FILL.value) {
                currentInputPopupDialog?.dismissAllowingStateLoss()
                createPhoneDialog()
            } else if (preferences.registrationStep == RegistrationStep.REGISTRATION_ACCOUNT_CREATED.value) {
                currentInputPopupDialog?.dismissAllowingStateLoss()
                createEmailDialog()
            }
        }
    }

    override fun onEmailSuccess() {
        preferences.registrationStep = RegistrationStep.REGISTRATION_EMAIL_FILL.value
        dismissDialogAfterSuccess()
        regProfileSuccessLayout.visibility = View.VISIBLE
        regProfileSuccessTitle.text = "Successfully added your email!"
        regProfileSuccessSubtitle.text =
            "Your email address:\n${registrationViewModel.getStoredMail()}"
        regProfileSuccessBtn.setOnClickListener {
            createPhoneDialog()
        }
    }

    override fun onPhoneSuccess() {
        preferences.registrationStep = RegistrationStep.REGISTRATION_PHONE_FILL.value
        dismissDialogAfterSuccess()
        regProfileSuccessTitle.text = "Successfully added your phone!"
        regProfileSuccessSubtitle.text = "Your phone:\n${registrationViewModel.getStoredPhone()}"
        regProfileSuccessLayout.visibility = View.VISIBLE
        regProfileSuccessBtn.setOnClickListener {
            navigateMain()
        }
    }

    override fun onSkipEmail() {
        preferences.registrationStep = RegistrationStep.REGISTRATION_EMAIL_FILL.value
        step = RegistrationStep.REGISTRATION_EMAIL_FILL
        createPhoneDialog()
    }

    override fun onSkipPhone() {
        preferences.registrationStep = RegistrationStep.REGISTRATION_PHONE_FILL.value
        step = RegistrationStep.REGISTRATION_PHONE_FILL
        navigateMain()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        registrationViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(RegistrationViewModel::class.java)

        mainViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(MainViewModel::class.java)

        networkViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(NetworkViewModel::class.java)

        initComponents(view)
    }

    override fun initComponents(root: View) {
        step = RegistrationStep.REGISTRATION_USERNAME
        regFirstBtn.setOnClickListener(firstBtnListener)
        regFirstBtn.disable()
        verifyStep()
        addTextValidation()
        watchObservables()
        regTitle.addKeyboardInsetListener { isKeyboardVisible ->
            if (isKeyboardVisible) {
                regTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    setMargins(
                        regTitle.marginLeft,
                        Utils.dpToPx(30),
                        regTitle.marginRight,
                        regTitle.marginBottom
                    )
                }
            } else {
                regTitle.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    setMargins(
                        regTitle.marginLeft,
                        Utils.dpToPx(50),
                        regTitle.marginRight,
                        regTitle.marginBottom
                    )
                }
            }
        }
    }

    private fun verifyStep() {
        val registrationStep = RegistrationStep.from(preferences.registrationStep)
        if (registrationStep.value > 1) {
            nameStepClear(registrationStep)
        }
    }

    private fun addTextValidation() {
        regInput.onFocusChangeListener = View.OnFocusChangeListener { view, isFocused ->
            if (isFocused) {
                lastFieldFocus = view
                showMessage()
            } else {
                if (lastFieldFocus == regInput) {
                    verifyField()
                }
            }
        }

        regInput.afterTextChanged {
            verifyField()
        }

        regInput.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                if (regFirstBtn.isEnabled) {
                    firstBtnListener.onClick(view)
                }
                return@setOnEditorActionListener true
            }
            false
        }

        regInput.incognito(preferences.isIncognitoKeyboardEnabled)
    }

    private val firstBtnListener = View.OnClickListener {
        when (step) {
            RegistrationStep.REGISTRATION_USERNAME -> {
                if (SystemClock.elapsedRealtime() - lastClickTime >= 1000) {
                    mainViewModel.showSpinner.value = DataRequestState.Start()
                    registrationViewModel.registerUsername(regInput.text.toString())
                }
            }

            RegistrationStep.REGISTRATION_ACCOUNT_CREATED -> {
                navigateMain()
            }
            else -> {
            }
        }
    }

    private fun nameStepClear(registrationStep: RegistrationStep) {
        snackBar.dismiss()
        regLogo.visibility = View.GONE
        regTitle.visibility = View.GONE
        regSubTitle.visibility = View.GONE
        regInputLayoutHolder.visibility = View.GONE
        regInputHelperText.visibility = View.GONE
        regFirstBtn.visibility = View.GONE
        regLogo.removeKeyboardInsetListener()

        if (registrationStep.value > step.value) {
            step = registrationStep
        }

        when (step) {
            RegistrationStep.REGISTRATION_ACCOUNT_CREATED -> {
                createEmailDialog()
            }
            RegistrationStep.REGISTRATION_EMAIL_FILL -> {
                createPhoneDialog()
            }
            RegistrationStep.REGISTRATION_PHONE_FILL -> {
                navigateMain()
            }
            else -> {
            }
        }
    }

    private fun showMessage() {
        when (step) {
            RegistrationStep.REGISTRATION_USERNAME -> {
                registrationViewModel.setRegistrationState(RegistrationInputState.NAME_MESSAGE)
            }
            RegistrationStep.REGISTRATION_ACCOUNT_CREATED -> {
                registrationViewModel.setRegistrationState(RegistrationInputState.REGISTRATION_ACCOUNT_CREATED)
            }
            else -> {
            }
        }
    }

    private fun verifyField() {
        when (step) {
            RegistrationStep.REGISTRATION_USERNAME -> {
                registrationViewModel.isNameValid(regInputLayout)
            }
            RegistrationStep.REGISTRATION_ACCOUNT_CREATED -> {
                registrationViewModel.setRegistrationState(RegistrationInputState.REGISTRATION_ACCOUNT_CREATED)
            }
            else -> {
            }
        }
    }

    private fun watchObservables() {
        registrationViewModel.registrationState.observe(viewLifecycleOwner, { state ->
            handleInputState(state)
        })

        registrationViewModel.usernameRequestState.observe(viewLifecycleOwner, { state ->
            handleUsernameRequestState(state)
        })

        mainViewModel.showSpinner.observe(viewLifecycleOwner, { show ->
            if (show is DataRequestState.Start) {
                loading.show()
                Timber.v("show spinner #ud")
            } else {
                loading.dismiss()
                Timber.v("finished spinner #ud")
            }
        })
    }

    private fun handleInputState(inputState: RegistrationInputState) {
        when (inputState) {
            RegistrationInputState.NONE -> {
                hideNotification()
            }

            RegistrationInputState.NAME_MESSAGE -> {
                hideNotification()
//                regInputLayout.helperText =
//                    getString(R.string.registration_username_must_be_char)
            }

            RegistrationInputState.EMPTY -> {
                regFirstBtn.disable()
                setNotificationError()
                regInputHelperText.text =
                    getString(R.string.registration_username_cannot_be_empty)
            }

            RegistrationInputState.NAME_INVALID_LENGTH -> {
                regFirstBtn.disable()
                setNotificationError()
                regInputHelperText.text =
                    getString(R.string.registration_username_must_be_char)
            }

            RegistrationInputState.NAME_INVALID_CHARACTERS -> {
                regFirstBtn.disable()
                setNotificationError()
                regInputHelperText.text =
                    getString(R.string.registration_username_invalid_char)
            }

            RegistrationInputState.NAME_MAX_CHARACTERS -> {
                regFirstBtn.disable()
                setNotificationError()
                regInputHelperText.text =
                    getString(R.string.registration_username_max_char)
            }

            RegistrationInputState.NAME_VALID -> {
                regFirstBtn.enable()
                setNotificationOk()
                regInputHelperText.text =
                    getString(R.string.registration_character_requirement_met)
            }

            RegistrationInputState.REGISTRATION_ACCOUNT_CREATED -> {
                regInputLayout.visibility = View.GONE
                hideNotification()
                regFirstBtn.enable()
            }
        }
    }

    private fun setNotificationError() {
        regInputIcon.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_danger
            )
        )
    }

    private fun setNotificationOk() {
        regInputIcon.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_check_green
            )
        )
    }

    private fun hideNotification() {
        regInputIcon.setImageDrawable(null)
    }

    private fun handleUsernameRequestState(state: SimpleRequestState<Boolean>) {
        when (state) {
            is SimpleRequestState.Success -> {
                preferences.nextRegistrationPhase()
                nameStepClear(RegistrationStep.REGISTRATION_ACCOUNT_CREATED)
                completeRequest()
            }
            is SimpleRequestState.Error -> {
                val err = state.error!!
                when {
                    err.localizedMessage?.contains("already taken") == true -> {
                        showUsernameIsAlreadyTaken()
                    }
                    err.localizedMessage?.contains("lateinit property") == true -> {
                        showError("Could not connect to xx network. Please, verify your connection and try again.")
                    }
                    else -> {
                        showError(err, isBindingError = true)
                    }
                }
                completeRequest()
            }
            is SimpleRequestState.Completed -> {
                clearState()
            }
        }
    }

    private fun completeRequest() {
        registrationViewModel.completeUsernameState()
    }

    private fun clearState() {
        registrationViewModel.lastErrMsg = ""
        mainViewModel.showSpinner.value = DataRequestState.Completed()
    }

    private fun showUsernameIsAlreadyTaken() {
        mainViewModel.showSpinner.value = DataRequestState.Completed()
        setNotificationError()
        regInputHelperText.text =
            getString(R.string.registration_username_already_taken)
    }

    override fun onDetach() {
        if (::registrationViewModel.isInitialized) {
            registrationViewModel.clearFields()
        }
        super.onDetach()
    }

    internal enum class RegistrationStep constructor(val value: Int) {
        REGISTRATION_USERNAME(0),
        REGISTRATION_ACCOUNT_CREATED(1),
        REGISTRATION_EMAIL_FILL(2),
        REGISTRATION_PHONE_FILL(3);

        companion object {
            fun from(value: Int) = values().first {
                when {
                    value > 3 -> {
                        it.value == REGISTRATION_PHONE_FILL.value
                    }
                    value < 0 -> {
                        it.value == REGISTRATION_USERNAME.value
                    }
                    else -> {
                        it.value == value
                    }
                }
            }
        }
    }
}

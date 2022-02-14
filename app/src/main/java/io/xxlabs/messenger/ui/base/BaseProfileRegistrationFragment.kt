package io.xxlabs.messenger.ui.base

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.datatype.input.EmailFieldState
import io.xxlabs.messenger.data.datatype.input.PhoneFieldState
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.util.ValidationUtils
import io.xxlabs.messenger.support.view.LooperCircularProgressBar
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.countrycode.CountryFullscreenDialog
import io.xxlabs.messenger.ui.main.countrycode.CountrySelectionListener
import io.xxlabs.messenger.ui.main.ud.modal.ProfileInputPopupDialog
import io.xxlabs.messenger.ui.main.ud.registration.UdRegistrationViewModel
import timber.log.Timber
import javax.inject.Inject

abstract class BaseProfileRegistrationFragment(val isRegistration: Boolean = false) :
    BasePhotoFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected lateinit var navController: NavController
    protected lateinit var networkViewModel: NetworkViewModel
    protected lateinit var udProfileRegistrationViewModel: UdRegistrationViewModel
    protected lateinit var snackBar: Snackbar
    private var resendCountdown: CountDownTimer? = null
    protected lateinit var loading: LooperCircularProgressBar
    protected var currentInputPopupDialog: ProfileInputPopupDialog? = null
    protected var currentFact: String = ""
    protected var isCurrentDialogEmail: Boolean = false

    protected var countryFullscreenDialog: CountryFullscreenDialog = createPhoneFullscreenDialog()
    private lateinit var phoneCurrentDialCode: String
    private lateinit var phoneCurrentCountryCode: String
    private lateinit var phoneCurrentCountryFlag: String

    abstract fun onUdLoaded()
    abstract fun onShowDialog(dialogFragment: DialogFragment)
    abstract fun onRemoveFact(factToDelete: FactType)
    abstract fun onEnterCode()
    abstract fun onEmailSuccess()
    abstract fun onPhoneSuccess()
    abstract fun onSkipEmail()
    abstract fun onSkipPhone()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()

        networkViewModel = ViewModelProvider(requireActivity(), viewModelFactory)
            .get(NetworkViewModel::class.java)


        udProfileRegistrationViewModel = ViewModelProvider(this, viewModelFactory)
            .get(UdRegistrationViewModel::class.java)


        loading = LooperCircularProgressBar(requireContext(), false)
        snackBar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            "Waiting for User Discovery...",
            Snackbar.LENGTH_INDEFINITE,
        ).setAction("Dismiss") {
            snackBar.dismiss()
        }
        snackBar.view.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars())
        if (!networkViewModel.isUserDiscoveryRunning()) {
            snackBar.show()
        }
        createPhoneFullscreenDialog()
        watchForChanges()
    }

    override fun onDetach() {
        snackBar.dismiss()
        super.onDetach()
    }

    protected fun addEmail() {
        if (isConnected()) {
            createEmailDialog()
        } else {
            showNetworkError()
        }

    }

    protected fun addPhone() {
        if (isConnected()) {
            createPhoneDialog()
        } else {
            showNetworkError()
        }
    }

    private fun showNetworkError() {
        showError("You cannot add / remove this information while User Discovery is not connected.")
    }

    private fun watchForChanges() {
        networkViewModel.userDiscoveryStatus.observe(viewLifecycleOwner, { isUdInitiated ->
            if (!isUdInitiated) {
                snackBar.dismiss()
                showUdError()
            } else {
                snackBar.dismiss()
                onUdLoaded()
            }
        })

        udProfileRegistrationViewModel.emailRegistrationState.observe(
            viewLifecycleOwner,
            { result ->
                onEmailRegistered(result)
            })

        udProfileRegistrationViewModel.emailCode.observe(viewLifecycleOwner, { result ->
            if (result == true) {
                udProfileRegistrationViewModel.emailCode.postValue(null)
                registerEmailSuccess()
            } else if (result == false) {
                registerEmailFailed()
            }
        })

        udProfileRegistrationViewModel.phoneRegistrationState.observe(
            viewLifecycleOwner,
            { result ->
                onPhoneRegistered(result)
            })

        udProfileRegistrationViewModel.phoneCode.observe(viewLifecycleOwner, { result ->
            if (result == true) {
                udProfileRegistrationViewModel.emailCode.postValue(null)
                registerPhoneSuccess()
            } else if (result == false) {
                registerPhoneFailed()
            }
        })

        udProfileRegistrationViewModel.codeLoading.observe(viewLifecycleOwner, { state ->
            when (state) {
                is DataRequestState.Start -> {
                    loading.show()
                }
                is DataRequestState.Success -> {
                    udProfileRegistrationViewModel.codeLoading.value = DataRequestState.Completed()
                }

                is DataRequestState.Error -> {
                    udProfileRegistrationViewModel.codeLoading.value = DataRequestState.Completed()
                }

                else -> {
                    loading.hide()
                }
            }
        })
    }

    protected fun showUdError() {
        showError("Error to initialize User Discovery, try again when the network is healthy.")
    }

    private fun isConnected() =
        networkViewModel.hasConnection() && networkViewModel.isUserDiscoveryRunning()

    protected fun showDelete(factToDelete: FactType) {
        val deletion = when (factToDelete) {
            FactType.EMAIL -> {
                "email address"
            }
            FactType.PHONE -> {
                "phone number"
            }
            else -> {
                ""
            }
        }

        PopupActionBottomDialogFragment.getInstance(
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_alert_rounded),
            titleText = "Are you sure you want delete your $deletion?",
            subtitleText = "This action cannot be undone.",
            positiveBtnText = "Yes, Delete",
            negativeBtnText = "No",
            onClickPositive = {
                onRemoveFact(factToDelete)
            },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "profileDeleteFact")
    }

    protected fun onEmailRegistered(result: DataRequestState<String>?) {
        when (result) {
            is DataRequestState.Start -> {
                loading.show()
            }
            is DataRequestState.Success -> {
                requestVerificationCode(result.data, currentFact, true)
                finishEmailRequest()
            }

            is DataRequestState.Error -> {
                showError(result.error, isBindingError = true)
                finishEmailRequest()
            }

            else -> {
                loading.hide()
            }
        }
    }

    protected fun createEmailDialog() {
        val emailDialog = ProfileInputPopupDialog.getInstance(
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_mail_circle),
            titleText = "Add email",
            subtitleText = if (isRegistration) {
                SpannableStringBuilder("You can always add/remove it in your profile")
            } else {
                SpannableStringBuilder()
            },
            inputHint = "Email Address",
            textValidation = { email, _ ->
                ValidationUtils.isEmailValid(email) == EmailFieldState.VALID
            },
            positiveBtnText = "Ok",
            skipBtnText = if (isRegistration) {
                "Skip"
            } else {
                ""
            },
            onClickPositiveBtn = { dialog, view, email ->
                currentFact = email
                checkEmail(email)
            }, onClickNegativeBtn = { dialog, _, _ ->
                onSkipEmail()
            }, onShowListener = {
                onShowDialog(it)
            }, beforeDismiss = {
                resendCountdown?.cancel()
            },
            showCancelBtn = !isRegistration,
            isRegistration = isRegistration,
            isIncognito = preferences.isIncognitoKeyboardEnabled,
            isCancelable = false
        )

        isCurrentDialogEmail = true
        currentInputPopupDialog = emailDialog
        currentInputPopupDialog?.show(childFragmentManager, "emailRegistrationDialog")
    }

    private fun checkEmail(email: String) {
        udProfileRegistrationViewModel.registerEmail(email)
    }

    private fun requestVerificationCode(confirmationId: String, fact: String, isEmail: Boolean) {
        val cleanFact: String = if (isEmail) {
            fact
        } else {
            phoneCurrentDialCode + fact.substring(0, fact.length - 2)
        }

        val subtitleText =
            SpannableStringBuilder("Enter the code we just sent to\n").append(cleanFact)
        subtitleText.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.brand_dark
                )
            ),
            subtitleText.length - cleanFact.length,
            subtitleText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        currentInputPopupDialog?.title?.visibility = View.VISIBLE
        currentInputPopupDialog?.subtitle?.visibility = View.VISIBLE
        currentInputPopupDialog?.toolbarTitle?.visibility = View.GONE
        currentInputPopupDialog?.toolbarLine?.visibility = View.GONE
        (currentInputPopupDialog?.icon?.layoutParams as ViewGroup.MarginLayoutParams?)?.setMargins(
            0,
            0,
            0,
            0
        )

        resendCountdown = createResendCountdown(currentInputPopupDialog?.secondary)
        currentInputPopupDialog?.hidePhone()
        currentInputPopupDialog?.setIcon(
            if (isEmail) R.drawable.ic_mail_circle
            else R.drawable.ic_phone_circle
        )
        currentInputPopupDialog?.title?.text = "Enter Code"
        currentInputPopupDialog?.subtitle?.text = subtitleText
        currentInputPopupDialog?.secondary?.text = "Resend code"
        currentInputPopupDialog?.secondary?.visibility = View.VISIBLE
        currentInputPopupDialog?.setHint("Code")
        currentInputPopupDialog?.inputLayout?.gravity = Gravity.CENTER_HORIZONTAL
        currentInputPopupDialog?.input?.text = null
        currentInputPopupDialog?.btnPositive?.text = "Finish"
        currentInputPopupDialog?.btnSkip?.visibility = View.GONE
        currentInputPopupDialog?.setValidation { code, _ ->
            !code.isNullOrBlank()
        }

        currentInputPopupDialog?.setOnClick(ProfileInputPopupDialog.OnClickType.SECONDARY_TEXT) { _, _, _ ->
            if (isEmail) {
                checkEmail(currentFact)
            } else {
                checkPhone(currentFact)
            }
        }

        currentInputPopupDialog?.setOnClick(ProfileInputPopupDialog.OnClickType.POSITIVE) { _, _, inputText ->
            currentInputPopupDialog?.inputLayout?.error = null
            udProfileRegistrationViewModel.confirmFact(confirmationId, inputText, fact, isEmail)
        }

        onEnterCode()
    }

    private fun createResendCountdown(textView: TextView?): CountDownTimer {
        textView?.disable()
        return object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = (millisUntilFinished / 1000).toInt()
                context?.let {
                    textView?.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.neutral_weak
                        )
                    )
                }
                val text = SpannableStringBuilder("Resend code ").append("($remainingSeconds secs)")
                textView?.text = text
                textView?.contentDescription = "ud.profile.dialog.resend.counting"
                Timber.v("[VERIFICATION CODE]Remaining seconds: $remainingSeconds")
            }

            override fun onFinish() {
                context?.let {
                    textView?.setTextColor(
                        ContextCompat.getColor(
                            it,
                            R.color.brand_dark
                        )
                    )
                }
                textView?.contentDescription = "ud.profile.dialog.resend.ready"
                textView?.text = "Resend Code"
                textView?.enable()
            }
        }.start()
    }

    protected fun registerEmailSuccess() {
        onEmailSuccess()
    }

    protected fun registerEmailFailed() {
        currentInputPopupDialog?.inputLayout?.error = "Invalid Verification Code"
    }

    private fun finishEmailRequest() {
        udProfileRegistrationViewModel.emailRegistrationState.value = DataRequestState.Completed()
    }

    protected fun onPhoneRegistered(result: DataRequestState<String>?) {
        when (result) {
            is DataRequestState.Start -> {
                loading.show()
            }
            is DataRequestState.Success -> {
                requestVerificationCode(result.data, currentFact, false)
                finishPhoneRequest()
            }

            is DataRequestState.Error -> {
                showError(result.error, isBindingError = true)
                finishPhoneRequest()
            }

            else -> {
                loading.hide()
            }
        }
    }

    private fun createPhoneFullscreenDialog(): CountryFullscreenDialog {
        setDefaultCountry()
        return CountryFullscreenDialog.getInstance(object : CountrySelectionListener {
            override fun onItemSelected(country: Country) {
                val countryString = country.flag + country.dialCode
                phoneCurrentCountryCode = country.countryCode
                phoneCurrentDialCode = country.dialCode
                phoneCurrentCountryFlag = country.flag
                countryFullscreenDialog.dismiss()
                currentInputPopupDialog?.phoneCodeInput?.setText(countryString)
            }
        })
    }

    private fun setDefaultCountry() {
        val usa = Country.getDefaultCountry()
        phoneCurrentCountryCode = usa.countryCode
        phoneCurrentDialCode = usa.dialCode
        phoneCurrentCountryFlag = usa.flag
    }

    protected fun createPhoneDialog() {
        val phoneDialog = ProfileInputPopupDialog.getInstance(
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_phone_circle),
            titleText = "Add Phone",
            subtitleText = if (isRegistration) {
                SpannableStringBuilder("You can always add/remove it in your profile.")
            } else {
                SpannableStringBuilder()
            },
            inputHint = "Phone Number",
            isInputPhone = true,
            textValidation = { phone, countryCode ->
                Timber.d("Phone $phone, Code +${countryCode?.substringAfter("+")}")
                ValidationUtils.isPhoneValid(
                    "+${countryCode?.substringAfter("+")}$phone",
                    phoneCurrentCountryCode
                ) == PhoneFieldState.VALID
            },
            positiveBtnText = "Ok",
            skipBtnText = if (isRegistration) {
                "Skip"
            } else {
                ""
            },
            onClickPositiveBtn = { window, view, phone ->
                val filteredPhone = phone + phoneCurrentCountryCode
                Timber.v("Filtered phone: $filteredPhone")
                currentFact = filteredPhone
                checkPhone(currentFact)
            },
            onClickNegativeBtn = { _, _, _ ->
                onSkipPhone()
            },
            onShowListener = {
                val countryStringDefault = phoneCurrentCountryFlag + phoneCurrentDialCode
                currentInputPopupDialog?.activatePhone()
                currentInputPopupDialog?.phoneCodeInput?.setText(countryStringDefault)
                val dialog = (it as ProfileInputPopupDialog)
                dialog.phoneCodeInput.setOnSingleClickListener {
                    countryFullscreenDialog.show(childFragmentManager, "fullscreenCountryDialog")
                }
            }, beforeDismiss = {
                setDefaultCountry()
                resendCountdown?.cancel()
            }, showCancelBtn = !isRegistration,
            isRegistration = isRegistration,
            isIncognito = preferences.isIncognitoKeyboardEnabled,
            isCancelable = false
        )

        isCurrentDialogEmail = false
        currentInputPopupDialog = phoneDialog
        currentInputPopupDialog?.show(childFragmentManager, "emailRegistrationDialog")
    }

    private fun checkPhone(phone: String) {
        udProfileRegistrationViewModel.registerPhone(phone)
    }

    protected fun registerPhoneSuccess() {
        onPhoneSuccess()
    }

    fun navigateMain() {
        findNavController().navigateSafe(R.id.action_global_chats)
    }

    fun dismissDialogAfterSuccess() {
        resendCountdown?.cancel()
        currentInputPopupDialog?.dismiss()
    }

    protected fun registerPhoneFailed() {
        currentInputPopupDialog?.inputLayout?.error = "Invalid Verification Code"
    }

    private fun finishPhoneRequest() {
        udProfileRegistrationViewModel.phoneRegistrationState.value = DataRequestState.Completed()
    }
}
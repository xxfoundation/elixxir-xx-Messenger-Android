package io.xxlabs.messenger.ui.main.ud.modal

import android.app.Dialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputLayout
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.util.Utils
import kotlinx.android.synthetic.main.fragment_register.*


class ProfileInputPopupDialog(
    private val iconDrawable: Drawable? = null,
    private val toolbarTitleText: String?,
    private val titleText: String,
    private val subtitleText: SpannableStringBuilder,
    private val secondaryText: String,
    private val inputHint: String,
    private val isInputPassword: Boolean,
    private val isInputPhone: Boolean,
    private val textValidation: ((String, String?) -> Boolean)?,
    private val positiveBtnText: String?,
    private val skipBtnText: String?,
    private val onClickPositiveBtn: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = { _, _, _ -> },
    private val onClickNegativeBtn: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = { _, _, _ -> },
    private val onClickTextSecondary: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = { _, _, _ -> },
    private val onClickCancel: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = { _, _, _ -> },
    private val onShowListener: ((window: DialogFragment) -> Unit)?,
    private val beforeDismiss: (() -> Unit)?,
    private val showCancelBtn: Boolean,
    private val isRegistration: Boolean,
    private val isIncognito: Boolean,
    setIsCancelable: Boolean
) : DialogFragment() {
    lateinit var root: View
    lateinit var toolbarTitle: TextView
    lateinit var toolbarLine: View
    lateinit var icon: ImageView
    lateinit var title: TextView
    lateinit var subtitle: TextView
    lateinit var secondary: TextView
    lateinit var inputLayout: TextInputLayout
    lateinit var input: EditText
    lateinit var btnPositive: Button
    lateinit var btnSkip: TextView
    lateinit var btnBack: ImageView
    lateinit var phoneCodeInput: EditText
    lateinit var hintTitle: TextView

    init {
        isCancelable = setIsCancelable
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.XxFullscreenDialog)
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        root =
            inflater.inflate(R.layout.component_ud_profile_registration_dialog, container, false)
        return root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            dialog.window?.enterTransition = null
            WindowCompat.setDecorFitsSystemWindows(dialog.window!!, false)
            onShowListener?.let { it1 -> it1(this) }
        }
        return dialog
    }

    private fun bindText() {
        if (isRegistration) {
            title.text = titleText
            toolbarLine.visibility = View.GONE
            btnSkip.visibility = View.VISIBLE
        } else {
            toolbarTitle.text = titleText
            title.visibility = View.GONE
            subtitle.visibility = View.GONE
        }

        subtitle.text = subtitleText

        if (showCancelBtn) {
            btnBack.visibility = View.VISIBLE
        } else {
            btnBack.visibility = View.GONE
        }

        input.setOnEditorActionListener { view, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    if (isRegistration) {
                        if (btnPositive.isEnabled) {
                            btnPositive.callOnClick()
                        }
                    } else {
                        Utils.hideKeyboardGlobal(requireContext(), input)
                    }
                    true
                }
                else -> false
            }
        }

        when {
            isInputPassword -> {
                input.inputType =
                    (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
                input.setTextAppearance(R.style.InputEditText)
                input.incognito(isIncognito)
            }
            isInputPhone -> {
                input.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_PHONE)
                input.setTextAppearance(R.style.InputEditText)
                input.incognito(isIncognito)
            }
            else -> {
                input.inputType =
                    (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                input.setTextAppearance(R.style.InputEditText)
                input.incognito(isIncognito)
            }
        }

        if (positiveBtnText.isNullOrEmpty()) {
            btnPositive.visibility = View.GONE
        } else {
            btnPositive.text = positiveBtnText
        }

        if (skipBtnText.isNullOrEmpty()) {
            btnSkip.visibility = View.GONE
        } else {
            btnSkip.text = skipBtnText
        }

        if (secondaryText.isEmpty()) {
            secondary.visibility = View.GONE
        } else {
            secondary.text = secondaryText
        }

        setHint(inputHint)
        input.incognito(isIncognito)
    }

    private fun bindListeners() {
        btnPositive.disableWithAlpha()
        setOnClick(OnClickType.POSITIVE, false, onClickPositiveBtn)
        setOnClick(OnClickType.SKIP, true, onClickNegativeBtn)
        setOnClick(OnClickType.BACK, true, onClickCancel)
        setOnClick(OnClickType.SECONDARY_TEXT, false, onClickTextSecondary)

        setValidation(textValidation)
    }

    fun setOnClick(
        type: OnClickType,
        dismiss: Boolean = false,
        onClickCancel: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = null
    ) {
        val btn = when (type) {
            OnClickType.POSITIVE -> btnPositive
            OnClickType.SKIP -> btnSkip
            OnClickType.BACK -> btnBack
            OnClickType.SECONDARY_TEXT -> secondary
        }

        btn.setOnSingleClickListener {
            if (dismiss) {
                dismiss()
            }
            onClickCancel?.let { function -> function(this, it, input.text.toString()) }
        }
    }

    fun setHint(inputHint: String) {
        hintTitle.text = inputHint
        input.hint = inputHint
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icon = view.findViewById(R.id.profileRegistrationDialogIcon)
        title = view.findViewById(R.id.profileRegistrationDialogTitle)
        subtitle = view.findViewById(R.id.profileRegistrationDialogSubtitle)
        secondary = view.findViewById(R.id.profileRegistrationResendBtn)
        inputLayout = view.findViewById(R.id.profileRegistrationFactInputLayout)
        input = view.findViewById(R.id.profileRegistrationFactInput)
        btnPositive = view.findViewById(R.id.profileRegistrationDialogPositiveBtn)
        btnSkip = view.findViewById(R.id.toolbarGenericActionText)
        btnBack = view.findViewById(R.id.toolbarGenericBackBtn)

        btnSkip.text = "Skip"
        toolbarTitle = view.findViewById(R.id.toolbarGenericTitle)
        toolbarTitle.text = toolbarTitleText
        val toolbar = view.findViewById<ViewGroup>(R.id.toolbarGeneric)
        toolbarLine = toolbar.findViewById(R.id.toolbarGenericLine)

        toolbar.setInsets(topMask = WindowInsetsCompat.Type.systemBars())

        hintTitle = view.findViewById(R.id.profileRegistrationFactInputHint)
        phoneCodeInput = view.findViewById(R.id.profileRegistrationPhoneCode)
        icon.setImageDrawable(iconDrawable)

        toolbar.setOnClickListener {
            Utils.hideKeyboardGlobal(requireContext(), input)
        }

        val rootLayout = view.findViewById<ViewGroup>(R.id.profileRegistrationDialogRoot)
        rootLayout.setOnClickListener {
            Utils.hideKeyboardGlobal(requireContext(), input)
        }

        if (isRegistration) {
            icon.addKeyboardInsetListener { isKeyboardVisible ->
                if (isKeyboardVisible) {
                    icon.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        setMargins(icon.marginLeft, 0, icon.marginRight, icon.marginBottom)
                    }
                } else {
                    icon.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        setMargins(
                            icon.marginLeft,
                            Utils.dpToPx(40),
                            icon.marginRight,
                            icon.marginBottom
                        )
                    }
                }
            }

            inputLayout.addKeyboardInsetListener { isKeyboardVisible ->
                if (isKeyboardVisible) {
                    inputLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        setMargins(
                            inputLayout.marginLeft,
                            Utils.dpToPx(40),
                            inputLayout.marginRight,
                            inputLayout.marginBottom
                        )
                    }
                } else {
                    inputLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        setMargins(
                            inputLayout.marginLeft,
                            Utils.dpToPx(64),
                            inputLayout.marginRight,
                            inputLayout.marginBottom
                        )
                    }
                }
            }
        }
        bindText()
        bindListeners()
    }

    fun hideInputBtn() {
        inputLayout.visibility = View.GONE
        btnPositive.visibility = View.INVISIBLE
        btnBack.visibility = View.INVISIBLE
        secondary.visibility = View.GONE
        btnSkip.visibility = View.GONE
    }

    fun setIcon(newIcon: Int) {
        icon.setImageDrawable(ContextCompat.getDrawable(requireContext(), newIcon))
        icon.visibility = View.VISIBLE
    }

    fun setValidation(validation: ((String, String?) -> Boolean)?) {
        if (isInputPhone) {
            phoneCodeInput.afterTextChanged {
                validate(validation, input.text.toString(), it)
            }
        }

        input.afterTextChanged {
            validate(validation, it, phoneCodeInput.text.toString())
        }
    }

    private fun validate(validation: ((String, String?) -> Boolean)?, it: String, it2: String) {
        if (isInputPhone) {
            if (phoneCodeInput.text.isNotEmpty() && validation?.invoke(it, it2) == true) {
                btnPositive.enable()
            } else {
                btnPositive.disable()
            }
        } else {
            if (validation?.invoke(it, it2) == true) {
                btnPositive.enable()
            } else {
                btnPositive.disable()
            }
        }
    }

    fun hidePhone() {
        phoneCodeInput.visibility = View.GONE
    }

    fun activatePhone() {
        hintTitle.text = "Phone Number"
        phoneCodeInput.visibility = View.VISIBLE
    }


    override fun dismiss() {
        beforeDismiss?.invoke()
        super.dismissAllowingStateLoss()
    }

    enum class OnClickType {
        POSITIVE,
        SKIP,
        BACK,
        SECONDARY_TEXT
    }

    companion object {
        fun getInstance(
            icon: Drawable? = null,
            toolbarTitleText: String? = null,
            titleText: String,
            subtitleText: SpannableStringBuilder = SpannableStringBuilder(null),
            secondaryText: String = "",
            inputHint: String = "",
            isInputPassword: Boolean = false,
            isInputPhone: Boolean = false,
            textValidation: ((String, String?) -> Boolean)? = { _, _ -> true },
            positiveBtnText: String? = null,
            skipBtnText: String? = null,
            onClickPositiveBtn: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = { _, _, _ -> },
            onClickNegativeBtn: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = { _, _, _ -> },
            onClickTextSecondary: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = { _, _, _ -> },
            onClickCancel: ((window: DialogFragment, view: View, inputText: String) -> Unit)? = { _, _, _ -> },
            onShowListener: ((window: DialogFragment) -> Unit)? = null,
            beforeDismiss: (() -> Unit)? = null,
            showCancelBtn: Boolean,
            isRegistration: Boolean,
            isIncognito: Boolean = false,
            isCancelable: Boolean = true
        ): ProfileInputPopupDialog {
            return ProfileInputPopupDialog(
                icon,
                toolbarTitleText,
                titleText,
                subtitleText,
                secondaryText,
                inputHint,
                isInputPassword,
                isInputPhone,
                textValidation,
                positiveBtnText,
                skipBtnText,
                onClickPositiveBtn,
                onClickNegativeBtn,
                onClickTextSecondary,
                onClickCancel,
                onShowListener,
                beforeDismiss,
                showCancelBtn,
                isRegistration,
                isIncognito,
                isCancelable
            )
        }
    }
}
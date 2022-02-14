package io.xxlabs.messenger.support.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.disableWithAlpha
import io.xxlabs.messenger.support.extensions.enable
import io.xxlabs.messenger.support.extensions.incognito
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.misc.DebugLogger
import timber.log.Timber

class PopupActionBottomDialogFragment(
    private val iconDrawable: Drawable? = null,
    private val titleText: String,
    private val subtitleText: String,
    private val positiveBtnText: String?,
    private val negativeBtnText: String?,
    private val onClickPositive: ((String) -> Unit)? = { },
    private val onClickNegative: (() -> Unit)? = { },
    private val positiveBtnColor: Int,
    private val textValidation: ((String) -> Boolean)? = null,
    private val inputError: String? = null,
    private val hintTitle: String? = null,
    private val hint: String? = null,
    private val defaultInput: String? = null,
    private val isInputDialog: Boolean = false,
    isCancellableDialog: Boolean = false,
    private val exportLogs: Boolean = false,
    private val isIncognito: Boolean = false,
    private val onDismissedByUser: (() -> Unit) = { }
) : BottomSheetDialogFragment() {
    lateinit var root: View
    lateinit var icon: ImageView
    lateinit var title: TextView
    lateinit var subtitle: TextView
    lateinit var btnPositive: Button
    lateinit var btnNegative: Button
    lateinit var input: EditText
    lateinit var inputHint: TextView
    lateinit var inputLayout: TextInputLayout

    private var dismissedByUser = true

    var isBrokenDialog = false
    lateinit var mBehavior: BottomSheetBehavior<FrameLayout>
    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(sheet: View, p1: Float) {}

        override fun onStateChanged(sheet: View, state: Int) {
            Timber.v("ON STATE CHANGED $state")
            when (state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    Timber.v("COLLAPSED")
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    Timber.v("DRAGGING")
                    mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                BottomSheetBehavior.STATE_SETTLING -> {
                    Timber.v("SETTLING")
                }

                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.XxBottomSheetDialog)
    }

    constructor() : this(
        null,
        "",
        "",
        null,
        null,
        null,
        null,
        R.color.accent_danger,
        null,
        null,
        null,
        null,
        null,
        true,
        false
    ) {
        isBrokenDialog = true
    }


    init {
        isCancelable = isCancellableDialog
    }

    private fun setBehavior() {
        mBehavior = BottomSheetBehavior.from(root.parent as FrameLayout)
        mBehavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        mBehavior.isHideable = isCancelable
        mBehavior.peekHeight = 0
        mBehavior.skipCollapsed = true
        root.parent.requestLayout()
        mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.component_action_dialog_bottom, container)
        root.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime())
        return root
    }

    private fun initComponents() {
//        if (iconDrawable != null) {
//            icon.setImageDrawable(iconDrawable)
//            icon.visibility = View.VISIBLE
//        }

        if (exportLogs) {
            icon.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_download
                )
            )
            icon.visibility = View.VISIBLE
            icon.setOnClickListener {
                dismiss()
                DebugLogger.exportLatestLog(requireContext())
            }
        }

        bindText()
        bindListeners()

        if (isInputDialog) {
            inputLayout.visibility = View.VISIBLE
            inputHint.visibility = View.VISIBLE
            btnPositive.disableWithAlpha()
            inputHint.text = hintTitle
            input.hint = hint
            input.doAfterTextChanged {
                if (it.isNullOrBlank() || (textValidation?.invoke(it.toString()) == false)) {
                    inputLayout.helperText = inputError
                    btnPositive.disableWithAlpha()
                } else {
                    inputLayout.helperText = " "
                    btnPositive.enable()
                }
            }
            defaultInput?.apply {
                input.setText(this)
            }
        } else {
            inputLayout.visibility = View.GONE
            inputHint.visibility = View.GONE
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            if (isBrokenDialog) {
                dismissAllowingStateLoss()
                return@setOnShowListener
            }

            dialog.window!!.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            dialog.window!!.setGravity(Gravity.BOTTOM)
        }
        return dialog
    }

    private fun bindText() {
        title.text = titleText
        subtitle.text = subtitleText

        if (positiveBtnText.isNullOrEmpty()) {
            btnPositive.visibility = View.GONE
        } else {
            btnPositive.text = positiveBtnText
            btnPositive.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), positiveBtnColor)
        }

        if (negativeBtnText.isNullOrEmpty()) {
            btnNegative.visibility = View.GONE
        } else {
            btnNegative.visibility = View.VISIBLE
            btnNegative.text = negativeBtnText
        }
    }

    private fun bindListeners() {
        btnPositive.setOnClickListener {
            dismissedByUser = false
            dismiss()
            onClickPositive?.invoke(input.text.toString().trim())
        }

        btnNegative.setOnClickListener {
            dismissedByUser = false
            dismiss()
            onClickNegative?.invoke()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (dismissedByUser) onDismissedByUser()
        super.onDismiss(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        icon = view.findViewById(R.id.actionDialogIcon)
        title = view.findViewById(R.id.actionDialogTitle)
        subtitle = view.findViewById(R.id.actionDialogSubtitle)
        btnPositive = view.findViewById(R.id.actionDialogPositiveBtn)
        btnPositive = view.findViewById(R.id.actionDialogPositiveBtn)
        btnNegative = view.findViewById(R.id.actionDialogNegativeBtn)
        input = view.findViewById(R.id.actionDialogInput)
        inputHint = view.findViewById(R.id.actionDialogInputHint)
        inputLayout = view.findViewById(R.id.actionDialogInputLayout)
        input.incognito(isIncognito)

        setBehavior()
        initComponents()
    }

    companion object {
        fun getInstance(
            icon: Drawable? = null,
            titleText: String,
            subtitleText: String = "",
            positiveBtnText: String? = null,
            negativeBtnText: String? = null,
            onClickPositive: ((String) -> Unit)? = { },
            onClickNegative: (() -> Unit)? = { },
            positiveBtnColor: Int = R.color.accent_danger,
            textValidation: ((String) -> Boolean)? = null,
            inputError: String? = "name must be at least 4 characters",
            hintTitle: String? = "Contact Nickname",
            hint: String? = "Nickname",
            defaultInput: String? = null,
            isInputDialog: Boolean = false,
            isCancellable: Boolean = false,
            exportLogs: Boolean = false,
            isIncognito: Boolean,
            onDismissedByUser: () -> Unit = { }
        ): PopupActionBottomDialogFragment {
            return PopupActionBottomDialogFragment(
                icon,
                titleText,
                subtitleText,
                positiveBtnText,
                negativeBtnText,
                onClickPositive,
                onClickNegative,
                positiveBtnColor,
                textValidation,
                inputError,
                hintTitle,
                hint,
                defaultInput,
                isInputDialog,
                isCancellable,
                exportLogs,
                isIncognito,
                onDismissedByUser
            )
        }
    }
}
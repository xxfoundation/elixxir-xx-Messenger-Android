package io.xxlabs.messenger.support.dialog

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.misc.DebugLogger
import timber.log.Timber

class PopupActionBottomDialog(
    context: Context,
    private val iconDrawable: Drawable? = null,
    private val titleText: String,
    private val subtitleText: String,
    private val positiveBtnText: String?,
    private val negativeBtnText: String?,
    private val onClickPositive: ((String, String?) -> Unit)? = { _, _ -> },
    private val onClickNegative: (() -> Unit)? = { },
    private val positiveBtnColor: Int,
    private val textValidation: ((String, String?) -> Boolean)? = null,
    private val isInputDialog: Boolean = false,
    private val isSecondInputDialog: Boolean = false,
    private val exportLogs: Boolean = false,
    private val isIncognito: Boolean = false,
    private val isCancellable: Boolean = false
) : BottomSheetDialog(context, R.style.XxBottomSheetDialog) {
    var root: ViewGroup? = null
    var icon: ImageView? = null
    var title: TextView? = null
    var subtitle: TextView? = null
    var btnPositive: Button? = null
    var btnNegative: Button? = null
    var input: EditText? = null
    var inputHint: TextView? = null
    var inputLayout: TextInputLayout? = null
    var secondInput: EditText? = null
    var secondInputHint: TextView? = null
    var secondInputLayout: TextInputLayout? = null

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
        setCancelable(isCancellable)
        setContentView(R.layout.component_action_dialog_bottom)
        window!!.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window!!.setGravity(Gravity.BOTTOM)
        bindViews()
    }

    private fun setBehavior() {
        root?.let { root ->
            mBehavior = BottomSheetBehavior.from(root.parent as FrameLayout)
            mBehavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
            mBehavior.isHideable = false
            mBehavior.peekHeight = 0
            mBehavior.skipCollapsed = true
            mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            root.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime())
        }
    }

    private fun initComponents() {
//        if (iconDrawable != null) {
//            icon?.setImageDrawable(iconDrawable)
//            icon?.visibility = View.VISIBLE
//        }
        if (exportLogs) {
            icon?.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_download
                )
            )
            icon?.visibility = View.VISIBLE
            icon?.setOnSingleClickListener() {
                dismiss()
                DebugLogger.exportLatestLog(context)
            }
        }

        bindText()
        bindListeners()

        if (isInputDialog) {
            if (isSecondInputDialog) {
                inputLayout?.visibility = View.VISIBLE
                inputHint?.visibility = View.VISIBLE
                secondInputLayout?.visibility = View.VISIBLE
                secondInputHint?.visibility = View.VISIBLE

                inputLayout?.helperText = null
                secondInputLayout?.helperText = null
                inputHint?.text = null
                secondInputHint?.text = null
//                input?.setDrawableStart(R.drawable.ic_menu_contacts)
//                secondInput?.setDrawableStart(R.drawable.ic_bubble)
                inputLayout?.startIconDrawable = null
                secondInputLayout?.startIconDrawable = null

                input?.hint = context.getString(R.string.group_create_dialog_name_hint)
                secondInput?.hint = context.getString(R.string.group_create_dialog_description_hint)

                btnPositive?.disableWithAlpha()

                input?.doAfterTextChanged {
                    if (it.isNullOrBlank()) {
                        btnPositive?.disableWithAlpha()
                        inputLayout?.error = "Cannot be empty."
                    } else if (textValidation?.invoke(it.toString(), secondInput?.text.toString()) == false) {
                        btnPositive?.disableWithAlpha()
                        inputLayout?.error = "Names are maximum by 20 characters or 256 bytes."
                    } else {
                        inputLayout?.helperText = " "
                        inputLayout?.error = null
                        btnPositive?.enable()
                    }
                }
            } else {
                inputLayout?.startIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_contact_18)
                inputLayout?.visibility = View.VISIBLE
                inputHint?.visibility = View.VISIBLE
                btnPositive?.disableWithAlpha()
                input?.doAfterTextChanged {
                    if (it.isNullOrBlank() || (textValidation?.invoke(
                            it.toString(),
                            null
                        ) == false)
                    ) {
                        inputLayout?.helperText = "name must be at least 4 characters"
                        btnPositive?.disableWithAlpha()
                    } else {
                        inputLayout?.helperText = " "
                        btnPositive?.enable()
                    }
                }
            }
        } else {
            inputLayout?.visibility = View.GONE
            inputHint?.visibility = View.GONE
        }
    }

    private fun bindText() {
        title?.text = titleText
        subtitle?.text = subtitleText

        if (positiveBtnText.isNullOrEmpty()) {
            btnPositive?.visibility = View.GONE
        } else {
            btnPositive?.text = positiveBtnText
            btnPositive?.backgroundTintList =
                ContextCompat.getColorStateList(context, positiveBtnColor)
        }

        if (negativeBtnText.isNullOrEmpty()) {
            btnNegative?.visibility = View.GONE
        } else {
            btnNegative?.visibility = View.VISIBLE
            btnNegative?.text = negativeBtnText
        }
    }

    private fun bindListeners() {
        btnPositive?.setOnSingleClickListener {
            dismiss()
            onClickPositive?.let { function ->
                function(input?.text.toString().trim(), secondInput?.text.toString().trim())
            }
        }

        btnNegative?.setOnSingleClickListener {
            dismiss()
            onClickNegative?.let { function -> function() }
        }
    }

    fun bindViews() {
        root = findViewById(R.id.actionDialogRoot)
        icon = findViewById(R.id.actionDialogIcon)
        title = findViewById(R.id.actionDialogTitle)
        subtitle = findViewById(R.id.actionDialogSubtitle)
        btnPositive = findViewById(R.id.actionDialogPositiveBtn)
        btnNegative = findViewById(R.id.actionDialogNegativeBtn)
        input = findViewById(R.id.actionDialogInput)
        inputHint = findViewById(R.id.actionDialogInputHint)
        inputLayout = findViewById(R.id.actionDialogInputLayout)
        secondInput = findViewById(R.id.actionDialogSecondInput)
        secondInputHint = findViewById(R.id.actionDialogSecondInputHint)
        secondInputLayout = findViewById(R.id.actionDialogSecondInputLayout)

        input?.incognito(isIncognito)
        secondInput?.incognito(isIncognito)

        setBehavior()
        initComponents()
    }

    companion object {
        fun getInstance(
            context: Context,
            icon: Drawable? = null,
            titleText: String,
            subtitleText: String = "",
            positiveBtnText: String? = null,
            negativeBtnText: String? = null,
            onClickPositive: ((String, String?) -> Unit)? = { _, _ -> },
            onClickNegative: (() -> Unit)? = { },
            positiveBtnColor: Int = R.color.accent_danger,
            textValidation: ((String, String?) -> Boolean)? = null,
            isInputDialog: Boolean = false,
            isSecondInputDialog: Boolean = false,
            isCancellable: Boolean = false,
            exportLogs: Boolean = false,
            isIncognito: Boolean
        ): PopupActionBottomDialog {
            return PopupActionBottomDialog(
                context,
                icon,
                titleText,
                subtitleText,
                positiveBtnText,
                negativeBtnText,
                onClickPositive,
                onClickNegative,
                positiveBtnColor,
                textValidation,
                isInputDialog,
                isSecondInputDialog,
                exportLogs,
                isIncognito,
                isCancellable
            )
        }
    }
}
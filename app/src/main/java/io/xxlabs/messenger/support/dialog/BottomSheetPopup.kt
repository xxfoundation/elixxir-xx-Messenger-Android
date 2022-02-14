package io.xxlabs.messenger.support.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.setInsets
import timber.log.Timber

class BottomSheetPopup constructor(
    context: Context,
    val iconInt: Int = R.drawable.ic_alert_rounded,
    val descriptionText: String? = null,
    val primaryBtnTitle: String? = null,
    val primaryBtnCallback: () -> Unit = { },
    val primaryCallbackDismiss: Boolean = false,
    val secondaryBtnTitle: String? = null,
    val secondaryBtnCallback: () -> Unit = { },
    val secondaryCallbackDismiss: Boolean = false,
    val neutralBtnTitle: String? = null,
    val neutralBtnCallback: () -> Unit = { },
    val neutralCallbackDismiss: Boolean = false,
    val check: String? = null,
    val checkCallback: (Boolean) -> Unit = { },
    val onCancel: () -> Unit = { },
) : BottomSheetDialog(context, R.style.XxBottomSheetDialog) {
    var root: ViewGroup? = null
    var title: TextView? = null
    var description: TextView? = null
    var icon: ImageView? = null
    var primaryBtn: Button? = null
    var neutralBtn: Button? = null
    var secondaryBtn: Button? = null
    var checkbox: CheckBox? = null
    var dismissCallOnCancel = true
    lateinit var bottomBehavior: BottomSheetBehavior<FrameLayout>

    private val bottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(sheet: View, p1: Float) {}

        override fun onStateChanged(sheet: View, state: Int) {
            Timber.v("ON STATE CHANGED $state")
            when (state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    dismiss()
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    Timber.v("DRAGGING")
                }
                BottomSheetBehavior.STATE_SETTLING -> {
                    Timber.v("SETTLING")
                }

                else -> {
                }
            }
        }
    }

    override fun dismiss() {
        super.dismiss()
        if (dismissCallOnCancel) {
            onCancel()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.component_bottom_sheet_dialog)
        bindViews()
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        window?.setGravity(Gravity.BOTTOM)
    }

    private fun bindViews() {
        root = findViewById(R.id.dialogPopUp)
        description = findViewById(R.id.dialogPopUpDesc)
        icon = findViewById(R.id.dialogPopUpIcon)
        primaryBtn = findViewById(R.id.dialogPopUpBtnPrimary)
        neutralBtn = findViewById(R.id.dialogPopUpBtnNeutral)
        secondaryBtn = findViewById(R.id.dialogPopUpBtnSecondary)
        checkbox = findViewById(R.id.dialogPopUpCheckbox)

        root?.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime())
        setBehavior()
//        bindTitleIcon()
        setBtnVisibility()
        bindListeners()
    }

    private fun bindListeners() {
        primaryBtn?.setOnClickListener {
            if (primaryCallbackDismiss) {
                dismissCallOnCancel = false
                dismiss()
            }

            primaryBtnCallback()
        }

        secondaryBtn?.setOnClickListener {
            if (secondaryCallbackDismiss) {
                dismissCallOnCancel = false
                dismiss()
            }
            secondaryBtnCallback()
        }

        neutralBtn?.setOnClickListener {
            if (neutralCallbackDismiss) {
                dismissCallOnCancel = false
                dismiss()
            }
            neutralBtnCallback()
        }


        checkCallback.let { onCheck ->
            checkbox?.setOnCheckedChangeListener { _, isChecked ->
                onCheck(isChecked)
            }
        }
    }

    private fun setBtnVisibility() {
        primaryBtn?.visibility = if (!primaryBtnTitle.isNullOrEmpty()) {
            primaryBtn?.text = primaryBtnTitle
            View.VISIBLE
        } else {
            View.GONE
        }

        secondaryBtn?.visibility = if (!secondaryBtnTitle.isNullOrEmpty()) {
            secondaryBtn?.text = secondaryBtnTitle
            View.VISIBLE
        } else {
            View.GONE
        }

        neutralBtn?.visibility = if (!neutralBtnTitle.isNullOrEmpty()) {
            neutralBtn?.text = neutralBtnTitle
            View.VISIBLE
        } else {
            View.GONE
        }

        checkbox?.visibility = if (check == null) {
            View.GONE
        } else {
            checkbox?.text = check
            View.VISIBLE
        }

        primaryBtn?.isEnabled = true
        secondaryBtn?.isEnabled = true
        neutralBtn?.isEnabled = true
    }

    private fun bindTitleIcon() {
        description?.text = descriptionText
        if (icon != null) {
            icon?.setImageDrawable(ContextCompat.getDrawable(context, iconInt))
        } else {
            icon?.visibility = View.GONE
        }
    }

    private fun setBehavior() {
        root?.let {
            bottomBehavior = BottomSheetBehavior.from(it.parent as FrameLayout)
            bottomBehavior.addBottomSheetCallback(bottomSheetBehaviorCallback)
            bottomBehavior.isHideable = false
            bottomBehavior.peekHeight = 0
            bottomBehavior.skipCollapsed = true
            bottomBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    companion object {
        fun getInstance(
            context: Context,
            icon: Int = R.drawable.ic_alert_rounded,
            description: String? = null,
            topButtonTitle: String? = null,
            topButtonClick: () -> Unit = { },
            topButtonDismiss: Boolean = false,
            bottomButtonTitle: String? = null,
            bottomButtonClick: () -> Unit = { },
            bottomButtonDismiss: Boolean = false,
            middleButtonTitle: String? = null,
            middleButtonClick: () -> Unit = { },
            middleButtonDismiss: Boolean = false,
            checkBoxTitle: String? = null,
            checkBoxCallback: (Boolean) -> Unit = { },
            onCancel: () -> Unit = { }
        ): BottomSheetPopup {
            return BottomSheetPopup(
                context,
                icon,
                description,
                topButtonTitle,
                topButtonClick,
                topButtonDismiss,
                bottomButtonTitle,
                bottomButtonClick,
                bottomButtonDismiss,
                middleButtonTitle,
                middleButtonClick,
                middleButtonDismiss,
                checkBoxTitle,
                checkBoxCallback,
                onCancel
            )
        }
    }
}
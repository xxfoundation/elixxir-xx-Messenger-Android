package io.xxlabs.messenger.support.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.misc.DebugLogger

class PopupActionDialog(
    context: Context,
    private val iconRes: Int? = null,
    private val titleText: String,
    private val subtitleText: String,
    private val positiveBtnText: String?,
    private val negativeBtnText: String?,
    private val onClickPositive: (() -> Unit)? = { },
    private val onClickNegative: (() -> Unit)? = { },
    private val onDismiss: (() -> Unit)? = { },
    private val isCancellableDialog: Boolean = false,
    private val exportLogs: Boolean = false
) : Dialog(context, R.style.XxDialog) {
    lateinit var root: View
    lateinit var icon: ImageView
    lateinit var title: TextView
    lateinit var subtitle: TextView
    lateinit var btnPositive: Button
    lateinit var btnNegative: Button
    var isBrokenDialog = false
    var executedOnClickBtns = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.component_action_dialog)
        setCancelable(isCancellableDialog)
        bindViews()
    }

    override fun dismiss() {
        super.dismiss()
        if (!executedOnClickBtns) {
            onDismiss?.invoke()
        }
    }

    private fun bindViews() {
        icon = findViewById(R.id.actionDialogIcon)
        title = findViewById(R.id.actionDialogTitle)
        subtitle = findViewById(R.id.actionDialogSubtitle)
        btnPositive = findViewById(R.id.actionDialogPositiveBtn)
        btnNegative = findViewById(R.id.actionDialogNegativeBtn)

        if (isBrokenDialog) {
            dismiss()
            return
        }

        window?.setLayout(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)

        if (iconRes != null) {
            icon.setImageDrawable(ContextCompat.getDrawable(context, iconRes))
            icon.visibility = View.VISIBLE
        }

        if (exportLogs) {
            icon.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.ic_download_24dp
                )
            )
            icon.visibility = View.VISIBLE
            icon.setOnClickListener {
                dismiss()
                DebugLogger.exportLatestLog(context)
            }
        }

        bindText()
        bindListeners()
    }

    private fun bindText() {
        title.text = titleText
        subtitle.text = subtitleText

        if (positiveBtnText.isNullOrEmpty()) {
            btnPositive.visibility = View.GONE
        } else {
            btnPositive.text = positiveBtnText
            btnNegative.visibility = View.VISIBLE
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
            executedOnClickBtns = true
            dismiss()
            onClickPositive?.let { function -> function() }
        }

        btnNegative.setOnClickListener {
            executedOnClickBtns = true
            dismiss()
            onClickNegative?.let { function -> function() }
        }
    }

    companion object {
        fun getInstance(
            context: Context,
            icon: Int? = null,
            titleText: String,
            subtitleText: String = "",
            positiveBtnText: String? = null,
            negativeBtnText: String? = null,
            onClickPositive: (() -> Unit)? = { },
            onClickNegative: (() -> Unit)? = { },
            onDismissOnly: (() -> Unit)? = { },
            isCancellable: Boolean = false,
            exportLogs: Boolean = false
        ): PopupActionDialog {
            return PopupActionDialog(
                context,
                icon,
                titleText,
                subtitleText,
                positiveBtnText,
                negativeBtnText,
                onClickPositive,
                onClickNegative,
                onDismissOnly,
                isCancellable,
                false
            )
        }
    }
}
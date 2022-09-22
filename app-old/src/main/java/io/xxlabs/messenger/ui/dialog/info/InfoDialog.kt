package io.xxlabs.messenger.ui.dialog.info

import android.content.DialogInterface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentInfoDialogBinding
import io.xxlabs.messenger.support.view.XxBottomSheetDialog

class InfoDialog : XxBottomSheetDialog() {

    private lateinit var binding: ComponentInfoDialogBinding
    private val dialogUI: InfoDialogUI by lazy {
        requireArguments().getSerializable(ARG_UI) as InfoDialogUI
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_info_dialog,
            container,
            false
        )

        dialogUI.spans?.let {
            binding.infoDialogBody.text = getSpannableBody(dialogUI)
            binding.infoDialogBody.movementMethod = LinkMovementMethod.getInstance()
        } ?: run {
            binding.infoDialogBody.text= dialogUI.body
        }

        binding.ui = dialogUI
        binding.infoDialogButton.setOnClickListener { dismiss() }
        return binding.root
    }

    private fun getSpannableBody(dialogUI: InfoDialogUI): Spannable {
        val builder = SpannableStringBuilder(dialogUI.body)

        dialogUI.spans?.forEach {
            val highlight = requireContext().getColor(it.color)
            val text = it.text
            val startIndex = dialogUI.body.indexOf(text, ignoreCase = true)
            val endIndex = startIndex + text.length

            builder.apply {
                it.url?.let { link ->
                    setSpan(
                        URLSpan(link),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
                setSpan(
                    ForegroundColorSpan(highlight),
                    startIndex,
                    endIndex,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE
                )
            }
        }
        return builder
    }

    override fun onDismiss(dialog: DialogInterface) {
        dialogUI.onDismissed?.invoke()
        super.onDismiss(dialog)
    }

    companion object Factory {
        private const val ARG_UI: String = "ui"

        fun newInstance(dialogUI: InfoDialogUI): InfoDialog =
            InfoDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UI, dialogUI)
                }
            }
    }
}
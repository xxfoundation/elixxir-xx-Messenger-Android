package io.xxlabs.messenger.dialog.info

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
import io.xxlabs.messenger.databinding.ComponentTwoButtonDialogBinding
import io.xxlabs.messenger.dialog.XxBottomSheetDialog
import io.xxlabs.messenger.util.setUiText

/**
 * An info dialog with positive and negative buttons.
 */
class TwoButtonInfoDialog : XxBottomSheetDialog() {

    private lateinit var binding: ComponentTwoButtonDialogBinding
    private val dialogUI: TwoButtonInfoDialogUi by lazy {
        requireArguments().get(ARG_UI) as TwoButtonInfoDialogUi
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_two_button_dialog,
            container,
            false
        )

        dialogUI.spans?.let {
            binding.infoDialogBody.text = getSpannableBody(dialogUI)
            binding.infoDialogBody.movementMethod = LinkMovementMethod.getInstance()
        } ?: run {
            binding.infoDialogBody.setUiText(dialogUI.body)
        }

        initClickListeners()
        binding.ui = dialogUI
        return binding.root
    }

    private fun initClickListeners() {
        binding.infoDialogOkButton.setOnClickListener {
            dialogUI.onPositiveClick()
            dismiss()
        }

        binding.infoDialogCancelButton.setOnClickListener {
            dialogUI.onNegativeClick()
            dismiss()
        }
    }

    private fun getSpannableBody(dialogUi: InfoDialogUi): Spannable {
        val bodyText = dialogUi.body.asString(requireContext())
        val builder = SpannableStringBuilder(bodyText)

        dialogUI.spans?.forEach {
            val highlight = requireContext().getColor(it.color)
            val text = it.text.asString(requireContext())
            val startIndex = bodyText.indexOf(text, ignoreCase = true)
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

        fun newInstance(dialogUi: TwoButtonInfoDialogUi): TwoButtonInfoDialog =
            TwoButtonInfoDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UI, dialogUi)
                }
            }
    }
}
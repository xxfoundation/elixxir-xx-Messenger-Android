package io.elixxir.core.ui.dialog.warning

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentWarningDialogBinding
import io.elixxir.core.ui.dialog.XxBottomSheetDialog

class WarningDialog : XxBottomSheetDialog() {

    private lateinit var binding: ComponentWarningDialogBinding
    private val dialogUi: WarningDialogUi by lazy {
        requireArguments().getSerializable(ARG_UI) as WarningDialogUi
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_warning_dialog,
            container,
            false
        )
        binding.ui = dialogUi
        binding.confirmDialogButton.apply {
            setOnClickListener {
                dialogUi.buttonOnClick()
                dismiss()
            }
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        dialogUi.onDismissed?.invoke()
        super.onDismiss(dialog)
    }

    companion object Factory {
        private const val ARG_UI: String = "ui"

        fun newInstance(dialogUI: WarningDialogUi): WarningDialog =
            WarningDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UI, dialogUI)
                }
            }
    }
}
package io.xxlabs.messenger.ui.dialog.action

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentNewActionDialogBinding
import io.xxlabs.messenger.support.view.XxBottomSheetDialog

class ActionDialog : XxBottomSheetDialog() {

    private lateinit var binding: ComponentNewActionDialogBinding
    private val dialogUI: ActionDialogUI by lazy {
        requireArguments().getSerializable(ARG_UI) as ActionDialogUI
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_new_action_dialog,
            container,
            false
        )
        binding.ui = dialogUI
        binding.actionDialogButton.apply {
            setOnClickListener {
                dialogUI.buttonOnClick()
                dismiss()
            }
        }

        return binding.root
    }

    override fun onDismiss(dialog: DialogInterface) {
        dialogUI.onDismissed?.invoke()
        super.onDismiss(dialog)
    }

    companion object Factory {
        private const val ARG_UI: String = "ui"

        fun newInstance(dialogUI: ActionDialogUI): ActionDialog =
            ActionDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UI, dialogUI)
                }
            }
    }
}
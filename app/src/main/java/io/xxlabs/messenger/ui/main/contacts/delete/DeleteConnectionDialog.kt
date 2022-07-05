package io.xxlabs.messenger.ui.main.contacts.delete

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentDeleteConnectionDialogBinding
import io.xxlabs.messenger.support.view.XxBottomSheetDialog

class DeleteConnectionDialog : XxBottomSheetDialog() {

    private lateinit var binding: ComponentDeleteConnectionDialogBinding
    private val dialogUI by lazy {
        requireArguments().getSerializable(ARG_UI) as DeleteConnectionDialogUI
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_delete_connection_dialog,
            container,
            false
        )
        binding.ui = dialogUI
        binding.confirmDialogButton.apply {
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

        fun newInstance(dialogUI: DeleteConnectionDialogUI): DeleteConnectionDialog =
            DeleteConnectionDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UI, dialogUI)
                }
            }
    }
}
package io.xxlabs.messenger.support.dialog.action

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentNewActionDialogBinding

class ActionDialog(private val dialogUI: ActionDialogUI) : BottomSheetDialogFragment() {

    private lateinit var binding: ComponentNewActionDialogBinding

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

    override fun getTheme(): Int = R.style.RoundedModalBottomSheetDialog

    override fun onDismiss(dialog: DialogInterface) {
        dialogUI.onDismissed?.invoke()
        super.onDismiss(dialog)
    }
}
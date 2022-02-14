package io.xxlabs.messenger.support.dialog.confirm


import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentConfirmDialogBinding

class ConfirmDialog(private val dialogUI: ConfirmDialogUI) : BottomSheetDialogFragment() {

    private lateinit var binding: ComponentConfirmDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_confirm_dialog,
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

    override fun getTheme(): Int = R.style.RoundedModalBottomSheetDialog

    override fun onDismiss(dialog: DialogInterface) {
        dialogUI.onDismissed?.invoke()
        super.onDismiss(dialog)
    }
}
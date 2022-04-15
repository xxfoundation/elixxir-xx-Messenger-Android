package io.xxlabs.messenger.backup.ui.save

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.marginTop
import androidx.core.view.postDelayed
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentRadiobuttonDialogBinding
import kotlinx.coroutines.*

class RadioButtonDialog : BottomSheetDialogFragment() {

    private lateinit var binding: ComponentRadiobuttonDialogBinding
    private val dialogUI: RadioButtonDialogUI by lazy {
        requireArguments().getSerializable(ARG_UI) as RadioButtonDialogUI
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_radiobutton_dialog,
            container,
            false
        )

        binding.ui = dialogUI
        initRadioGroup()
        binding.radioDialogCancelButton.setOnClickListener { dismiss() }
        return binding.root
    }

    private fun initRadioGroup() {
        dialogUI.options.forEach { option ->
            binding.radioDialogRadioGroup.addView(radioButtonFrom(option))
        }
    }

    private fun radioButtonFrom(option: RadioButtonDialogOption): RadioButton {
        val view = layoutInflater.inflate(
            R.layout.component_radio_button,
            binding.radioDialogRadioGroup,
            false
        )
        return view.findViewById<RadioButton>(R.id.radio_button).apply {
            text = option.name
            setOnClickListener {
                option.onClick()
                postDelayed(DISMISS_DELAY_MS) { dismiss() }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun getTheme(): Int = R.style.RoundedModalBottomSheetDialog

    companion object Factory {
        private const val ARG_UI: String = "ui"
        private const val DISMISS_DELAY_MS = 800L

        fun newInstance(dialogUI: RadioButtonDialogUI): RadioButtonDialog =
            RadioButtonDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UI, dialogUI)
                }
            }
    }
}
package io.xxlabs.messenger.requests.ui.accepted.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.Group
import io.xxlabs.messenger.databinding.ComponentRequestAcceptedDialogBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.requests.ui.RequestsViewModel
import javax.inject.Inject

/**
 * UI to navigate to group chat immediately or later, after accepting.
 */
class InvitationAcceptedDialog : BottomSheetDialogFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: ComponentRequestAcceptedDialogBinding

    private val requestsViewModel: RequestsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory }
    )
    private val group: Group by lazy {
        requireArguments().getSerializable(ARG_GROUP) as Group
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ComponentRequestAcceptedDialogBinding.inflate(
            inflater,
            container,
            false
        )
        binding.ui =  requestsViewModel.getInvitationAccepted(group)
        binding.lifecycleOwner = viewLifecycleOwner
        initClickListeners()

        return binding.root
    }

    private fun initClickListeners() {
        binding.closeButtonLayout.closeButton.setOnClickListener {
            binding.ui?.onCloseClicked()
            dismiss()
        }

        binding.dialogButtonLayout.positiveButton.setOnClickListener {
            binding.ui?.onPositiveClick()
            dismiss()
        }

        binding.dialogButtonLayout.negativeButton.setOnClickListener {
            binding.ui?.onNegativeClick()
            dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun getTheme(): Int = R.style.RoundedModalBottomSheetDialog

    companion object {
        private const val ARG_GROUP: String = "group"

        fun newInstance(group: Group): InvitationAcceptedDialog =
            InvitationAcceptedDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_GROUP, group)
                }
            }
    }
}
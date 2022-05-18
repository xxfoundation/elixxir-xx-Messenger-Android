package io.xxlabs.messenger.requests.ui.send

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.databinding.ComponentSendRequestDialogBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.requests.ui.RequestsViewModel
import io.xxlabs.messenger.ui.dialog.ExpandedBottomSheetDialog
import javax.inject.Inject

/**
 * UI to create a [ContactRequest].
 */
class SendRequestDialog : ExpandedBottomSheetDialog(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: ComponentSendRequestDialogBinding

    private val requestsViewModel: RequestsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory }
    )
    private val user: Contact by lazy {
        requireArguments().getSerializable(ARG_USER) as Contact
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ComponentSendRequestDialogBinding.inflate(
            inflater,
            container,
            false
        )
        binding.ui = requestsViewModel.contactRequestTo(user)
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

    companion object {
        private const val ARG_USER: String = "user"

        fun newInstance(user: Contact): SendRequestDialog =
            SendRequestDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_USER, user)
                }
            }
    }
}
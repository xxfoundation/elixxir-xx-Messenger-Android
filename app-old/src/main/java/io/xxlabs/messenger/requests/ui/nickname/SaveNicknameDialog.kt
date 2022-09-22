package io.xxlabs.messenger.requests.ui.nickname

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.databinding.ComponentSendRequestNicknameBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.requests.ui.RequestsViewModel
import io.xxlabs.messenger.requests.ui.send.OutgoingRequest
import io.xxlabs.messenger.ui.dialog.ExpandedBottomSheetDialog
import javax.inject.Inject

class SaveNicknameDialog : ExpandedBottomSheetDialog(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: ComponentSendRequestNicknameBinding

    private val requestsViewModel: RequestsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory }
    )
    private val request: OutgoingRequest by lazy {
        requireArguments().getSerializable(ARG_REQUEST) as OutgoingRequest
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ComponentSendRequestNicknameBinding.inflate(
            inflater,
            container,
            false
        )
        binding.ui = requestsViewModel.getSaveNickname(request)
        binding.lifecycleOwner = viewLifecycleOwner
        initClickListeners()

        return binding.root
    }

    private fun initClickListeners() {
        binding.closeButtonLayout.closeButton.setOnClickListener {
            binding.ui?.onCloseClicked()
            dismiss()
        }

        binding.saveNicknameButton.setOnClickListener {
            binding.ui?.onPositiveClick()
            dismiss()
        }
    }

    companion object {
        private const val ARG_REQUEST: String = "request"

        fun newInstance(request: OutgoingRequest): SaveNicknameDialog =
            SaveNicknameDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_REQUEST, request)
                }
            }
    }
}
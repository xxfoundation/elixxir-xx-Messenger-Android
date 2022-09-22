package io.xxlabs.messenger.requests.ui.details.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentRequestDetailsDialogBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.requests.model.ContactRequest
import io.xxlabs.messenger.requests.ui.RequestsViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI to accept or hide a [ContactRequest].
 */
class RequestDetailsDialog : BottomSheetDialogFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: ComponentRequestDetailsDialogBinding

    private val requestsViewModel: RequestsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory }
    )
    private val request: ContactRequest by lazy {
        requireArguments().getSerializable(ARG_REQUEST) as ContactRequest
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ComponentRequestDetailsDialogBinding.inflate(
            inflater,
            container,
            false
        )

        lifecycleScope.launch {
            requestsViewModel.getRequestDetails(request).collectLatest { ui ->
                ui?.let { binding.ui = it }
            }
        }
        initClickListeners()
        binding.lifecycleOwner = viewLifecycleOwner
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
        private const val ARG_REQUEST: String = "request"

        fun newInstance(request: ContactRequest): RequestDetailsDialog =
            RequestDetailsDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_REQUEST, request)
                }
            }
    }
}
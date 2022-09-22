package io.xxlabs.messenger.requests.ui.details.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentInvitationDetailsDialogBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.requests.model.GroupInvitation
import io.xxlabs.messenger.requests.ui.RequestsViewModel
import io.xxlabs.messenger.requests.ui.details.group.adapter.MembersAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI to accept or hide a [GroupInvitation].
 */
class InvitationDetailsDialog  : BottomSheetDialogFragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var binding: ComponentInvitationDetailsDialogBinding

    private val requestsViewModel: RequestsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory }
    )
    private val invitation: GroupInvitation by lazy {
        requireArguments().getSerializable(ARG_REQUEST) as GroupInvitation
    }

    private val membersAdapter = MembersAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ComponentInvitationDetailsDialogBinding.inflate(
            inflater,
            container,
            false
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                initUI()
            }
        }

        initClickListeners()
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    private suspend fun initUI() {
        requestsViewModel.getInvitationDetails(invitation).onEach { ui ->
            ui?.let {
                binding.ui = it
                initRecyclerView()
            }
        }.launchIn(lifecycleScope)

        requestsViewModel.fetchMembers(invitation).onEach { members ->
            if (members.isNotEmpty())
            membersAdapter.showMembers(members)
        }.launchIn(lifecycleScope)
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

    private fun initRecyclerView() {
        binding.invitationDetailsMembersList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }
    }

    override fun getTheme(): Int = R.style.RoundedModalBottomSheetDialog

    companion object {
        private const val ARG_REQUEST: String = "invitation"

        fun newInstance(request: GroupInvitation): InvitationDetailsDialog =
            InvitationDetailsDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_REQUEST, request)
                }
            }

    }
}
package io.xxlabs.messenger.ui.intro.registration.added

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.databinding.FragmentRegistrationAddedBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.intro.registration.RegistrationViewModel
import io.xxlabs.messenger.ui.main.ud.registration.UdRegistrationViewModel
import javax.inject.Inject

class RegistrationAddedFragment : Fragment(), Injectable {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var udProfileRegistrationViewModel: UdRegistrationViewModel

    /* UI */

    private lateinit var binding: FragmentRegistrationAddedBinding
    private val ui : AddedRegistrationController by lazy { registrationViewModel }
    private val factType: FactType by lazy {
        RegistrationAddedFragmentArgs.fromBundle(requireArguments()).factType
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_registration_added,
            container,
            false
        )
        initViewModels()

        binding.factType = factType
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun initViewModels() {
        registrationViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[RegistrationViewModel::class.java]
        networkViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[NetworkViewModel::class.java]
        udProfileRegistrationViewModel =
            ViewModelProvider(this, viewModelFactory)[UdRegistrationViewModel::class.java]
        binding.ui = registrationViewModel
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.onAddedNavigateNextStep(factType).observe(viewLifecycleOwner) {
            if (it) {
                when (factType) {
                    FactType.EMAIL -> navigateNextStep()
                    FactType.PHONE -> navigateToChats()
                }
            }
        }
    }

    private fun navigateNextStep() {
        val directions = RegistrationAddedFragmentDirections
            .actionRegistrationAddedFragmentToRegistrationPhoneFragment()
        findNavController().navigate(directions)
        ui.onAddedNavigateHandled(factType)
    }

    private fun navigateToChats() {
        val directions = RegistrationAddedFragmentDirections.actionGlobalChats()
        findNavController().navigate(directions)
        ui.onAddedNavigateHandled(factType)
    }
}
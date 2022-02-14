package io.xxlabs.messenger.ui.intro.registration.tfa

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
import io.xxlabs.messenger.databinding.FragmentRegistration2faBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.support.dialog.info.InfoDialog
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.intro.registration.RegistrationViewModel
import io.xxlabs.messenger.ui.main.ud.registration.UdRegistrationViewModel
import javax.inject.Inject

class RegistrationTfaFragment : Fragment(), Injectable {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var registrationViewModel: RegistrationViewModel
    private lateinit var networkViewModel: NetworkViewModel
    private lateinit var udProfileRegistrationViewModel: UdRegistrationViewModel

    /* UI */

    private lateinit var binding: FragmentRegistration2faBinding
    private val ui : TfaRegistrationController by lazy { registrationViewModel }
    private val tfaCredentials: TwoFactorAuthCredentials by lazy {
        RegistrationTfaFragmentArgs.fromBundle(requireArguments()).tfaCredentials
    }
    private val factType: FactType by lazy { tfaCredentials.factType }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_registration_2fa,
            container,
            false
        )
        initViewModels()
        resetInput()
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    private fun resetInput() {
        binding.registrationEmail2faInput.editText?.apply {
            if (text.isNotBlank()) text = null
        }
    }

    private fun initViewModels() {
        registrationViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[RegistrationViewModel::class.java]
        networkViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory)[NetworkViewModel::class.java]
        udProfileRegistrationViewModel =
            ViewModelProvider(this, viewModelFactory)[UdRegistrationViewModel::class.java]

        binding.tfaCredentials = tfaCredentials
        binding.ui = registrationViewModel
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    private fun observeUI() {
        ui.tfaInfoClicked.observe(viewLifecycleOwner) { show ->
            if (show) displayTfaInfoDialog()
        }

        ui.onTfaNavigateNextStep(tfaCredentials).observe(viewLifecycleOwner) { next ->
            if (next) navigateNextStep()
        }
    }

    private fun displayTfaInfoDialog() {
        InfoDialog(ui.tfaDialogUI).show(requireActivity().supportFragmentManager, null)
        ui.onTfaInfoHandled()
    }

    private fun navigateNextStep() {
        val directions = RegistrationTfaFragmentDirections
            .actionRegistrationTfaFragmentToRegistrationAddedFragment(factType)
        findNavController().navigate(directions)
        ui.onTfaNavigateHandled(tfaCredentials)
    }
}
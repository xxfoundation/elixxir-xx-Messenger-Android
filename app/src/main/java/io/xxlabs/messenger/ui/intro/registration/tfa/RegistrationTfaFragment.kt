package io.xxlabs.messenger.ui.intro.registration.tfa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.databinding.FragmentRegistration2faBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.ui.dialog.info.InfoDialog
import io.xxlabs.messenger.ui.intro.registration.success.RegistrationStep
import io.xxlabs.messenger.ui.intro.registration.RegistrationFlowFragment

class RegistrationTfaFragment : RegistrationFlowFragment(), Injectable {

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
        InfoDialog.newInstance(ui.tfaDialogUI)
            .show(requireActivity().supportFragmentManager, null)
        ui.onTfaInfoHandled()
    }

    private fun navigateNextStep() {
        val step = when (factType) {
            FactType.EMAIL -> RegistrationStep.EMAIL
            else -> RegistrationStep.PHONE
        }
        val directions = RegistrationTfaFragmentDirections
            .actionRegistrationTfaFragmentToRegistrationCompletedStepFragment(step)
        findNavController().navigate(directions)
        ui.onTfaNavigateHandled(tfaCredentials)
    }
}
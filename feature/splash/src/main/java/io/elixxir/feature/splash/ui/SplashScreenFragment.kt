package io.elixxir.feature.splash.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.elixxir.core.ui.util.openLink
import io.elixxir.feature.splash.model.UpdateRecommended
import io.elixxir.feature.splash.model.UpdateRequired
import io.elixxir.feature.splash.model.VersionOk
import kotlinx.coroutines.launch

/**
 * Placeholder screen during app initialization.
 */
class SplashScreenFragment : Fragment() {

    private val viewModel: SplashScreenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                observeState()
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.appState.collect {
                with(it) {
                    when (versionState) {
                        is VersionOk -> {
                            if (userState == UserState.NewUser) navigateToRegistration()
                            else navigateToMain()
                        }
                        is UpdateRecommended -> showAlert(versionState.alertUi)
                        is UpdateRequired -> showAlert(versionState.alertUi)
                        else -> {}
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.launchUrl.collect {
                it?.let {
                    openLink(it)
                }
            }
        }
    }

    private fun navigateToRegistration() {
        TODO("Navigate to nav_registration graph")
    }

    private fun navigateToMain() {
        TODO("Navigate to nav_main graph")
    }

    private fun showAlert(alertUi: VersionAlertUi) {
        TODO()
    }
}
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
import io.elixxir.data.session.model.SessionState
import io.elixxir.data.version.model.UpdateRecommended
import io.elixxir.data.version.model.UpdateRequired
import io.elixxir.data.version.model.VersionOk
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
                            if (userState == SessionState.NewUser) navigateToRegistration()
                            else navigateToMain()
                        }
                        is UpdateRecommended -> showAlert(alert)
                        is UpdateRequired -> showAlert(alert)
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

    private fun showAlert(alertUi: VersionAlertUi?) {
        TODO()
    }
}
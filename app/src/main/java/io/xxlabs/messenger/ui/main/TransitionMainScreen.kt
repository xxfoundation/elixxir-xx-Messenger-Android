package io.xxlabs.messenger.ui.main


import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.ui.base.BaseFragment

/**
 * Just an empty black screen to serve as
 * placeholder to the main interaction
 */
class TransitionMainScreen : BaseFragment() {
    override fun onStart() {
        super.onStart()
        navigate()
    }

    fun navigate() {
        if (preferences.name.isNotEmpty()) findNavController().navigate(R.id.action_global_chats)
        else findNavController().navigate(R.id.action_global_onboarding)
    }
}
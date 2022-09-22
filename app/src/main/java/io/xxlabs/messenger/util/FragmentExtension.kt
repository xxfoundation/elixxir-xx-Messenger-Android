package io.xxlabs.messenger.util

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.MessengerApp

fun Fragment.log(message: String) {
    (requireActivity().application as MessengerApp).logger(message)
}

fun Fragment.navigateSafe(directions: NavDirections) {
    try {
        findNavController().navigate(directions)
    } catch (e: Exception) {
        log(e.message ?: genericError("navigate safely"))
    }
}
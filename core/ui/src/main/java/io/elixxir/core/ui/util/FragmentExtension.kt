package io.elixxir.core.ui.util

import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import io.elixxir.core.logging.log

fun Fragment.navigateSafe(directions: NavDirections) {
    try {
        findNavController().navigate(directions)
    } catch (e: Exception) {
        log(e.message ?: genericError("navigate safely"))
    }
}
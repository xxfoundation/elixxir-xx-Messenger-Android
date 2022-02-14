package io.xxlabs.messenger.support.extensions

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import timber.log.Timber

/**
 * Activity related extensions
 */
fun Activity.makeStatusBarTransparent() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

fun View.setInsets(bottomMask: Int? = null, topMask: Int? = null) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        bottomMask?.let { v.updatePadding(bottom = insets.getInsets(bottomMask).bottom) }
        topMask?.let {
            v.updatePadding(
                top = insets.getInsets(topMask).top
                        + (insets.displayCutout?.safeInsetTop ?: 0)
            )
        }
        insets
    }
}

fun Activity.changeStatusBarColor(color: Int) {
    window.statusBarColor = ContextCompat.getColor(this, color)
}

fun Activity.changeStatusBarIconTheme(lightMode: Boolean = false) {
    if (lightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.decorView.systemUiVisibility or
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
        }
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    window.decorView.systemUiVisibility and
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                } else {
                    window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }

        }
    }
}

/**
 * Navigation prevent double click crash
 */
fun NavController.navigateSafe(
    navDirections: Int,
    bundle: Bundle? = null,
    navOptions: NavOptions? = null,
    extras: Navigator.Extras? = null
) {
    try {
        navDirections.let {
            this.navigate(navDirections, bundle, navOptions, extras)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun NavController.isFragmentInBackStack(fragmentId: Int): Boolean {
    return try {
        val back: NavBackStackEntry = getBackStackEntry(fragmentId)
        Timber.v("in_back_stack: ${back.destination.label}")
        true

    } catch (ex: IllegalArgumentException) {
        Timber.e("no entries in backstack")
        false
    }
}
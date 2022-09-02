package io.xxlabs.messenger.util

import android.app.ActivityOptions
import android.content.Context
import android.os.Bundle

fun Context.getTransition(enterAnim: Int, exitAnim: Int): Bundle? {
    val options: ActivityOptions = ActivityOptions.makeCustomAnimation(
        this,
        enterAnim, exitAnim
    )
    return options.toBundle()
}
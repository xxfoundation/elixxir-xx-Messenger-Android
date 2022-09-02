package io.xxlabs.messenger.util

import android.app.ActivityOptions
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

fun Context.getTransition(enterAnim: Int, exitAnim: Int): Bundle? {
    val options: ActivityOptions = ActivityOptions.makeCustomAnimation(
        this,
        enterAnim, exitAnim
    )
    return options.toBundle()
}

fun Context.toast(message: String) {
    GlobalScope.launch(context = Dispatchers.Main) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}

fun Context.openLink(url: String) {
    val webIntent = Intent(Intent.ACTION_VIEW)
    webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
        webIntent.data = Uri.parse(url)
        val title = "Choose your browser"
        val chooser = Intent.createChooser(webIntent, title)
        startActivity(chooser)
    } catch (e: Exception) {
        toast("You have no browser apps available.")
        Timber.e(e, "Error on loading link $e")
    }
}
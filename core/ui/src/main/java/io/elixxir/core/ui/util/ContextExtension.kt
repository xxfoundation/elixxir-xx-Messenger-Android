package io.elixxir.core.ui.util

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import io.elixxir.core.logging.log
import io.elixxir.core.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

fun Fragment.openLink(url: String) {
    requireContext().openLink(url)
}

fun Context.openLink(url: String) {
    val webIntent = Intent(Intent.ACTION_VIEW)
    webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    try {
        webIntent.data = Uri.parse(url)
        val title = getString(R.string.url_intent_launcher_title)
        val chooser = Intent.createChooser(webIntent, title)
        startActivity(chooser)
    } catch (e: Exception) {
        toast(getString(R.string.url_intent_launcher_no_browser_found))
        log("Error on loading link $e")
    }
}
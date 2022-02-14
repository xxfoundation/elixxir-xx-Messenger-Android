package io.xxlabs.messenger.support.extensions

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.DialogInterface
import android.media.MediaPlayer
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import io.xxlabs.messenger.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.ln

fun Context.vibrateDevice(duration: Long = 5L) {
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    vibrator?.let {
        if (Build.VERSION.SDK_INT >= 26) {
            it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(duration)
        }
    }
}

fun Context.playBeepSound(isReceived: Boolean = true) {
    var mediaPlayer = if (isReceived) {
        MediaPlayer.create(this, R.raw.msg_received)
    } else {
        MediaPlayer.create(this, R.raw.msg_sent)
    }

    val maxVolume = 100.0
    val soundVolume = 60.0
    val volume =
        (1 - (ln((maxVolume - soundVolume)) / ln(maxVolume))).toFloat()
    mediaPlayer.setVolume(volume, volume)
    mediaPlayer?.start()
    Handler(Looper.getMainLooper()).postDelayed({
        mediaPlayer?.release()
        mediaPlayer = null
    }, mediaPlayer.duration.toLong())
}

fun Context.dialog(
    theme: Int?,
    title: String,
    msg: String,
    positiveListener: DialogInterface.OnClickListener? = null,
    negativeListener: DialogInterface.OnClickListener? = null,
    hideNegativeButton: Boolean = false
) {
    val builder = AlertDialog.Builder(this, theme ?: R.style.XxDialog)
    builder.setTitle(title)
        .setMessage(msg)

    positiveListener.apply {
        builder.setPositiveButton(getString(android.R.string.ok), this)
    }

    if (!hideNegativeButton) {
        builder.setNegativeButton(getString(android.R.string.cancel), negativeListener)
    }

    builder.create().show()
}

fun Context.toast(message: String) {
    when (val context = this) {
        is Fragment -> GlobalScope.launch(context = Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        is Activity -> GlobalScope.launch(context = Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
        else -> GlobalScope.launch(context = Dispatchers.Main) {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        }
    }
}

fun Context.toast(restId: Int) {
    when (val context = this) {
        is Fragment -> GlobalScope.launch(context = Dispatchers.Main) {
            Toast.makeText(context, context.getString(restId), Toast.LENGTH_LONG).show()
        }
        is Activity -> GlobalScope.launch(context = Dispatchers.Main) {
            Toast.makeText(context, context.getString(restId), Toast.LENGTH_LONG).show()
        }
        else -> GlobalScope.launch(context = Dispatchers.Main) {
            Toast.makeText(applicationContext, context.getString(restId), Toast.LENGTH_LONG).show()
        }
    }
}

fun Context.getTransition(enterAnim: Int, exitAnim: Int): Bundle? {
    val options: ActivityOptions = ActivityOptions.makeCustomAnimation(
        this,
        enterAnim, exitAnim
    )
    return options.toBundle()
}
package io.xxlabs.messenger.support.callback

import android.os.SystemClock
import android.view.View

class OnSingleClickListener(val listener: View.OnClickListener) : View.OnClickListener {
    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val currentClickTime = SystemClock.uptimeMillis()
        val elapsedTime = currentClickTime - lastClickTime
        lastClickTime = currentClickTime
        if (elapsedTime <= MIN_CLICK_INTERVAL) return

        listener.onClick(v)
    }

    companion object {
        private const val MIN_CLICK_INTERVAL: Long = 1000
    }
}
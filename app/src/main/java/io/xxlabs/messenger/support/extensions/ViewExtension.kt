package io.xxlabs.messenger.support.extensions

import android.content.res.ColorStateList
import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.widget.ImageViewCompat
import io.xxlabs.messenger.support.callback.OnSingleClickListener

fun View.semiTransparent(transparent: Boolean) {
    alpha = if (transparent) 0.5f else 1.0f
}

fun View.disable() {
    isEnabled = false
    isClickable = false
}

fun View.disableWithAlpha() {
    isEnabled = false
    isClickable = false
    alpha = 0.5f
}

fun View.enable() {
    isEnabled = true
    isClickable = true
    alpha = 1.0f
}

fun View.addKeyboardInsetListener(keyboardCallback: (visible: Boolean) -> Unit) {
    doOnLayout {
        //get init state of keyboard
        var keyboardVisible =
            ViewCompat.getRootWindowInsets(it)?.isVisible(WindowInsetsCompat.Type.ime()) == true

        //callback as soon as the layout is set with whether the keyboard is open or not
        keyboardCallback(keyboardVisible)

        //whenever there is an inset change on the App, check if the keyboard is visible.
        setOnApplyWindowInsetsListener { _, windowInsets ->
            val keyboardUpdateCheck =
                ViewCompat.getRootWindowInsets(it)?.isVisible(WindowInsetsCompat.Type.ime()) == true
            //since the observer is hit quite often, only callback when there is a change.
            if (keyboardUpdateCheck != keyboardVisible) {
                keyboardCallback(keyboardUpdateCheck)
                keyboardVisible = keyboardUpdateCheck
            }

            windowInsets
        }
    }
}

fun View.removeKeyboardInsetListener() {
    doOnLayout{}
}

fun EditText.setDrawableStart(drawable: Int) {
    setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
}

fun EditText.setDrawableEnd(drawable: Int) {
    setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
}

fun View.expandTouchArea(extraPadding: Int) {
    val parent = parent as View?
    parent?.post {
        val rect = Rect()
        getHitRect(rect)
        rect.top -= extraPadding
        rect.left -= extraPadding
        rect.right += extraPadding
        rect.bottom += extraPadding
        parent.touchDelegate = TouchDelegate(rect, this)
    }
}

fun View.expandTouchArea(top: Int = 0,  left: Int = 0, right: Int = 0, bottom: Int = 0) {
    val parent = parent as View?
    parent?.post {
        val rect = Rect()
        getHitRect(rect)
        rect.top -= top
        rect.left -= left
        rect.right += right
        rect.bottom += bottom
        parent.touchDelegate = TouchDelegate(rect, this)
    }
}

fun View.setOnSingleClickListener(l: View.OnClickListener) {
    setOnClickListener(OnSingleClickListener(l))
}

fun ImageView.setTint(@ColorRes colorRes: Int) {
    ImageViewCompat.setImageTintList(this, ColorStateList.valueOf(ContextCompat.getColor(context, colorRes)))
}
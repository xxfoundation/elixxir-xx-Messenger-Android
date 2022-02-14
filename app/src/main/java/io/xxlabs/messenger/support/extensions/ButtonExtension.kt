package io.xxlabs.messenger.support.extensions

import android.widget.Button
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun CompoundButton.setCustomChecked(
    value: Boolean,
    listener: CompoundButton.OnCheckedChangeListener
) {
    setOnCheckedChangeListener(null)
    isChecked = value
    setOnCheckedChangeListener(listener)
}

fun Button.changeColor(color: Int) {
    backgroundTintList = ContextCompat.getColorStateList(context, color)
}

fun FloatingActionButton.changeColor(color: Int) {
    backgroundTintList = ContextCompat.getColorStateList(context, color)
}
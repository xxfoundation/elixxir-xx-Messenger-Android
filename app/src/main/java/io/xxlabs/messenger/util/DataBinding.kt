package io.xxlabs.messenger.util

import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("uiText")
fun TextView.setUiText(uiText: UiText) {
    text = uiText.asString(context)
}
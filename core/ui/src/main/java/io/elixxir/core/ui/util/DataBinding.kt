package io.elixxir.core.ui.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import io.elixxir.core.ui.model.UiText

@BindingAdapter("uiText")
fun TextView.setUiText(uiText: UiText) {
    text = uiText.asString(context)
}
package io.elixxir.core.ui.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.google.android.material.textfield.TextInputLayout
import io.elixxir.core.ui.model.UiText

@BindingAdapter("uiText")
fun TextView.setUiText(uiText: UiText) {
    text = uiText.asString(context)
}

@BindingAdapter("uiText")
fun TextView.setUiText(uiTextLive: LiveData<UiText>) {
    findViewTreeLifecycleOwner()?.let {
        uiTextLive.observe(it) { text ->
            setUiText(text)
        }
    }
}

@BindingAdapter("error")
fun TextInputLayout.setUiTextError(uiText: UiText) {
    error = uiText.asString(context)
}
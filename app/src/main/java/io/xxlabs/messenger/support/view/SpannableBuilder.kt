package io.xxlabs.messenger.support.view

import android.graphics.Typeface
import android.text.style.StyleSpan

class SpannableBuilder {
    lateinit var what: Any
    var flags: Int = 0
    var textFlags: StyleSpan = StyleSpan(Typeface.NORMAL)
}
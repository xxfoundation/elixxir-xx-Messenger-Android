package io.xxlabs.messenger.support.extensions

import android.util.Base64
import kotlin.math.abs
import kotlin.math.log10

fun Long.length() = when (this) {
    0L -> 1L
    else -> log10(abs(toDouble())).toLong() + 1L
}

fun ByteArray.toBase64String(decodeFlag: Int = Base64.NO_WRAP): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

fun String.fromBase64toByteArray(decodeFlag: Int = Base64.NO_WRAP): ByteArray {
    return Base64.decode(this, Base64.NO_WRAP)
}
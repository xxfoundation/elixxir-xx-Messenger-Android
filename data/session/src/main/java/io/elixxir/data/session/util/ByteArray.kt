package io.elixxir.data.session.util

import android.util.Base64

fun ByteArray.toBase64String(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

fun String.fromBase64toByteArray(): ByteArray {
    return Base64.decode(this, Base64.NO_WRAP)
}
package io.elixxir.core.common

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

fun log(message: String, reportToFirebase: Boolean = false) {
    Timber.d(message)
    if (reportToFirebase) Firebase.crashlytics.log(message)
}

fun logException(e: Exception, reportToFirebase: Boolean = true) {
    Timber.e(e)
    if (reportToFirebase) Firebase.crashlytics.recordException(e)
}
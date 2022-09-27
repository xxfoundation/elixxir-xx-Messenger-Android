package io.elixxir.core.common

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

interface Config {
    val defaultDispatcher: CoroutineDispatcher
    fun log(message: String, export: Boolean = false)
    fun logException(e: Exception, export: Boolean = true)
}

object DefaultConfig : Config {
    override val defaultDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO

    override fun log(message: String, export: Boolean) {
        Timber.d(message)
        if (export) Firebase.crashlytics.log(message)
    }

    override fun logException(e: Exception, export: Boolean) {
        Timber.e(e)
        if (export) Firebase.crashlytics.recordException(e)
    }
}
package io.xxlabs.messenger.support.error

import io.reactivex.exceptions.UndeliverableException
import io.reactivex.functions.Consumer
import timber.log.Timber

object DefaultErrorHandler : Consumer<Throwable> {
    override fun accept(t: Throwable) {
        when (t) {
            is UndeliverableException -> accept(t.cause!!)
            is NullPointerException,
            is IllegalArgumentException -> Thread.currentThread().run {
                uncaughtExceptionHandler?.uncaughtException(this, t)
            }
            else -> { // Swallow the exception here. We logged it to Crashlytics...
                Timber.e(t)
            }
        }
    }
}
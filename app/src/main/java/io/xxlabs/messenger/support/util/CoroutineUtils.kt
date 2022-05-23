package io.xxlabs.messenger.support.util

import io.reactivex.Maybe
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend inline fun <reified T> Single<T>.value(
    scheduler: Scheduler = Schedulers.io()
): T = suspendCoroutine { continuation ->
    this.subscribeOn(scheduler)
        .observeOn(scheduler)
        .doOnSuccess { result -> continuation.resume(result) }
        .doOnError { error -> continuation.resumeWithException(error) }
        .subscribe()
}

suspend inline fun <reified T> Maybe<T>.value(
    scheduler: Scheduler = Schedulers.io()
): T? = suspendCoroutine { continuation ->
    this.subscribeOn(scheduler)
        .observeOn(scheduler)
        .doOnSuccess { result -> continuation.resume(result) }
        .doOnError { error -> continuation.resumeWithException(error) }
        .doOnComplete { continuation.resume(null) }
        .subscribe()
}
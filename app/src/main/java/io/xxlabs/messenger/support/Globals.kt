package io.xxlabs.messenger.support

import android.app.Application
import io.reactivex.Flowable
import io.reactivex.functions.BiFunction
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.data.datatype.Environment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.math.pow

private val SINGLE_EXECUTOR = Executors.newSingleThreadExecutor()
private val SINGLE_DISPATCHER = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

/**
 * Utility method to run blocks on a dedicated background thread, used for io/database work.
 */
fun ioThread(f : () -> Unit) {
    SINGLE_EXECUTOR.execute(f)
}

// TODO: Replace usages of this with local scope
fun singleThread(f: () -> Unit) {
    GlobalScope.launch(SINGLE_DISPATCHER) {
        f.invoke()
    }
}

fun singleExecutorInstance(): ExecutorService {
    return SINGLE_EXECUTOR
}

fun backoffRetry(errors: Flowable<Throwable>, maxTimes: Int): Flowable<Long>? {
    return errors.zipWith(
        Flowable.range(1, maxTimes + 1),
        BiFunction { error: Throwable, retryCount: Int ->
            if (error is UnknownHostException || maxTimes > 5) {
                throw error
            } else {
                retryCount
            }
        }).flatMap { retryCount: Int ->
        Flowable.timer(
            2.toDouble().pow(retryCount.toDouble()).toLong(),
            TimeUnit.SECONDS
        )
    }
}

fun isMockVersion() = BuildConfig.ENVIRONMENT == Environment.MOCK

fun appContext(): Application {
    return XxMessengerApplication.instance
}
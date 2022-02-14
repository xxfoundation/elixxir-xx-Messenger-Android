package io.xxlabs.messenger.support.extensions

import androidx.lifecycle.*
import io.reactivex.Flowable

/**
 * Converts LiveData into a Flowable
 */
fun <T> LiveData<T>.toFlowable(lifecycleOwner: LifecycleOwner) : Flowable<T> =
    Flowable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, this))


/**
 * Converts a Flowable into LiveData
 */
fun <T> Flowable<T>.toLiveData(): LiveData<T> = LiveDataReactiveStreams.fromPublisher(this)

fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T?, K?) -> R
): MutableLiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) {
        result.value = block(this.value, liveData.value)
    }
    result.addSource(liveData) {
        result.value = block(this.value, liveData.value)
    }
    return result
}
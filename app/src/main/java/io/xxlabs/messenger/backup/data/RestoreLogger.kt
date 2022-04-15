package io.xxlabs.messenger.backup.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

interface RestoreLog {
    val data: LiveData<List<String>>
}

class RestoreLogger : RestoreLog {
    private val eventsList = mutableListOf<String>()

    override val data: LiveData<List<String>> by ::_data
    private val _data: MutableLiveData<List<String>> = MutableLiveData(eventsList)

    fun log(event: String) {
        Timber.d(event)
        eventsList.add(event)
        _data.postValue(eventsList)
    }
}
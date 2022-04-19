package io.xxlabs.messenger.backup.data

import io.xxlabs.messenger.backup.bindings.AccountArchive
import io.xxlabs.messenger.backup.bindings.BackupTaskCallback
import timber.log.Timber

class BackupTaskEventManager : BackupTaskCallback, BackupTaskPublisher {
    private val listeners = mutableListOf<BackupTaskListener>()

    override fun onComplete(backupData: AccountArchive) {
        for (listener in listeners) {
            try {
                Timber.d("OnComplete")
                listener.onComplete()
            } catch (e: Exception) {
                listeners.remove(listener)
            }
        }
    }

    override fun subscribe(listener: BackupTaskListener) {
        if (listeners.contains(listener)) return
        listeners.add(listener)
    }

    override fun unsubscribe(listener: BackupTaskListener) {
        listeners.remove(listener)
    }
}

interface BackupTaskPublisher {
    fun subscribe(listener: BackupTaskListener)
    fun unsubscribe(listener: BackupTaskListener)
}

interface BackupTaskListener {
    fun onComplete()
}
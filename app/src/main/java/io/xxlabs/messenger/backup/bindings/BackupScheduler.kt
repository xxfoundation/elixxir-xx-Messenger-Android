package io.xxlabs.messenger.backup.bindings

import android.content.Context
import android.net.ConnectivityManager
import io.xxlabs.messenger.backup.data.AccountBackupRepository
import io.xxlabs.messenger.backup.data.BackupRepository
import io.xxlabs.messenger.backup.data.BackupTaskListener
import io.xxlabs.messenger.backup.model.BackupOption
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.appContext

/**
 * Automatically uploads backups to the active [BackupOption]
 * if the network & frequency preferences are met.
 */
class BackupScheduler(
    private val preferences: PreferencesRepository,
    private val backupRepository: AccountBackupRepository<*>
) : BackupTaskListener {

    private val network: ConnectivityManager by lazy {
        appContext().getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
    }

    override fun onComplete() {
        if (preferences.autoBackup && networkPreferencesMet()) {
            backupRepository.getActiveBackupOption()?.backupNow()
        }
    }

    private fun networkPreferencesMet(): Boolean {
        return when (preferences.wiFiOnlyBackup) {
            true -> network.isWiFi()
            false -> network.isOnline()
        }
    }

    private fun ConnectivityManager.isOnline(): Boolean {
        return activeNetworkInfo?.isConnected == true
    }

    private fun ConnectivityManager.isWiFi(): Boolean {
        return isOnline()
                && getNetworkInfo(activeNetwork)?.type == ConnectivityManager.TYPE_WIFI
    }
}
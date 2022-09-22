package io.xxlabs.messenger.backup.data.backup

import android.content.Context
import android.net.ConnectivityManager
import io.xxlabs.messenger.support.appContext

class AccountBackupScheduler(
    private val preferences: BackupPreferencesRepository,
    private val backupManager: BackupManager
) : BackupTaskListener {

    private val network: ConnectivityManager by lazy {
        appContext().getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
    }

    override fun onComplete() {
        if (preferences.autoBackup && networkPreferencesMet()) {
            backupManager.getActiveBackupOption()?.backupNow()
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
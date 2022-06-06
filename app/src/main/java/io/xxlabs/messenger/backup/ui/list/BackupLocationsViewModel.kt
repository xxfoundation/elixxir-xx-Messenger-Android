package io.xxlabs.messenger.backup.ui.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.cloud.CloudAuthentication
import io.xxlabs.messenger.backup.model.AccountBackup
import io.xxlabs.messenger.backup.model.BackupLocation
import io.xxlabs.messenger.backup.cloud.AuthResultCallback
import io.xxlabs.messenger.backup.data.AccountBackupDataSource
import io.xxlabs.messenger.backup.data.BackupSource
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.dialog.info.TwoButtonInfoDialogUI

abstract class BackupLocationsViewModel(
    private val dataSource: AccountBackupDataSource,
    private val cloudAuthSource: CloudAuthentication,
) : ViewModel(), BackupLocationsController {

    private var backupLocation: BackupLocation? = null

    /* UI */

    private val backupLocationsMap: MutableMap<BackupLocation, AccountBackup> = mutableMapOf()

    override val locations: List<LocationOption> =
        dataSource.locations.map {
            backupLocationsMap[it.location] = it
            BackupLocationOption(it.location, ::onLocationSelected)
        }

    override val navigateToDetail: LiveData<BackupSource?> by ::_navigateToDetail
    private val _navigateToDetail = MutableLiveData<BackupSource?>(null)

    override val authLaunchConsentDialog: LiveData<TwoButtonInfoDialogUI?>
        by ::_authLaunchConsentDialog
    private val _authLaunchConsentDialog = MutableLiveData<TwoButtonInfoDialogUI?>(null)

    private val authLaunchConsentHandler: AuthLaunchConsentHandler
        get() {
            return AuthLaunchConsentHandler.create(
                backupLocation?.name ?: "Cloud storage",
                ::onConsentGiven,
                ::onConsentDenied,
            )
        }

    override val backupError: LiveData<String?> by ::_backupError
    private val _backupError = MutableLiveData<String?>(null)

    override val isLoading: LiveData<Boolean> by ::_isLoading
    private val _isLoading = MutableLiveData(false)

    override fun onNavigationHandled() {
        setLoading(false)
        _navigateToDetail.value = null
    }

    override fun onErrorHandled() {
        _backupError.value = null
    }

    protected open fun onLocationSelected(backupLocation: BackupLocation) {
        with (backupLocation) {
            when {
                !signInRequired() -> onAuthSuccess(this)
                authBackgroundsApp() -> requestAuthLaunchConsent(this)
                else -> signIn(this)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    private fun requestAuthLaunchConsent(backupLocation: BackupLocation) {
        this.backupLocation = backupLocation
        _authLaunchConsentDialog.value = authLaunchConsentHandler.authLaunchConsentUI
    }

    private fun onConsentGiven() {
        backupLocation?.let { signIn(it) }
    }

    private fun onConsentDenied() {}

    override fun onLaunchConsentHandled() {
        _authLaunchConsentDialog.value = null
    }

    private fun signIn(backupLocation: BackupLocation) {
        setLoading(true)
        val authHandler = backupLocation.createAuthHandler(
            object : AuthResultCallback {
                override fun onFailure(errorMsg: String) = onAuthFailure(errorMsg)
                override fun onSuccess() = onAuthSuccess(backupLocation)
            }
        )
        authHandler?.let {
            cloudAuthSource.signIn(it)
        } ?: run { navigateToDetail(backupLocation) }
    }

    private fun onAuthFailure(errorMsg: String) {
        setError(errorMsg)
    }

    protected fun setError(errorMsg: String) {
        _backupError.value = errorMsg
    }

    protected open fun onAuthSuccess(backupLocation: BackupLocation) {
        navigateToDetail(backupLocation)
    }

    protected fun navigateToDetail(backupLocation: BackupLocation) {
        backupLocationsMap[backupLocation]?.let { backup ->
            dataSource.getSourceFor(backup)?.let { source ->
                _navigateToDetail.value = source
            }
        }
    }

    protected fun getAccountBackup(backupLocation: BackupLocation): AccountBackup? =
        backupLocationsMap[backupLocation]
}

private class AuthLaunchConsentHandler private constructor() {
    private var location: String = "Cloud storage"
    private var positiveLabel: Int = android.R.string.ok
    private var negativeLabel: Int = android.R.string.cancel
    private var onConsentGiven: () -> Unit = {}
    private var onConsentDenied: () -> Unit = {}

    val authLaunchConsentUI: TwoButtonInfoDialogUI by lazy {
        TwoButtonInfoDialogUI.create(
            infoDialogUi,
            _positiveLabel = positiveLabel,
            _negativeLabel = negativeLabel,
            onPositiveClick = onConsentGiven,
            onNegativeClick =onConsentDenied,
        )
    }

    private val infoDialogUi: InfoDialogUI by lazy {
        InfoDialogUI.create(
            appContext().getString(R.string.backup_restore_auth_consent_dialog_title, location),
            appContext().getString(R.string.backup_restore_auth_consent_dialog_body)
        )
    }

    companion object {
        fun create(
            locationName: String,
            _onConsentGiven: () -> Unit = {},
            _onConsentDenied: () -> Unit = {}
        ) = AuthLaunchConsentHandler().apply {
            location = locationName
            positiveLabel = R.string.backup_restore_auth_consent_dialog_positive_button
            negativeLabel = R.string.backup_restore_auth_consent_dialog_negative_button
            onConsentGiven = _onConsentGiven
            onConsentDenied = _onConsentDenied
        }
    }
}

interface LocationOption : BackupLocation {
    fun onClick()
}

private data class BackupLocationOption(
    private val location: BackupLocation,
    private val _onClick: (BackupLocation) -> Unit,
) : LocationOption, BackupLocation by location {
    override fun onClick() = _onClick(location)
}
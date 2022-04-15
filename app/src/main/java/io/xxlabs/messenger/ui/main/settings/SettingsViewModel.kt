package io.xxlabs.messenger.ui.main.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.misc.DebugLogger
import timber.log.Timber
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepository: DaoRepository,
    private val preferencesRepository: PreferencesRepository,
    private val schedulers: SchedulerProvider,
    private val deleteAccount: DeleteAccountUIController
) : ViewModel(), DeleteAccountUIController by deleteAccount {
    private val subscriptions = CompositeDisposable()

    var enableNotifications = MutableLiveData<DataRequestState<Boolean>>()
    var enableBiometrics = MutableLiveData<SimpleRequestState<Boolean>>()
    var enableCrashReport = MutableLiveData<Boolean>()
    var enableDebug = MutableLiveData<SimpleRequestState<Boolean>>()
    var deleteUser = MutableLiveData<DataRequestState<Boolean>>()
    var deleteSuccess = MutableLiveData<Any>()

    fun enablePushNotifications(enable: Boolean) {
        enableNotifications.postValue(DataRequestState.Start())
        if (enable) {
            registerForPushNotifications()
        } else {
            unregisterForPushNotifications()
        }
    }

    private fun registerForPushNotifications() {
        subscriptions.add(
            repo.registerNotificationsToken()
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onSuccess = { token ->
                        Timber.d("new token successfully sent! $this")
                        preferencesRepository.areNotificationsOn = true
                        preferencesRepository.currentNotificationsTokenId = token!!
                        preferencesRepository.notificationsTokenId = token
                        enableNotifications.postValue(DataRequestState.Success(true))
                    },
                    onError = { err ->
                        Timber.e("error on sending token ${err.localizedMessage}")
                        enableNotifications.postValue(DataRequestState.Error(err))
                    })
        )
    }

    private fun unregisterForPushNotifications() {
        subscriptions.add(
            repo.unregisterForNotification()
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onSuccess = {
                        Timber.d("unregistered token with success")
                        onUnregisterSuccess()
                    },
                    onError = { err ->
                        Timber.e("error on unregistering token: ${err.localizedMessage}")
                        if (err.localizedMessage?.contains("Failed to find user with intermediary ID") == true) {
                            onUnregisterSuccess()
                        } else {
                            enableNotifications.postValue(DataRequestState.Error(err))
                        }
                    })
        )
    }

    private fun onUnregisterSuccess() {
        preferencesRepository.areNotificationsOn = false
        preferencesRepository.currentNotificationsTokenId = ""
        preferencesRepository.notificationsTokenId = ""
        enableNotifications.postValue(DataRequestState.Success(false))
    }

    fun enableInAppNotifications(enable: Boolean) {
        preferencesRepository.areInAppNotificationsOn = enable
    }

    fun enableCoverTraffic(enable: Boolean) {
        preferencesRepository.isCoverTrafficOn = enable
    }

    fun enableDebug(context: Context, isEnabled: Boolean) {
        if (!isEnabled) {
            DebugLogger.cancelProcess(context)
            enableDebug.postValue(SimpleRequestState.Success(false))
            preferencesRepository.areDebugLogsOn = false
        } else {
            subscriptions.add(
                DebugLogger.initService(context)
                    .subscribeOn(schedulers.single)
                    .observeOn(schedulers.main)
                    .subscribeBy(
                        onError = { err ->
                            enableDebug.postValue(SimpleRequestState.Error(err))
                        },
                        onSuccess = {
                            Timber.d("debug logs are enabled: $it")
                            preferencesRepository.areDebugLogsOn = true
                            enableDebug.postValue(SimpleRequestState.Success(true))
                        }
                    )
            )
        }
    }

    fun enableBiometrics(isEnabled: Boolean) {
        enableBiometrics.value = SimpleRequestState.Success(isEnabled)
    }

    fun exportLatestLog(context: Context) {
        DebugLogger.exportLatestLog(context)
    }

    fun getLogSize(context: Context): String {
        return DebugLogger.getLogSize(context)
    }

    fun enableCrashReport(enable: Boolean) {
        enableCrashReport.value = enable
        preferencesRepository.isCrashReportEnabled = enable
    }

    fun arePushNotificationsOn(): Boolean {
        return preferencesRepository.areNotificationsOn
    }

    fun areInAppNotificationsOn(): Boolean {
        return preferencesRepository.areInAppNotificationsOn
    }

    fun isCoverTrafficOn(): Boolean {
        return preferencesRepository.isCoverTrafficOn
    }

    fun areDebugLogsOn(): Boolean {
        return preferencesRepository.areDebugLogsOn
    }

    fun isCrashReportOn(): Boolean {
        return preferencesRepository.isCrashReportEnabled
    }

    fun deleteUser() {
        deleteUser.value = DataRequestState.Start()
        subscriptions.add(
            repo.unregisterForNotification()
                .flatMap {
                    repo.deleteUser()
                }
                .flatMap {
                    repo.stopNetworkFollower()
                }
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.io)
                .flatMap {
                    preferencesRepository.clearAll()
                    var trial = 0
                    var deleted: Boolean
                    do {
                        deleted = BindingsWrapperBindings.getSessionFolder(appContext())
                            ?.deleteRecursively() ?: false
                        trial++
                        Timber.v("Trying to delete session folder ($trial): $deleted")
                    } while (!deleted || trial < 3)

                    Single.just(deleted)
                }
                .flatMap {
                    daoRepository.deleteAll()
                }.observeOn(schedulers.main)
                .subscribeBy(
                    onError = { err ->
                        deleteUser.value = DataRequestState.Error(err)
                    },
                    onSuccess = {
                        deleteUser.value = DataRequestState.Success(true)
                    }
                )
        )
    }

    /* Account backup & restore navigation handling */

    val navigateToBackupSettings: LiveData<Boolean> by ::_navigateToBackupSettings
    private val _navigateToBackupSettings = MutableLiveData(false)

    val navigateToBackupSetup: LiveData<Boolean> by ::_navigateToBackupSetup
    private val _navigateToBackupSetup = MutableLiveData(false)

    fun onBackupClicked() {
        if (preferencesRepository.backupLocation == null) _navigateToBackupSetup.value = true
        else _navigateToBackupSettings.value = true
    }

    fun onBackupNavigationHandled() {
        _navigateToBackupSettings.value = false
        _navigateToBackupSetup.value = false
    }

    override fun onCleared() {
        super.onCleared()
        subscriptions.clear()
    }
}
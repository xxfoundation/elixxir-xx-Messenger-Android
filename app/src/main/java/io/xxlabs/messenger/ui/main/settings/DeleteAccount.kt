package io.xxlabs.messenger.ui.main.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Single
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.support.appContext
import javax.inject.Inject
import android.content.Context.ACTIVITY_SERVICE

import androidx.core.content.ContextCompat.getSystemService

import android.app.ActivityManager

import android.os.Build
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.Exception


class DeleteAccount @Inject constructor(
    private val repo: BaseRepository,
    private val daoRepository: DaoRepository,
    private val preferences: PreferencesRepository,
    private val schedulers: SchedulerProvider,
    private val application: Application,
) : DeleteAccountUIController {
    
    override var confirmDeleteInput: String = ""
        set(value) {
            when (value == preferences.name) {
                true -> validInput()
                false -> invalidInput()
            }
            field = value
        }

    override val inputError: LiveData<String?> get() = _inputError
    private val _inputError = MutableLiveData<String?>(null)

    override val infoClicked: LiveData<Boolean> get() = _infoClicked
    private val _infoClicked = MutableLiveData(false)

    override val confirmEnabled: LiveData<Boolean> get() = _confirmEnabled
    private val _confirmEnabled = MutableLiveData(false)

    override val loading: LiveData<Boolean> get() = _loading
    private val _loading = MutableLiveData(false)
    
    override val accountDeleted: LiveData<Boolean> get() = _accountDeleted
    private val _accountDeleted = MutableLiveData(false)
    
    override val deletionError: LiveData<String?> get() = _deletionError
    private val _deletionError = MutableLiveData<String?>(null)
    
    override val cancelClicked: LiveData<Boolean> get() = _cancelClicked
    private val _cancelClicked = MutableLiveData(false)

    private fun validInput() {
        _confirmEnabled.value = true
        _inputError.value = null
    }

    private fun invalidInput() {
        _confirmEnabled.value = false
        _inputError.value = application.getString(R.string.settings_delete_account_username_error)
    }

    override fun onInfoClicked() {
        _infoClicked.value = true
    }

    override fun onConfirmDeleteClicked() {
        _loading.value = true
        _confirmEnabled.value = false
        deleteAccount()
    }

    private fun deleteAccount() {
        repo.unregisterForNotification()
            .flatMap { repo.deleteUser() }
            .flatMap { repo.stopNetworkFollower() }
            .subscribeOn(schedulers.single)
            .observeOn(schedulers.io)
            .flatMap {
                // Wait for running processes to complete.
                var isBusy: Boolean
                do {
                    Thread.sleep(RUNNING_PROCESSES_POLL_INTERVAL)
                    isBusy = ClientRepository.clientWrapper.client.hasRunningProcessies()
                } while (isBusy)

                Single.just(clearAppData())
            }
            .flatMap { daoRepository.deleteAll() }
            .observeOn(schedulers.main)
            .doOnError { onDeletionError(it) }
            .doOnSuccess { onDeletionSuccess() }
            .subscribe()
    }

    private fun clearAppData(): Boolean {
        return try {
            (application.getSystemService(ACTIVITY_SERVICE) as? ActivityManager)
                ?.clearApplicationUserData() ?: false
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            false
        }
    }

    private fun onDeletionError(error: Throwable) {
        _deletionError.value = bindingsErrorMessage(error)
        _loading.value = false
        _confirmEnabled.value = true
    }

    private fun onDeletionSuccess() {
        _accountDeleted.value = true
    }
    
    override fun onCancelClicked() {
        _cancelClicked.value = true
    }

    override fun onAccountDeletedHandled() {
        _accountDeleted.value = false
    }

    override fun onInfoHandled() {
        _infoClicked.value = false
    }

    override fun onCancelHandled() {
        _cancelClicked.value = false
    }

    companion object {
        private const val RUNNING_PROCESSES_POLL_INTERVAL = 1_000L
    }
}
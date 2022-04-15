package io.xxlabs.messenger.backup.ui.restore

import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.xxlabs.messenger.backup.data.BackupDataSource
import io.xxlabs.messenger.backup.model.RestoreEnvironment
import io.xxlabs.messenger.backup.model.RestoreOption
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.support.appContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RestoreDetailViewModel @AssistedInject constructor(
    private val dataSource: BackupDataSource<RestoreOption>,
    @Assisted override val backup: RestoreOption,
    @Assisted private val restorePassword: ByteArray,
): ViewModel(), RestoreDetailController {

    override val restoreComplete: LiveData<Boolean> by ::_restoreComplete
    private val _restoreComplete = MutableLiveData(false)

    override val cancelRestore: LiveData<Boolean> by ::_cancelRestore
    private val _cancelRestore = MutableLiveData(false)

    override val restoreError: LiveData<String?> by ::_restoreError
    private val _restoreError = MutableLiveData<String?>()

    override val isLoading: LiveData<Boolean> by ::_isLoading
    private val _isLoading = MutableLiveData(false)

    override val state: LiveData<RestoreState> get() =
        Transformations.map(backup.progress) { progress ->
            progress?.run {
                when (bytesTransferred) {
                    bytesTotal -> success
                    else -> startedState.also { setLoading(false) }
                }
            } ?: readyState
        }

    private val readyState: RestoreReady get() =
        RestoreReady.create(
            ::attemptRestore,
            ::cancelRestore
        )

    private val startedState: RestoreStarted = object : RestoreStarted {
        override val restore: RestoreOption = backup
    }

    private val success: RestoreSuccess get() =
        RestoreSuccess.create { _restoreComplete.value = true }

    private var restoreTask: Job? = null

    private fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    private fun attemptRestore() {
        setLoading(true)
        restoreTask?.let { return@let }
        restoreTask = viewModelScope.launch {
            try {
                backup.restore(restoreEnvironment)
            } catch (e: Exception) {
                _restoreError.postValue(e.message)
            }
        }
    }

    private fun cancelRestore() {
        _cancelRestore.value = true
    }

    override fun onErrorHandled() {
        _restoreError.value = null
    }

    private val restoreEnvironment: RestoreEnvironment
        get() {
            return RestoreEnvironment(
                BindingsWrapperBindings.getNdf(),
                BindingsWrapperBindings.createSessionFolder(appContext()).path,
                restorePassword,
                byteArrayOf()
            )
        }

    companion object {
        fun provideFactory(
            assistedFactory: BackupFoundViewModelFactory,
            backup: RestoreOption,
            restorePassword: ByteArray
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(backup, restorePassword) as T
            }
        }
    }
}

@AssistedFactory
interface BackupFoundViewModelFactory {
    fun create(backup: RestoreOption, restorePassword: ByteArray): RestoreDetailViewModel
}
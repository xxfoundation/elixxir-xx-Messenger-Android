package io.xxlabs.messenger.ui.main.ud.profile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import timber.log.Timber
import javax.inject.Inject

class UdProfileViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    private val preferencesRepository: PreferencesRepository,
    private val schedulers: SchedulerProvider
) : ViewModel() {
    var subscriptions = CompositeDisposable()
    var usernameField = MutableLiveData<String?>()
    var emailField = MutableLiveData<String?>()
    var phoneField = MutableLiveData<String?>()
    var errHandling = MutableLiveData<Throwable?>()

    fun initUdData() {
        refreshData()
    }

    fun refreshData() {
        usernameField.postValue(repo.getStoredUsername())
        emailField.postValue(repo.getStoredEmail())
        phoneField.postValue(Country.toFormattedNumber(repo.getStoredPhone()))
    }

    fun getProfileNickname(): CharSequence {
        return preferencesRepository.name
    }

    fun removeFact(factType: FactType) {
        subscriptions.add(
            repo.removeFact(factType)
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .doOnSuccess {
                    if (factType == FactType.EMAIL) {
                        emailField.value = null
                    } else {
                        phoneField.value = null
                    }
                }.doOnError { err ->
                    Timber.e(err.localizedMessage)
                    errHandling.postValue(err)
                }.subscribe()
        )
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }
}

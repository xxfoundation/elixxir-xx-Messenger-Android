package io.xxlabs.messenger.ui.main.ud.registration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import timber.log.Timber
import javax.inject.Inject

class UdRegistrationViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    private val schedulers: SchedulerProvider
) : ViewModel() {
    var subscriptions = CompositeDisposable()

    //Registration Step State
    var registrationStep = MutableLiveData<UdRegistrationStep>()
    var emailRegistrationState = MutableLiveData<DataRequestState<String>>()
    var phoneRegistrationState = MutableLiveData<DataRequestState<String>>()

    //Callback code
    var emailCode = MutableLiveData<Boolean?>()
    var phoneCode = MutableLiveData<Boolean?>()
    var codeLoading = MutableLiveData<DataRequestState<Any>>()

    //Internal
    var registeredEmail: Boolean = false
    var registeredPhone: Boolean = false

    init {
        registrationStep.value = UdRegistrationStep.NONE
    }


    fun confirmFact(confirmationId: String, code: String, fact: String, isEmailCode: Boolean) {
        codeLoading.value = DataRequestState.Start()
        repo.confirmFact(confirmationId, code, fact, isEmailCode)
            .subscribeOn(schedulers.single)
            .observeOn(schedulers.main)
            .doOnError { err ->
                if (isEmailCode) {
                    Timber.e("UD: Failed to register email: ${err.localizedMessage}")
                    emailCode.value = false
                } else {
                    Timber.e("UD: Failed to register phone: ${err.localizedMessage}")
                    phoneCode.value = false
                }
                codeLoading.value = DataRequestState.Error(err)
            }.doOnSuccess {
                if (isEmailCode) {
                    Timber.v("UD: Email registered with success")
                    emailCode.value = true
                } else {
                    Timber.v("UD: Phone registered with success")
                    phoneCode.value = true
                }
                codeLoading.value = DataRequestState.Success(true)
            }.subscribe()
    }


    fun registerEmail(email: String) {
        emailRegistrationState.postValue(DataRequestState.Start())
        subscriptions.add(
            repo.registerUdEmail(email.lowercase())
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .doOnError { err ->
                    Timber.e("Error registering email: ${err.localizedMessage}")
                    registeredEmail = false
                    emailRegistrationState.value = DataRequestState.Error(err)
                }.doOnSuccess { confirmationId ->
                    Timber.v("Successfully registered UD email")
                    registeredEmail = true
                    emailRegistrationState.value = DataRequestState.Success(confirmationId)
                }.subscribe()
        )
    }

    fun registerPhone(phone: String) {
        phoneRegistrationState.postValue(DataRequestState.Start())
        subscriptions.add(
            repo.registerUdPhone(phone)
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .doOnError { err ->
                    Timber.e("Error registering phone: ${err.localizedMessage}")
                    registeredPhone = false
                    phoneRegistrationState.value = DataRequestState.Error(err)
                }.doOnSuccess { confirmationId ->
                    Timber.v("Successfully registered UD phone")
                    registeredPhone = true
                    phoneRegistrationState.value = DataRequestState.Success(confirmationId)
                }.subscribe()
        )
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }
}

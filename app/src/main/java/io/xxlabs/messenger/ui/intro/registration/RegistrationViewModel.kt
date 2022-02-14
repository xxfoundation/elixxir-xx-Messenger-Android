package io.xxlabs.messenger.ui.intro.registration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.data.datatype.input.RegistrationInputState
import io.xxlabs.messenger.data.datatype.input.UsernameFieldState
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.util.ValidationUtils
import io.xxlabs.messenger.ui.intro.registration.added.AddedRegistrationController
import io.xxlabs.messenger.ui.intro.registration.email.EmailRegistrationController
import io.xxlabs.messenger.ui.intro.registration.phone.PhoneRegistrationController
import io.xxlabs.messenger.ui.intro.registration.tfa.TfaRegistrationController
import io.xxlabs.messenger.ui.intro.registration.username.UsernameRegistrationController
import io.xxlabs.messenger.ui.intro.registration.welcome.WelcomeRegistrationController
import timber.log.Timber
import javax.inject.Inject

class RegistrationViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    private val preferences: PreferencesRepository,
    private val schedulers: SchedulerProvider,
    usernameRegistration: UsernameRegistrationController,
    welcomeRegistration: WelcomeRegistrationController,
    emailRegistration: EmailRegistrationController,
    phoneRegistration: PhoneRegistrationController,
    addedRegistration: AddedRegistrationController,
    tfaRegistration: TfaRegistrationController,
) : ViewModel(),
    UsernameRegistrationController by usernameRegistration,
    WelcomeRegistrationController by welcomeRegistration,
    EmailRegistrationController by emailRegistration,
    PhoneRegistrationController by phoneRegistration,
    AddedRegistrationController by addedRegistration,
    TfaRegistrationController by tfaRegistration
{

    //Rx
    var subscriptions = CompositeDisposable()

    var usernameRequestState = MutableLiveData<SimpleRequestState<Boolean>>()
    var registrationState = MutableLiveData<RegistrationInputState>()

    //Private Fields
    var isUsernameValid: Boolean = false
    var lastErrMsg: String? = ""

    init {
        registrationState.value = RegistrationInputState.NONE
        isUsernameValid = false
    }

    fun registerUsername(username: String) {
        subscriptions.add(
            repo.registerUdUsername(username)
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .doOnError { err ->
                    Timber.e("Error registering username: ${err.localizedMessage}")
                    usernameRequestState.postValue(SimpleRequestState.Error(err))
                }.doOnSuccess {
                    Timber.v("Successfully registered UD username")
                    preferences.name = username
                    usernameRequestState.postValue(SimpleRequestState.Success(true))
                }.subscribe()
        )
    }

    fun completeUsernameState() {
        usernameRequestState.value = SimpleRequestState.Completed()
    }

    fun setRegistrationState(usernameMessage: RegistrationInputState) {
        registrationState.value = usernameMessage
    }

    fun isNameValid(input: TextInputLayout) {
        val usernameCheck = ValidationUtils.isUsernameValid(input, false)
        isUsernameValid = usernameCheck == UsernameFieldState.VALID

        when (usernameCheck) {
            UsernameFieldState.EMPTY -> {
                registrationState.postValue(RegistrationInputState.EMPTY)
            }
            UsernameFieldState.VALID -> {
                registrationState.postValue(RegistrationInputState.NAME_VALID)
            }

            UsernameFieldState.INVALID_LENGTH -> {
                registrationState.postValue(
                    RegistrationInputState.NAME_INVALID_LENGTH
                )
            }
            UsernameFieldState.INVALID_CHARACTERS -> {
                registrationState.postValue(
                    RegistrationInputState.NAME_INVALID_CHARACTERS
                )
            }
            UsernameFieldState.MAX_CHARACTERS -> {
                registrationState.postValue(
                    RegistrationInputState.NAME_MAX_CHARACTERS
                )
            }
        }
    }

    fun clearFields() {
        isUsernameValid = false
        registrationState.value = RegistrationInputState.NONE
    }

    fun getStoredMail(): String {
        return repo.getStoredEmail()
    }

    fun getStoredPhone(): String {
        return Country.toFormattedNumber(repo.getStoredPhone(), false) ?: ""
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }
}

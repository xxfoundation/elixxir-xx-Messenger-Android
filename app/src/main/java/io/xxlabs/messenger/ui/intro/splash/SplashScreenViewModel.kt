package io.xxlabs.messenger.ui.intro.splash

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.ioThread
import io.xxlabs.messenger.support.singleThread
import io.xxlabs.messenger.support.util.Utils
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SplashScreenViewModel @Inject constructor(
    val repo: BaseRepository,
    val preferences: PreferencesRepository,
    val schedulers: SchedulerProvider
) : ViewModel() {
    //Rx
    var subscriptions = CompositeDisposable()
    val onRegisterSession = MutableLiveData<Boolean>()
    fun doesUserSessionExists() = repo.doesBindingsFolderExists()
    fun isUserDataFilled() = preferences.userData.isNotBlank()

    init {
        fetchCommonErrors()
        getKronosTime()
    }

    fun fetchCommonErrors() {
        subscriptions.add(
            BindingsWrapperBindings.downloadCommonErrors()
                .timeout(1, TimeUnit.SECONDS)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .doOnError { Timber.v("[SplashScreenViewModel] Error retrieving common errors!") }
                .doOnSuccess { BindingsWrapperBindings.updateCommonErrors(it) }
                .subscribe()
        )
    }

    private fun getKronosTime() {
        val clock = XxMessengerApplication.kronosClock
        ioThread {
            val time = clock.getCurrentNtpTimeMs()
            time?.let {
                onKronosSuccess(it)
            } ?: onKronosError()
        }
    }

    private fun onKronosSuccess(time: Long) {
        Timber.v("Refreshed with success!")
        Timber.v("Kronos time retrieved: $time")
        Timber.v("Kronos time Now (ms): ${XxMessengerApplication.kronosClock.getCurrentNtpTimeMs()}")
        Timber.v("Java time: ${Utils.getCurrentTimeStamp()}")
        BindingsWrapperBindings.setTimeSource(XxMessengerApplication.getKronosTime())
    }

    private fun onKronosError() {
        Timber.v("Kronos time is null")
    }

    fun registerUser(context: Context, rsaDecryptPwd: ByteArray) {
        val appFolder = repo.createSessionFolder(context)
        Timber.v("Registering User...")

        val time = System.currentTimeMillis()
        Timber.v("Starting time (new client): $time")

        singleThread {
            try {
                repo.newClient(appFolder, rsaDecryptPwd)
                Timber.v("End time (new client): ${System.currentTimeMillis()}")
                val regTime =
                    "Creation time (new client) | Scheduler Single: ${System.currentTimeMillis() - time}ms"
                Timber.v(regTime)
                preferences.lastAppVersion = BuildConfig.VERSION_CODE
                onRegisterSession(true)
            } catch (err: Exception) {
                err.printStackTrace()
                onRegisterSession(false)
            }
        }
    }

    fun onRegisterSession(value: Boolean) {
        schedulers.main.scheduleDirect {
            onRegisterSession.value = value
        }
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }
}
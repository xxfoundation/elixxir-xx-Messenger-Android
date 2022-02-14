package io.xxlabs.messenger.application

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.NotificationManagerCompat
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.log.Logger
import com.datadog.android.privacy.TrackingConsent
import com.datadog.android.rum.GlobalRum
import com.datadog.android.rum.RumMonitor
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.lyft.kronos.AndroidClockFactory
import com.lyft.kronos.KronosClock
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.data.datatype.Environment
import io.xxlabs.messenger.di.utils.AppInjector
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.error.DefaultErrorHandler
import io.xxlabs.messenger.support.extensions.toast
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.misc.DebugLogger
import timber.log.Timber
import timber.log.Timber.DebugTree
import javax.inject.Inject


open class XxMessengerApplication : MultiDexApplication(), HasAndroidInjector {
    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Any>

    override fun androidInjector(): AndroidInjector<Any> = activityInjector

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    companion object {
        private var activityVisible = false
        lateinit var kronosClock: KronosClock
        lateinit var appResources: Resources
        lateinit var instance: XxMessengerApplication
            private set

        var isMessageListenerRegistered: Boolean = false
        var isNetworkCallbackRegistered: Boolean = false
        var isAuthCallbackRegistered: Boolean = false
        var isUserDiscoveryRunning: Boolean = false

        fun isActivityVisible(): Boolean {
            return activityVisible
        }

        fun activityResumed() {
            NotificationManagerCompat.from(instance).cancelAll()
            activityVisible = true
        }

        fun activityPaused() {
            activityVisible = false
        }

        fun getKronosTime(): () -> (Long) = {
            kronosClock.getCurrentTimeMs()
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()

        appResources = resources
        instance = this

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        RxJavaPlugins.setErrorHandler { DefaultErrorHandler } // nothing or some logging

        initGrpc()

        //Timber Init
        Timber.plant(DebugTree())

        //Dagger
        AppInjector.init(this)

        //Kronos
        initializeKronos()

        //Firebase
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(
            preferencesRepository.isCrashReportEnabled && !BuildConfig.DEBUG
        )

        //Debug Logs
        initDebugLogs()
    }

    private fun initGrpc() {
        if (!isMockVersion()) {
            BindingsWrapperBindings.registerGrpc()
        }
    }

    private fun initializeKronos() {
        kronosClock = AndroidClockFactory.createKronosClock(
            applicationContext
        )
        kronosClock.syncInBackground()

        val kronosNtpTime = kronosClock.getCurrentNtpTimeMs()
        val kronosTimeMs = kronosClock.getCurrentTimeMs()

        Timber.v("Kronos NTP time: $kronosNtpTime")
        Timber.v("Kronos Time Ms: $kronosTimeMs")
    }

    private fun initDebugLogs() {
        if (preferencesRepository.areDebugLogsOn) {
            DebugLogger
                .initService(this)
                .subscribeOn(Schedulers.io())
                .doOnError { err ->
                    Timber.e("Error: ${err.localizedMessage}")
                    toast("Failed initializing Debug Logger")
                }.subscribe()
        }
    }
}
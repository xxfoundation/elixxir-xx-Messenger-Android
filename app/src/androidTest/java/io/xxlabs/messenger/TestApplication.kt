package io.xxlabs.messenger

import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.di.utils.AppInjector
import io.xxlabs.messenger.di.utils.TestAppInjector

class TestApplication : XxMessengerApplication() {

    override fun onCreate() {
        super.onCreate()
        TestAppInjector.init(this)
    }
}
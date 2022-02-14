package io.xxlabs.messenger.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import io.xxlabs.messenger.notifications.MessagingService

@Module
abstract class ServiceBuilderModule {
    @ContributesAndroidInjector
    abstract fun provideMessagingService(): MessagingService
}

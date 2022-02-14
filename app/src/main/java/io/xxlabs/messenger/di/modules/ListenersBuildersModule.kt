package io.xxlabs.messenger.di.modules

import dagger.Module
import dagger.Provides
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import javax.inject.Singleton

@Module
class ListenersBuildersModule {
    @Provides
    @Singleton
    fun provideTextMessageListener(
        daoRepository: DaoRepository,
        schedulers: SchedulerProvider,
        preferences: PreferencesRepository
    ): MessageReceivedListener {
        return MessageReceivedListener.getInstance(
            daoRepository,
            schedulers,
            preferences
        )
    }
}
package io.xxlabs.messenger.di.modules

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import io.xxlabs.messenger.application.AppDatabase
import io.xxlabs.messenger.application.AppRxSchedulers
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.backup.BackupModule
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.repository.mock.ClientMockRepository
import io.xxlabs.messenger.requests.RequestsModule
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.ui.main.settings.SettingsModule
import javax.inject.Singleton

@Module(includes = [
    ViewModelModule::class,
    SettingsModule::class,
    BackupModule::class,
    RequestsModule::class
])
class AppModule {
    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return AppDatabase.getInstance(app)
    }

    @Provides
    @Singleton
    fun provideContext(
        app: Application
    ): Context {
        return app
    }

    @Provides
    @Singleton
    fun provideBindingsRepository(
        schedulers: SchedulerProvider,
        daoRepo: DaoRepository,
        preferencesRepository: PreferencesRepository,
        messageReceivedListener: MessageReceivedListener,
        backupService: BackupService
    ): BaseRepository {
        return if (isMockVersion()) {
            ClientMockRepository(preferencesRepository)
        } else {
            ClientRepository.getInstance(
                schedulers,
                daoRepo,
                preferencesRepository,
                messageReceivedListener,
                backupService
            )
        }
    }

    @Provides
    @Singleton
    fun provideDaoRepository(
        db: AppDatabase,
        schedulers: SchedulerProvider
    ): DaoRepository {
        return DaoRepository.getInstance(db, schedulers)
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        context: Context
    ): PreferencesRepository {
        return PreferencesRepository.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideRxSchedulers(): SchedulerProvider {
        return AppRxSchedulers()
    }
}
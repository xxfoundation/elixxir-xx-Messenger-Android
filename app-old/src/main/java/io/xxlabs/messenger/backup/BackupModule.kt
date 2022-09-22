package io.xxlabs.messenger.backup

import dagger.Binds
import dagger.Module
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.bindings.BindingsBackupMediator
import io.xxlabs.messenger.backup.data.backup.*
import io.xxlabs.messenger.backup.data.restore.RestoreManager
import io.xxlabs.messenger.backup.data.restore.RestoreMediator
import io.xxlabs.messenger.repository.PreferencesRepository
import javax.inject.Singleton

@Module
interface BackupModule {
    @Singleton
    @Binds
    fun backupService(
        service: BindingsBackupMediator
    ): BackupService

    @Singleton
    @Binds
    fun backupManager(
        backupManager: BackupMediator
    ): BackupManager

    @Binds
    fun restoreManager(
        restoreManager: RestoreMediator
    ): RestoreManager

    @Singleton
    @Binds
    fun backupTaskPubisher(
        backupTaskEventManager: BackupTaskEventManager
    ): BackupTaskPublisher

    @Binds
    fun backupPreferencesRepository(
        preferencesRepository: PreferencesRepository
    ): BackupPreferencesRepository
}
package io.xxlabs.messenger.backup

import dagger.Binds
import dagger.Module
import io.xxlabs.messenger.backup.bindings.BindingsBackupMediator
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.backup.data.BackupDataSource
import io.xxlabs.messenger.backup.data.BackupRepository
import io.xxlabs.messenger.backup.data.RestoreRepository
import io.xxlabs.messenger.backup.model.BackupOption
import io.xxlabs.messenger.backup.model.RestoreOption
import javax.inject.Singleton

@Module
interface BackupModule {

    @Singleton
    @Binds
    fun backupDataSource(
        backupDataSource: BackupRepository
    ): BackupDataSource<BackupOption>

    @Binds
    fun restoreDataSource(
        restoreDataSource: RestoreRepository
    ): BackupDataSource<RestoreOption>

    @Binds
    fun backupService(
        service: BindingsBackupMediator
    ): BackupService
}
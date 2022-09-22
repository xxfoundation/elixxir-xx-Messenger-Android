package io.xxlabs.messenger.ui.main.settings

import dagger.Binds
import dagger.Module

@Module
interface SettingsModule {

    @Binds
    fun deleteAccount(deleteAccount: DeleteAccount): DeleteAccountUIController
}
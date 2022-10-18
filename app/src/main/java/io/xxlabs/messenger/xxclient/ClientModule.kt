package io.xxlabs.messenger.xxclient

import dagger.Binds
import dagger.Module
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.PasswordStorage
import javax.inject.Singleton

@Module
interface ClientModule {

    @Binds
    @Singleton
    fun bindEnvironment(environment: DevEnvironment): MessengerEnvironment

    @Binds
    @Singleton
    fun bindPasswordStorage(keystore: AndroidKeyStore): PasswordStorage
}
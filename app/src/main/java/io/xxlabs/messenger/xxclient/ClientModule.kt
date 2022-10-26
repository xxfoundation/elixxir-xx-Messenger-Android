package io.xxlabs.messenger.xxclient

import dagger.Binds
import dagger.Module
import dagger.Provides
import io.elixxir.xxmessengerclient.Messenger
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.PasswordStorage
import javax.inject.Singleton

@Module(includes = [
    ClientModule::class
])
object MessengerModule {
    @Provides
    @Singleton
    fun provideMessenger(environment: MessengerEnvironment): Messenger {
        return Messenger(environment)
    }
}

@Module
abstract class ClientModule {

    @Binds
    @Singleton
    abstract fun bindEnvironment(environment: MainUDEnvironment): MessengerEnvironment

    @Binds
    @Singleton
    abstract fun bindPasswordStorage(keystore: AndroidKeyStore): PasswordStorage
}
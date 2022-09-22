package io.xxlabs.messenger.ui.main.chats

import dagger.Binds
import dagger.Module
import io.xxlabs.messenger.ui.main.chats.data.NewConnectionsDataSource
import io.xxlabs.messenger.ui.main.chats.data.NewConnectionsRepository
import javax.inject.Singleton

@Module
interface ChatsModule {

    @Singleton
    @Binds
    fun newConnectionsDataSource(
        newConnectionsRepository: NewConnectionsRepository
    ): NewConnectionsDataSource
}
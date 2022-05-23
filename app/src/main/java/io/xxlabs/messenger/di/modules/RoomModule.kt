package io.xxlabs.messenger.di.modules

import dagger.Module
import dagger.Provides
import io.xxlabs.messenger.application.AppDatabase
import io.xxlabs.messenger.data.room.dao.ContactsDao
import io.xxlabs.messenger.data.room.dao.MessagesDao
import io.xxlabs.messenger.data.room.dao.RequestsDao
import javax.inject.Singleton


@Module
class RoomModule {
    @Singleton
    @Provides
    fun provideContactsDao(database: AppDatabase): ContactsDao = database.contactsDao()

    @Singleton
    @Provides
    fun provideMessagesDao(database: AppDatabase): MessagesDao = database.messagesDao()

    @Singleton
    @Provides
    fun provideRequestsDao(database: AppDatabase): RequestsDao = database.requestsDao()
}
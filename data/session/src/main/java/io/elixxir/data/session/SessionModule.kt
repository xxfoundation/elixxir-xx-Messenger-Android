package io.elixxir.data.session

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.elixxir.data.session.data.KeyStoreManager
import io.elixxir.data.session.data.SessionDataSource
import io.elixxir.data.session.data.XxmKeyStore

@Module
@InstallIn(SingletonComponent::class)
interface SessionModule {

    @Binds
    fun bindKeyStoreManager(
        keyStore: XxmKeyStore
    ): KeyStoreManager

    @Binds
    fun bindSessionRepository(
        repo: SessionDataSource,
    ): SessionRepository
}
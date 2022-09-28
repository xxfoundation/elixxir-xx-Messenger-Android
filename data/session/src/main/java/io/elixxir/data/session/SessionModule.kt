package io.elixxir.data.session

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.elixxir.core.common.Config
import io.elixxir.data.session.data.KeyStoreManager
import io.elixxir.data.session.data.SessionDataSource
import io.elixxir.data.session.data.XxmKeyStore
import io.elixxir.xxclient.cmix.CMix
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SessionModule {

    @Binds
    fun bindKeyStoreManager(
        keyStore: XxmKeyStore
    ): KeyStoreManager

    @Singleton
    @Binds
    fun bindSessionRepository(
        repo: SessionDataSource,
    ): SessionRepository
}
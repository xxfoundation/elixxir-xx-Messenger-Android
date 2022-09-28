package io.elixxir.core.common

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CommonModule {

    @Singleton
    @Binds
    fun bindConfig(
        config: DefaultConfig
    ): Config
}
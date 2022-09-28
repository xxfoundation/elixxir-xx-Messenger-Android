package io.elixxir.data.networking

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.elixxir.data.networking.data.BindingsDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BindingsModule {

    @Singleton
    @Provides
    fun provideBindingsRepository(): BindingsRepository {
        return BindingsDataSource()
    }
}
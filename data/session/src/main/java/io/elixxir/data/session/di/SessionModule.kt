package io.elixxir.data.session.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.elixxir.data.session.SessionRepository
import io.elixxir.data.session.data.SessionDataSource

@Module
@InstallIn(ViewModelComponent::class)
interface SessionModule {

    @Provides
    fun provideSessionRepository(): SessionRepository {
        return SessionDataSource()
    }
}
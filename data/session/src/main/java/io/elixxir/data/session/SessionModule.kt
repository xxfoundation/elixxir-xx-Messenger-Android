package io.elixxir.data.session

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.elixxir.data.session.data.SessionDataSource

@Module
@InstallIn(ViewModelComponent::class)
interface SessionModule {

    @Binds
    fun bindSessionRepository(
        repo: SessionDataSource,
    ): SessionRepository
}
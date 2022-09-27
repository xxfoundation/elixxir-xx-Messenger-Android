package io.elixxir.data.version.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.elixxir.data.version.VersionRepository
import io.elixxir.data.version.data.VersionDataSource

@Module
@InstallIn(ViewModelComponent::class)
interface VersionModule {

    @Provides
    fun provideVersionRepository(): VersionRepository {
        return VersionDataSource()
    }
}
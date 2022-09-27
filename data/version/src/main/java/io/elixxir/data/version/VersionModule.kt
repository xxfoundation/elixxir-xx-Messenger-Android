package io.elixxir.data.version

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.elixxir.data.version.data.VersionDataSource

@Module
@InstallIn(ViewModelComponent::class)
interface VersionModule {

    @Binds
    fun bindVersionRepository(
        repo: VersionDataSource
    ): VersionRepository
}
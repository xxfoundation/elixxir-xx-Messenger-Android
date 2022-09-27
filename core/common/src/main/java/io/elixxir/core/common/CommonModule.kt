package io.elixxir.core.common

import com.google.android.datatransport.runtime.dagger.Binds
import com.google.android.datatransport.runtime.dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CommonModule {

    @Binds
    fun bindConfig(
        config: DefaultConfig
    ): Config
}
package io.elixxir.core.preferences.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.elixxir.core.preferences.PreferencesRepository

@Module
@InstallIn(ViewModelComponent::class)
interface PreferencesModule {

    @Binds
    fun bindPreferencesRepository(

    ): PreferencesRepository
}
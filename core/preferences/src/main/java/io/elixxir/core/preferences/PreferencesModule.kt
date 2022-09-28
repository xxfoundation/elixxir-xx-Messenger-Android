package io.elixxir.core.preferences

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.elixxir.core.preferences.model.KeyStorePreferences
import io.elixxir.core.preferences.model.KeyStorePrefs

@Module
@InstallIn(SingletonComponent::class)
interface PreferencesModule {

    @Binds
    fun bindKeyStorePreferences(
        prefs: KeyStorePrefs
    ): KeyStorePreferences
}
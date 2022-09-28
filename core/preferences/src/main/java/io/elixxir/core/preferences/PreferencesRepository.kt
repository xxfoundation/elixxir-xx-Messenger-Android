package io.elixxir.core.preferences

import io.elixxir.core.preferences.model.KeyStorePreferences

interface PreferencesRepository {
    val keyStore: KeyStorePreferences
}
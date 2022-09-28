package io.elixxir.core.preferences.model

import io.elixxir.core.preferences.PreferencesRepository
import javax.inject.Inject

interface KeyStorePreferences {
    var userSecret: String?
}

class KeyStorePrefs @Inject internal constructor(
    repo: PreferencesRepository
) : KeyStorePreferences, PreferencesRepository by repo {

    override var userSecret: String?
        get() = preferences.getString(USER_SECRET, null)
        set(value) {
            preferences.edit().putString(USER_SECRET, value).apply()
        }

    companion object Keys {
        private const val USER_SECRET = "secret_k"
    }
}
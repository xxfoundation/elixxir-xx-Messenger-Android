package io.elixxir.core.preferences

import android.content.SharedPreferences

interface PreferencesRepository {
    val preferences: SharedPreferences
}
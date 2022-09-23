package io.elixxir.core.preferences

interface PreferencesRepository {
    fun doesUserExist(): Boolean
}
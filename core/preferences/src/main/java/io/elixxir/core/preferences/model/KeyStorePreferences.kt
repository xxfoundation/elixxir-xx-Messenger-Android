package io.elixxir.core.preferences.model

import javax.inject.Inject

interface KeyStorePreferences {
    var userSecret: String
}

class KeyStorePrefs @Inject internal constructor() : KeyStorePreferences {
    override var userSecret: String = ""
}
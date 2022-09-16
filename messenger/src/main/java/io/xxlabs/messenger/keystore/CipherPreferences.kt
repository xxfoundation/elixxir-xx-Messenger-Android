package io.xxlabs.messenger.keystore

interface CipherPreferences {
    var userSecret: String
}

class CipherPrefs() : CipherPreferences {

    override var userSecret: String = ""
}
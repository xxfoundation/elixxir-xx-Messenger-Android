package io.xxlabs.messenger.cipher

interface CipherPreferences {
    var userSecret: String
}

class CipherPrefs() : CipherPreferences {

    override var userSecret: String = ""
}
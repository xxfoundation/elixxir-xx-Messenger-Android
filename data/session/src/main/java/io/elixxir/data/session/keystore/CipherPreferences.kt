package io.elixxir.data.session.keystore

interface CipherPreferences {
    var userSecret: String
}

class CipherPrefs() : CipherPreferences {

    override var userSecret: String = ""
}
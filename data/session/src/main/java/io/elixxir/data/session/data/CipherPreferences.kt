package io.elixxir.data.session.data

interface CipherPreferences {
    var userSecret: String
}

class CipherPrefs() : CipherPreferences {

    override var userSecret: String = ""
}
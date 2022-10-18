package io.xxlabs.messenger.xxclient

import io.elixxir.xxclient.password.PasswordStorage
import io.elixxir.xxmessengerclient.MessengerEnvironment

class DevEnvironment(
    override val passwordStorage: PasswordStorage,
    override val storageDir: String,
    override val udCert: ByteArray,
    override val udContact: ByteArray
): MessengerEnvironment()
package io.xxlabs.messenger.bindings.wrapper.user

import io.elixxir.xxmessengerclient.Messenger
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings

class UserBindings(private val messenger: Messenger) : UserBase {
    override fun getReceptionId(): ByteArray {
        return messenger.e2e?.receptionIdentity ?: byteArrayOf()
    }

    override fun getContact(): ContactWrapperBase {
        return ContactWrapperBindings(messenger.myContact())
    }

    override fun getTransmissionID(): ByteArray {
        return byteArrayOf()
    }
}
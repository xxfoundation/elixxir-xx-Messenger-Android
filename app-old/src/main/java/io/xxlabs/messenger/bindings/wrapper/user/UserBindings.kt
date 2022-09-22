package io.xxlabs.messenger.bindings.wrapper.user

import bindings.User
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings

class UserBindings(val user: User): UserBase {
    override fun getReceptionId(): ByteArray {
        return user.receptionID
    }

    override fun getContact(): ContactWrapperBase {
        return ContactWrapperBindings(user.contact)
    }

    override fun getTransmissionID(): ByteArray {
        return user.transmissionID
    }
}
package io.xxlabs.messenger.bindings.wrapper.user

import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings

class UserBindings(): UserBase {
    override fun getReceptionId(): ByteArray {
        TODO()
//        return user.receptionID
    }

    override fun getContact(): ContactWrapperBase {
        TODO()
//        return ContactWrapperBindings(user.contact)
    }

    override fun getTransmissionID(): ByteArray {
        TODO()
//        return user.transmissionID
    }
}
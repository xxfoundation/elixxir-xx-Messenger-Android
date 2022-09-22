package io.xxlabs.messenger.bindings.wrapper.user

import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperMock
import io.xxlabs.messenger.data.room.model.ContactData

class UserMock(val contactData: ContactData): UserBase {
    override fun getReceptionId(): ByteArray {
        return contactData.userId
    }

    override fun getContact(): ContactWrapperBase {
        return ContactWrapperMock(contactData)
    }

    override fun getTransmissionID(): ByteArray {
        return contactData.userId
    }
}
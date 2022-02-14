package io.xxlabs.messenger.bindings.wrapper.user
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase

interface UserBase {
    fun getReceptionId(): ByteArray
    fun getTransmissionID(): ByteArray
    fun getContact(): ContactWrapperBase
}
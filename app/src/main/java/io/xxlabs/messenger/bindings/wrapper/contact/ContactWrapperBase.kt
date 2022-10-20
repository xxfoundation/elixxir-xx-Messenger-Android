package io.xxlabs.messenger.bindings.wrapper.contact

import io.elixxir.xxclient.models.ContactAdapter


interface ContactWrapperBase {
    fun getId(): ByteArray

    fun addUsername(username: String)
    fun addEmail(email: String)
    fun addPhone(phone: String)
    fun addName(nickname: String)

    fun getUsernameFact(raw: Boolean = false): String
    fun getEmailFact(raw: Boolean = false): String?
    fun getPhoneFact(raw: Boolean = false): String?
    fun getNameFact(raw: Boolean = false): String?
    fun getFormattedPhone(): String?

    fun marshal(): ByteArray
    fun getStringifiedFacts(): String
    fun getDisplayName(): CharSequence?

    companion object {
        fun from(data: ByteArray = byteArrayOf()): ContactWrapperBase {
            return ContactWrapperBindings(ContactAdapter(data))
        }
    }
}
package io.xxlabs.messenger.bindings.wrapper.contact

import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.support.isMockVersion
import timber.log.Timber

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
        fun from(data: Any? = null, factsList: List<String>? = null): ContactWrapperBase {
            TODO()
//            if (isMockVersion()) {
//                val dataString = String(data as ByteArray)
//                val split = dataString.split("::")
//                Timber.v("[CONTACT WRAPPER MOCK] Split: $split")
//                val contact = ContactData()
//
//                if (split.size >= 5) {
//                    contact.username = split[0]
//                    contact.userId = split[1].toByteArray()
//                    contact.email = split[2]
//                    contact.phone = split[3]
//                    contact.nickname = split[4]
//                    contact.status = split[5].toInt()
//                } else {
//                    val username = if (dataString.length > 28) {
//                        dataString.substring(0, 32)
//                    } else {
//                        dataString
//                    }
//                    contact.username = username
//                }
//
//                return ContactWrapperMock(contact)
//            } else {
//                val contact = data as Contact
//                factsList?.forEach { fact ->
//                    Timber.v("Fact: $fact")
//                    when {
//                        fact[0] == 'U' -> {
//                            contact.factList.add(fact.substring(1), FactType.USERNAME.value)
//                        }
//                        fact[0] == 'E' -> {
//                            contact.factList.add(fact.substring(1), FactType.EMAIL.value)
//                        }
//                        fact[0] == 'P' -> {
//                            contact.factList.add(fact.substring(1), FactType.PHONE.value)
//                        }
//                    }
//                }
//                return ContactWrapperBindings(data)
//            }
        }
    }
}
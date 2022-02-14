package io.xxlabs.messenger.bindings.wrapper.contact

import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.room.model.ContactData

class ContactWrapperMock(
    val contact: ContactData
) : ContactWrapperBase {
    override fun getId(): ByteArray {
        return contact.userId
    }

    private fun addFactToFactList(fact: String, type: FactType) {
        when (type) {
            FactType.EMAIL -> {
                contact.email = fact.substring(1)
            }
            FactType.PHONE -> {
                contact.phone = fact.substring(1)
            }
            FactType.USERNAME -> {
                contact.username = fact.substring(1)
            }
            FactType.NICKNAME -> {
                contact.nickname = fact.substring(1)
            }
        }
    }

    override fun addUsername(username: String) {
        addFactToFactList(username, FactType.USERNAME)
    }

    override fun addEmail(email: String) {
        addFactToFactList(email, FactType.EMAIL)
    }

    override fun addPhone(phone: String) {
        addFactToFactList(phone, FactType.PHONE)
    }

    override fun addName(nickname: String) {
        addFactToFactList(nickname, FactType.NICKNAME)
    }

    override fun getUsernameFact(raw: Boolean): String {
        return if (!contact.username.isNullOrBlank()) {
            if (raw) {
                "U${contact.username}"
            } else {
                contact.username
            }
        } else {
            ""
        }
    }

    override fun getEmailFact(raw: Boolean): String? {
        return if (!contact.email.isNullOrBlank()) {
            if (raw) {
                "E${contact.email}"
            } else {
                contact.email
            }
        } else {
            null
        }
    }

    override fun getPhoneFact(raw: Boolean): String? {
        return if (!contact.phone.isNullOrBlank()) {
            if (raw) {
                "P${contact.phone}"
            } else {
                contact.phone
            }
        } else {
            null
        }
    }

    override fun getNameFact(raw: Boolean): String? {
        return if (!contact.nickname.isNullOrBlank()) {
            if (raw) {
                "N${contact.nickname}"
            } else {
                contact.nickname
            }
        } else {
            null
        }
    }

    override fun getFormattedPhone(): String? {
        return Country.toFormattedNumber(getPhoneFact(true))
    }

    override fun marshal(): ByteArray {
        val contactFacts = contact.username + "::" + contact.userId + "::"+
                contact.email + "::"  + contact.phone +  "::" + contact.nickname + "::" + contact.status
        return contactFacts.toByteArray(Charsets.ISO_8859_1)
    }

    override fun getStringifiedFacts(): String {
        return contact.toString()
    }

    override fun getDisplayName(): CharSequence? {
        return if (getNameFact().isNullOrEmpty()) {
            getUsernameFact()
        } else {
            getNameFact()
        }
    }
}
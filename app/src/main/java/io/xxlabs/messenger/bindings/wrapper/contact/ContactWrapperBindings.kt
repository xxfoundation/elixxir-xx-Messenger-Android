package io.xxlabs.messenger.bindings.wrapper.contact

import io.elixxir.xxclient.models.Contact
import io.elixxir.xxclient.models.Fact
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.datatype.FactType

class ContactWrapperBindings(
    private var contact: Contact
) : ContactWrapperBase {
    override fun getId(): ByteArray {
        return contact.getIdFromContact()
    }

    private fun addFactToFactList(fact: String, type: FactType) {
        contact = contact.setFactsOnContact(
            listOf(Fact(fact, type.value))
        )
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
        return getFactStringify(FactType.USERNAME, raw) ?: "xx messenger User"
    }

    override fun getEmailFact(raw: Boolean): String? {
        return getFactStringify(FactType.EMAIL, raw)
    }

    override fun getPhoneFact(raw: Boolean): String? {
        return getFactStringify(FactType.PHONE, raw)
    }

    override fun getNameFact(raw: Boolean): String? {
        return getFactStringify(FactType.NICKNAME, raw)
    }

    override fun getFormattedPhone(): String? {
        return Country.toFormattedNumber(getPhoneFact(true))
    }

    override fun marshal(): ByteArray {
        return contact.data
    }

    private fun getFact(type: FactType): Fact? {
        return contact.getFactsFromContact().firstOrNull {
            it.type == type.value
        }
    }

    private fun getFactStringify(type: FactType, raw: Boolean): String? {
        val prefix = if (raw) {
            when (type) {
                FactType.USERNAME -> 'U'
                FactType.EMAIL -> 'E'
                FactType.PHONE -> 'P'
                FactType.NICKNAME -> ""
            }
        } else ""
        return getFact(type)?.fact?.let {
            "$prefix$it"
        }
    }

    override fun getStringifiedFacts(): String {
        return contact.getFactsFromContact().mapNotNull { fact ->
            FactType.from(fact.type)?.let { factType ->
                getFactStringify(factType, true)
            }
        }.joinToString(separator = ",", postfix = ";")
    }

    override fun getDisplayName(): CharSequence {
        return getNameFact() ?: getUsernameFact()
    }
}

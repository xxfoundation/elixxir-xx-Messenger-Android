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
        return getFactStringify(FactType.USERNAME) ?: "xx messenger User"
    }

    override fun getEmailFact(raw: Boolean): String? {
        return getFactStringify(FactType.EMAIL)
    }

    override fun getPhoneFact(raw: Boolean): String? {
        return getFactStringify(FactType.PHONE)
    }

    override fun getNameFact(raw: Boolean): String? {
        return getFactStringify(FactType.NICKNAME)
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

    private fun getFactStringify(type: FactType): String? {
        return getFact(type)?.fact
    }

    override fun getStringifiedFacts(): String {
        return contact.getFactsFromContact().toString()
    }

    override fun getDisplayName(): CharSequence {
        return getNameFact() ?: getUsernameFact()
    }
}

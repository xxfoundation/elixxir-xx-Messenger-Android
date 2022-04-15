package io.xxlabs.messenger.bindings.wrapper.ud

import bindings.Fact
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperMock
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.room.model.ContactData

data class UserDiscoveryWrapperMock(
    var userContact: ContactWrapperMock
) : UserDiscoveryWrapperBase {
    override fun search(input: String, factType: FactType, callback: (ContactWrapperBase?, String?) -> (Unit)) {
        val contact = ContactData()
        contact.username = input
        callback.invoke(ContactWrapperMock(contact), null)
    }

    override fun searchSingle(input: String, callback: (ContactWrapperBase?, String?) -> (Unit)) {
        val contact = ContactData()
        contact.username = input
        callback.invoke(ContactWrapperMock(contact), null)
    }

    override fun addUsernameToContact(username: String) {
        userContact.addUsername(username)
    }

    override fun addEmailToContact(email: String) {
        userContact.addEmail(email)
    }

    override fun addPhoneToContact(phone: String) {
        userContact.addPhone(phone)
    }

    override fun getUdUsername(raw: Boolean): String {
        return userContact.getUsernameFact(raw)
    }

    override fun getUdEmail(raw: Boolean): String? {
        return userContact.getEmailFact(raw)
    }

    override fun getUdPhone(raw: Boolean): String? {
        return userContact.getPhoneFact(raw)
    }

    override fun registerUdUsername(username: String) {
        userContact.addUsername(username)
    }

    override fun registerUdNickname(nickname: String): String {
        val newFact = Fact(FactType.NICKNAME.value, nickname)
        return addFactToUd(newFact)
    }

    override fun registerUdEmail(email: String): String {
        val newFact = Fact(FactType.EMAIL.value, email)
        return addFactToUd(newFact)
    }

    override fun registerUdPhone(phone: String): String {
        val newFact = Fact(FactType.PHONE.value, phone)
        return addFactToUd(newFact)
    }

    override fun addFactToUd(newFact: Fact): String {
        return newFact.stringify()
    }

    override fun confirmFact(confirmationId: String, confirmationCode: String) {}

    override fun removeFact(factType: FactType): Boolean {
        return true
    }

    override fun deleteUser(username: String) {}
    override fun userLookup(userId: ByteArray, callback: (ContactWrapperBase?, String?) -> Unit) {

    }

    override fun multiLookup(
        ids: List<ByteArray>,
        callback: (List<ContactWrapperBase>?, ids: IdListBase, String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun setAlternativeUD(ipAddress: ByteArray, cert: ByteArray, contactFile: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun restoreNormalUD() {
        TODO("Not yet implemented")
    }
}
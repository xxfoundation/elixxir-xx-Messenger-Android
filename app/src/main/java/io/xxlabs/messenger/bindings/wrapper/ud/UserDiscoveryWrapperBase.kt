package io.xxlabs.messenger.bindings.wrapper.ud

import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.data.datatype.FactType

interface UserDiscoveryWrapperBase {
    fun search(input: String, factType: FactType, callback: (ContactWrapperBase?, String?) -> Unit)
    fun searchSingle(input: String, callback: (ContactWrapperBase?, String?) -> Unit)
    fun addUsernameToContact(username: String)
    fun addEmailToContact(email: String)
    fun addPhoneToContact(phone: String)
    fun getUdUsername(raw: Boolean = false): String
    fun getUdEmail(raw: Boolean = false): String?
    fun getUdPhone(raw: Boolean = false): String?
    fun registerUdUsername(username: String)
    fun registerUdNickname(nickname: String): String
    fun registerUdEmail(email: String): String
    fun registerUdPhone(phone: String): String
    fun addFactToUd(/*newFact: Fact*/): String
    fun confirmFact(confirmationId: String, confirmationCode: String)
    fun removeFact(factType: FactType): Boolean
    fun deleteUser(username: String)
    fun userLookup(userId: ByteArray, callback: (ContactWrapperBase?, String?) -> Unit)
    fun multiLookup(ids: List<ByteArray>, callback: (List<ContactWrapperBase>?, ids: IdListBase, String?) -> Unit)
    fun setAlternativeUD(ipAddress: ByteArray, cert: ByteArray, contactFile: ByteArray)
    fun restoreNormalUD()
}
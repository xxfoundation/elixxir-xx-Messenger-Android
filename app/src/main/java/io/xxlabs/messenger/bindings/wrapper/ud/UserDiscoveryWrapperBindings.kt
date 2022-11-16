package io.xxlabs.messenger.bindings.wrapper.ud

import io.elixxir.xxclient.callbacks.UdLookupResultListener
import io.elixxir.xxclient.callbacks.UdMultiLookupResultListener
import io.elixxir.xxclient.callbacks.UdSearchResultListener
import io.elixxir.xxclient.models.Fact
import io.elixxir.xxclient.models.UdMultiLookupResult
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxclient.utils.ContactData
import io.elixxir.xxmessengerclient.Messenger
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBindings
import io.xxlabs.messenger.data.datatype.FactType

data class UserDiscoveryWrapperBindings(
    val messenger: Messenger
) : UserDiscoveryWrapperBase {
    private val ud : UserDiscovery by lazy {
        messenger.ud!!
    }
    private val userContact: ContactWrapperBindings
        get() = ContactWrapperBindings(messenger.myContact())

    override fun search(input: String, factType: FactType, callback: (ContactWrapperBase?, String?) -> (Unit)) {
        messenger.searchContacts(
            listOf(Fact(input, factType.value)),
            object : UdSearchResultListener {
                override fun onResponse(result: Result<ContactData>) {
                    callback(
                        result.getOrNull()?.let {
                            ContactWrapperBase.from(it)
                        },
                        result.exceptionOrNull()?.message
                    )
                }
            }
        )
    }

    override fun addEmailToContact(email: String) {
        userContact.addEmail(email)
    }

    override fun addPhoneToContact(phone: String) {
        userContact.addPhone(phone)
    }

    override fun getUdEmail(raw: Boolean): String? {
        return userContact.getEmailFact(raw)
    }

    override fun getUdPhone(raw: Boolean): String? {
        return userContact.getPhoneFact(raw)
    }

    override fun registerUdEmail(email: String): String {
        val newFact = Fact(email, FactType.EMAIL.value)
        return addFactToUd(newFact)
    }

    override fun registerUdPhone(phone: String): String {
        val newFact = Fact(phone, FactType.PHONE.value)
        return addFactToUd(newFact)
    }

    override fun addFactToUd(newFact: Fact): String {
        return ud.sendRegisterFact(newFact)
    }

    override fun confirmFact(confirmationId: String, confirmationCode: String) {
        ud.confirmFact(confirmationId, confirmationCode)
    }

    override fun removeFact(factType: FactType): Boolean {
        messenger.myContact().getFactsFromContact().firstOrNull {
            it.type == factType.value
        }?.let {
            ud.removeFact(it)
            return true
        } ?: return false
    }

    override fun deleteUser(username: String) {
        userContact.getNameFact()?.takeIf {
            it == username
        }?.let {
            messenger.myContact().getFactsFromContact().firstOrNull { fact ->
                fact.fact == username
            }?.let { usernameFact ->
                ud.permanentDeleteAccount(usernameFact)
            }
        }
    }

    override fun userLookup(userId: ByteArray, callback: (ContactWrapperBase?, String?) -> Unit) {
        messenger.lookupContact(
            userId,
            object : UdLookupResultListener {
                override fun onResponse(response: Result<ContactData>) {
                    callback(
                        response.getOrNull()?.let {
                            ContactWrapperBase.from(it)
                        },
                        response.exceptionOrNull()?.message
                    )
                }
            }
        )
    }

    override fun multiLookup(ids: List<ByteArray>, callback: (List<ContactWrapperBase>?, ids: IdListBase, String?) -> Unit) {
        messenger.lookupContacts(
            ids,
            object : UdMultiLookupResultListener {
                override fun onResponse(response: Result<UdMultiLookupResult>) {
                    callback(
                        response.getOrNull()?.contacts?.map {
                            ContactWrapperBindings(it)
                        },
                        IdListBindings(
                            response.getOrNull()?.failedIds ?: listOf()
                        ),
                        response.exceptionOrNull()?.message
                    )
                }

            }
        )
    }

    override fun setAlternativeUD(ipAddress: ByteArray, cert: ByteArray, contactFile: ByteArray) {

    }

    override fun restoreNormalUD() {

    }
}
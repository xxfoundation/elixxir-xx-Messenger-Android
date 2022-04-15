package io.xxlabs.messenger.bindings.wrapper.ud

import bindings.Fact
import bindings.FactList
import bindings.IdList
import bindings.UserDiscovery
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBindings
import io.xxlabs.messenger.data.datatype.FactType
import timber.log.Timber

data class UserDiscoveryWrapperBindings(
    var userDiscovery: UserDiscovery,
    var userContact: ContactWrapperBindings
) : UserDiscoveryWrapperBase {
    override fun search(input: String, factType: FactType, callback: (ContactWrapperBase?, String?) -> (Unit)) {
        val factInput = FactList()
        try {
            factInput.add(input, factType.value)
            Fact(factType.value, input)
            userDiscovery.search(factInput.stringify(), { contacts, error ->
                val contact = if (contacts != null && contacts.len() > 0) {
                    Timber.v("Contact is not null")
                    Timber.d("ContactsList: $contacts")
                    Timber.d("Trying to print first: ${contacts[0]}")
                    Timber.d("User id: ${contacts[0].id}")
                    contacts.get(0)
                } else {
                    Timber.v("Contact is null")
                    null
                }

                Timber.d("Error is: $error")
                if (contacts != null) {
                    callback.invoke(ContactWrapperBase.from(contact), error)
                } else {
                    callback.invoke(null, error)
                }
            }, 30000)
        } catch (err: Exception) {
            callback.invoke(null, err.localizedMessage)
        }
    }

    override fun searchSingle(input: String, callback: (ContactWrapperBase?, String?) -> (Unit)) {
        try {
            Timber.v("Attaching Phone - Searching for $input")
            userDiscovery.searchSingle(input, { contacts, error ->
                val contact = if (contacts != null) {
                    Timber.v("Contact is not null")
                    Timber.d("ContactsList: $contacts")
                    Timber.d("Trying to print first: ${contacts}")
                    Timber.d("User id: ${contacts.id}")
                    contacts
                } else {
                    Timber.v("Contact is null")
                    null
                }

                Timber.d("Error is: $error")
                if (contacts != null) {
                    callback.invoke(ContactWrapperBase.from(contact), error)
                } else {
                    callback.invoke(null, error)
                }
            }, 30000)
        } catch (err: Exception) {
            Timber.e("Error: $err")
            callback.invoke(null, err.localizedMessage)
        }
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
        userDiscovery.register(username)
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
        val stringFact = newFact.stringify()
        return userDiscovery.addFact(stringFact)
    }

    override fun confirmFact(confirmationId: String, confirmationCode: String) {
        userDiscovery.confirmFact(confirmationId, confirmationCode)
    }

    override fun removeFact(factType: FactType): Boolean {
        val fact = when (factType) {
            FactType.EMAIL -> {
                getUdEmail(true)
            }
            FactType.PHONE -> {
                getUdPhone(true)
            }
            else -> {
                null
            }
        }

        Timber.v("Current user facts: $userDiscovery.")
        Timber.v("Fact to delete $fact")

        if (!fact.isNullOrBlank()) {
            userDiscovery.removeFact(fact)
            return true
        }
        return false
    }

    override fun deleteUser(username: String) {
        userDiscovery.removeUser("U$username")
    }

    override fun userLookup(userId: ByteArray, callback: (ContactWrapperBase?, String?) -> Unit) {
        userDiscovery.lookup(userId, { contact, error ->
            val wrapper = if (contact != null) {
                ContactWrapperBase.from(contact)
            } else {
                null
            }
            callback(wrapper, error)
        }, 20000)
    }

    override fun multiLookup(ids: List<ByteArray>, callback: (List<ContactWrapperBase>?, ids: IdListBase, String?) -> Unit) {
        val idList = IdList()
        ids.forEach { id ->
            idList.add(id)
        }
        Timber.v("[MULTI USER LOOKUP] UD Wrapper - Get members Multilookup")
        userDiscovery.multiLookup(idList, { contactList, idListBindings, error ->
            val contactBaseList = mutableListOf<ContactWrapperBase>()
            val idsList = IdListBindings(idListBindings)

            if (contactList != null) {
                for (i in 0 until contactList.len()) {
                    Timber.v("[GROUPS Contact] stringified: ${contactList[i].factList.stringify()}")
                    contactBaseList.add(ContactWrapperBase.from(contactList[i]))
                }
            } else {
                Timber.v("[MULTI USER LOOKUP] UD Wrapper - Error $error")
            }
            callback.invoke(contactBaseList, idsList, error)
        }, 30000)
    }

    override fun setAlternativeUD(ipAddress: ByteArray, cert: ByteArray, contactFile: ByteArray) {
        try {
            userDiscovery.setAlternativeUserDiscovery(ipAddress, cert, contactFile)
        } catch (e: Exception) {
            Timber.d("Failed to set dev UD: ${e.message}")
        }
    }

    override fun restoreNormalUD() {
        try {
            userDiscovery.unsetAlternativeUserDiscovery()
        } catch (e: Exception) {
            Timber.d("Failed to restore UD: ${e.message}")
        }
    }
}
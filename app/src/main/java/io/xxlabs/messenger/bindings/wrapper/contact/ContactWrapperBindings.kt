package io.xxlabs.messenger.bindings.wrapper.contact

import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.datatype.FactType
import timber.log.Timber

class ContactWrapperBindings(
    /*val contact: Contact*/
) : ContactWrapperBase {
    override fun getId(): ByteArray {
        TODO()
//        return contact.id
    }

    private fun addFactToFactList(fact: String, type: FactType) {
//        contact.factList.add(fact, type.value)
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
        return getFactStringfy(FactType.USERNAME, raw)
    }

    override fun getEmailFact(raw: Boolean): String {
        return getFactStringfy(FactType.EMAIL, raw)
    }

    override fun getPhoneFact(raw: Boolean): String {
        return getFactStringfy(FactType.PHONE, raw)
    }

    override fun getNameFact(raw: Boolean): String {
        return getFactStringfy(FactType.NICKNAME, raw)
    }

    override fun getFormattedPhone(): String? {
        return Country.toFormattedNumber(getPhoneFact(true))
    }

    override fun marshal(): ByteArray {
        TODO()
//        return contact.marshal()
    }

    private fun getFact(type: FactType): Nothing {
        TODO()
//        Timber.v("Facts list stringified: ${contact.factList.stringify()}")
//        try {
//            for (n in 0 until contact.factList.num()) {
//                Timber.v("Fact[$n] stringified = ${contact.factList[n].stringify()}")
//                if (contact.factList[n].get().isNotBlank()
//                    && type.value == contact.factList[n].type()
//                ) {
//                    return contact.factList[n]
//                }
//            }
//        } catch (err: Exception) {
//            err.localizedMessage
//        }
//        return null
    }

    private fun getFactStringfy(type: FactType, raw: Boolean): String {
        TODO()
//        return try {
//            Timber.v("Get fact: ${getFact(type)?.stringify()}")
//            if (raw) {
//                getFact(type)?.stringify() ?: ""
//            } else {
//                if (type == FactType.PHONE) {
//                    val rawPhone = getFact(type)?.stringify()
//
//                    if (rawPhone != null) {
//                        val country = Country.fromRawPhone(rawPhone)
//                        Timber.v("Selected country: $country")
//                        country?.dialCode + rawPhone.substring(1, rawPhone.length - 2)
//                    } else {
//                        ""
//                    }
//                } else {
//                    getFact(type)?.stringify()?.substring(1) ?: ""
//                }
//            }
//        } catch (err: Exception) {
//            Timber.e("Error retrieving fact:")
//            err.printStackTrace()
//            ""
//        }
    }

    override fun getStringifiedFacts(): String {
        TODO()
//        Timber.v("[STRINGIFY] Username ${getFact(FactType.USERNAME)?.stringify()}")
//        Timber.v("[STRINGIFY] EMAIL ${getFact(FactType.EMAIL)?.stringify()}")
//        Timber.v("[STRINGIFY] PHONE ${getFact(FactType.PHONE)?.stringify()}")
//        Timber.v("[STRINGIFY] NICKNAME ${getFact(FactType.NICKNAME)?.stringify()}")
//        return contact.factList.stringify()
    }

    override fun getDisplayName(): CharSequence {
        return if (getNameFact().isNullOrEmpty()) {
            getUsernameFact()
        } else {
            getNameFact()
        }
    }
}

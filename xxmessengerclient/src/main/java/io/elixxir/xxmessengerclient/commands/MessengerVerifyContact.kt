package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.UdLookupResultListener
import io.elixxir.xxclient.models.Contact
import io.elixxir.xxmessengerclient.MessengerEnvironment

class MessengerVerifyContact(private val env: MessengerEnvironment) {

    operator fun invoke(
        contact: Contact,
        listener: UdLookupResultListener,
    ) {
        TODO("Use legacy implementation to call the methods directly")
//        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
//        val ud = env.ud ?: throw MessengerException.NotLoaded("UD")
//        val facts = contact.getFactsFromContact()
//        var verifiedContact: Contact?
//
//        if (facts.isEmpty()) {
//            env.lookupUD(
//                e2eId = e2e.id,
//                contact = ud.contact,
//                listener = listener,
//                lookupId = contact.getIdFromContact(),
//                singleUseParams = env.getSingleUseParams()
//            )
//        }
//
//        e2e.verifyOwnership(
//            receivedContact = contact,
//            verifiedContact = TODO(),
//            e2eId = e2e.id
//        )
    }
}
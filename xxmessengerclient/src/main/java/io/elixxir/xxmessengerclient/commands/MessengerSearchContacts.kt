package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.callbacks.UdSearchResultListener
import io.elixxir.xxclient.models.Fact
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException


class MessengerSearchContacts(private val env: MessengerEnvironment) {

    operator fun invoke(
        query: List<Fact>,
        listener: UdSearchResultListener
    ) {
        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
        val ud = env.ud ?: throw MessengerException.NotLoaded("UD")

        env.searchUD(
            e2eId = e2e.id,
            udContact = ud.contactModel,
            facts = query,
            searchResultListener = listener,
            singleUseParams = env.getSingleUseParams()
        )
    }
}
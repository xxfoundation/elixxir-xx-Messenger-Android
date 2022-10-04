package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.UdSearchResultListener
import io.elixxir.xxclient.models.BindingsModel.Companion.encode
import io.elixxir.xxclient.models.Contact
import io.elixxir.xxclient.models.FactsList
import io.elixxir.xxclient.models.SingleUseReport
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.SingleUseParams
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class SearchUD(
    private val bindings: Bindings,
    private val e2eId: () -> E2eId,
    private val udContact: () -> Contact,
    private val searchResultListener: () -> UdSearchResultListener,
    private val facts: () -> FactsList,
    private val singleUseParams: () -> SingleUseParams
) {

    operator fun invoke(): Result<SingleUseReport> {
        return nonNullResultOf {
            bindings.searchUd(
                e2eId(),
                udContact(),
                searchResultListener(),
                encode(facts()),
                singleUseParams()
            )
        }
    }
}
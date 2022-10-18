package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.UdSearchResultListener
import io.elixxir.xxclient.models.BindingsModel.Companion.encodeArray
import io.elixxir.xxclient.models.Contact
import io.elixxir.xxclient.models.Fact
import io.elixxir.xxclient.models.SingleUseReport
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.SingleUseParams
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class SearchUD(private val bindings: Bindings) {

    operator fun invoke(
        e2eId: E2eId,
        udContact: Contact,
        facts: List<Fact>,
        singleUseParams: SingleUseParams,
        searchResultListener: UdSearchResultListener,
    ): Result<SingleUseReport> {
        return nonNullResultOf {
            bindings.searchUd(
                e2eId,
                udContact,
                searchResultListener,
                encodeArray(facts),
                singleUseParams
            )
        }
    }
}
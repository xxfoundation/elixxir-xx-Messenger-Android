package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.UdLookupResultListener
import io.elixxir.xxclient.models.Contact
import io.elixxir.xxclient.models.SingleUseReport
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.SingleUseParams
import io.elixxir.xxclient.utils.UserId

class LookupUD(private val bindings: Bindings) {

    operator fun invoke(
        e2eId: E2eId,
        contact: Contact,
        listener: UdLookupResultListener,
        lookupId: UserId,
        singleUseParams: SingleUseParams
    ): SingleUseReport? {
        return bindings.lookupUd(
            e2eId,
            contact,
            listener,
            lookupId,
            singleUseParams
        )
    }
}
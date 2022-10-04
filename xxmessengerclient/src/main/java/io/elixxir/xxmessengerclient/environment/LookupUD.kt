package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.UdLookupResultListener
import io.elixxir.xxclient.models.Contact
import io.elixxir.xxclient.models.SingleUseReport
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.SingleUseParams
import io.elixxir.xxclient.utils.UserId

class LookupUD(
    private val bindings: Bindings,
    private val e2eId: () -> E2eId,
    private val contact: () -> Contact,
    private val listener: () -> UdLookupResultListener,
    private val lookupId: () -> UserId,
    private val singleUseParams: () -> SingleUseParams
) {

    operator fun invoke(): SingleUseReport {
        return bindings.lookupUd(
            e2eId(),
            contact(),
            listener(),
            lookupId(),
            singleUseParams()
        )
    }
}
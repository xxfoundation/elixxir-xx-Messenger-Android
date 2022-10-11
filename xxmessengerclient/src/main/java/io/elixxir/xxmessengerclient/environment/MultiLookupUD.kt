package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.callbacks.UdMultiLookupResultListener
import io.elixxir.xxclient.models.Contact
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.SingleUseParams
import io.elixxir.xxclient.utils.UserId
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class MultiLookupUD(
    private val bindings: Bindings,
    private val e2eId: () -> E2eId,
    private val contact: () -> Contact,
    private val listener: () -> UdMultiLookupResultListener,
    private val lookupIds: () -> List<UserId>,
    private val singleUeParams: () -> SingleUseParams
) {

    operator fun invoke(): Result<Unit> {
        return nonNullResultOf {
            bindings.multiLookupUd(
                e2eId(),
                contact(),
                listener(),
                lookupIds(),
                singleUeParams()
            )
        }
    }
}
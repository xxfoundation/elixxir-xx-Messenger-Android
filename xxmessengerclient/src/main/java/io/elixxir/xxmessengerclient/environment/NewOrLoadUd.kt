package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.models.Contact
import io.elixxir.xxclient.models.NetworkFollowerStatus
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxclient.utils.Certificate
import io.elixxir.xxclient.utils.CertificateData
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.Signature
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class NewOrLoadUd(
    private val bindings: Bindings,
    private val e2eId: () -> E2eId,
    private val networkFollowerStatus: () -> NetworkFollowerStatus,
    private val username: () -> String,
    private val registrationValidationSignature: () -> Signature,
    private val certificateData: () -> CertificateData,
    private val contact: () -> Contact,
    private val udIpAddress: () -> String,
) {

    operator fun invoke(): Result<UserDiscovery> {
        return nonNullResultOf {
            bindings.getOrCreateUd(
                e2eId(),
                networkFollowerStatus(),
                username(),
                registrationValidationSignature(),
                certificateData(),
                contact(),
                udIpAddress()
            )
        }
    }
}
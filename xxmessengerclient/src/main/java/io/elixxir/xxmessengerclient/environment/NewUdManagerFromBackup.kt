package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.models.NetworkFollowerStatus
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxclient.utils.CertificateData
import io.elixxir.xxclient.utils.ContactData
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.nonNullResultOf

class NewUdManagerFromBackup(
    private val bindings: Bindings,
    private val networkFollowerStatus: () -> NetworkFollowerStatus,
    private val e2eId: () -> E2eId,
    private val certificateData: () -> CertificateData,
    private val contactData: () -> ContactData,
    private val ipAddress: () -> String
) {

    operator fun invoke(): Result<UserDiscovery> {
        return nonNullResultOf {
            bindings.newUdFromBackup(
                e2eId(),
                networkFollowerStatus(),
                certificateData(),
                contactData(),
                ipAddress()
            )
        }
    }
}
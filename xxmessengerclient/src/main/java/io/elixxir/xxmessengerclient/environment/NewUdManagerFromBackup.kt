package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.models.NetworkFollowerStatus
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxclient.utils.CertificateData
import io.elixxir.xxclient.utils.ContactData
import io.elixxir.xxclient.utils.E2eId
import io.elixxir.xxclient.utils.nonNullResultOf

class NewUdManagerFromBackup(private val bindings: Bindings) {

    operator fun invoke(
        networkFollowerStatus: NetworkFollowerStatus,
        e2eId: E2eId,
        certificateData: CertificateData,
        contactData: ContactData,
        ipAddress: String
    ): Result<UserDiscovery> {
        return nonNullResultOf {
            bindings.newUdFromBackup(
                e2eId,
                networkFollowerStatus,
                certificateData,
                contactData,
                ipAddress
            )
        }
    }
}
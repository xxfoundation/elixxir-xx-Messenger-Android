package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.models.ContactAdapter
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerLogIn(private val env: MessengerEnvironment) {

    operator fun invoke() {
        val cMix = env.cMix ?: throw MessengerException.NotLoaded("CMix")
        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")

        env.ud = env.newOrLoadUd(
            e2eId = e2e.id,
            networkFollowerStatus = cMix.getNetworkFollowerStatus(),
            username = "",
            registrationValidationSignature = byteArrayOf(),
            certificateData = env.udCert,
            contact = ContactAdapter(env.udContact),
            udIpAddress = env.udAddress
        ).getOrThrow()
    }

}
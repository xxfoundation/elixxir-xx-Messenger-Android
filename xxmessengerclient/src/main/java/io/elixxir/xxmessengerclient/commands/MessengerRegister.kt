package io.elixxir.xxmessengerclient.commands

import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxclient.models.ContactAdapter
import io.elixxir.xxclient.userdiscovery.UserDiscovery
import io.elixxir.xxmessengerclient.MessengerEnvironment
import io.elixxir.xxmessengerclient.utils.MessengerException

class MessengerRegister(private val env: MessengerEnvironment) {

    operator fun invoke(username: String) {
        val cMix = env.cMix ?: throw MessengerException.NotLoaded("CMix")
        val e2e = env.e2e ?: throw MessengerException.NotLoaded("E2E")
        env.ud = loadUd(username, cMix, e2e)
    }

    private fun loadUd(username: String, cMix: CMix, e2e: E2e): UserDiscovery =
        env.newOrLoadUd(
            e2eId = e2e.id,
            networkFollowerStatus = cMix.getNetworkFollowerStatus(),
            username = username,
            registrationValidationSignature = cMix.receptionRegistrationValidationSignature,
            certificateData = env.udCert,
            contact = ContactAdapter(env.udContact),
            udIpAddress = env.udAddress
        ).getOrThrow().apply {
            createProfile(e2e, this)
        }

    private fun createProfile(e2e: E2e, ud: UserDiscovery) {
        e2e.userProfile.setFactsOnContact(ud.facts)
    }
}
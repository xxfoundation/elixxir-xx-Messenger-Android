package io.xxlabs.messenger.bindings.wrapper.client

import bindings.*
import io.elixxir.xxclient.callbacks.AuthEventListener
import io.elixxir.xxclient.callbacks.MessageDeliveryListener
import io.elixxir.xxclient.callbacks.NetworkHealthListener
import io.elixxir.xxclient.cmix.CMix
import io.elixxir.xxclient.dummytraffic.DummyTraffic
import io.elixxir.xxclient.e2e.E2e
import io.elixxir.xxclient.models.ContactAdapter
import io.elixxir.xxmessengerclient.Messenger
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBindings
import io.xxlabs.messenger.bindings.wrapper.user.UserBase
import io.xxlabs.messenger.bindings.wrapper.user.UserBindings
import io.xxlabs.messenger.data.datatype.MsgType

class ClientWrapperBindings(
    private val messenger: Messenger
) : ClientWrapperBase {
    private val cMix: CMix by lazy {
        messenger.cMix!!
    }
    private val e2e: E2e by lazy {
        messenger.e2e!!
    }

    private val dummyTrafficManager: DummyTraffic =
        messenger.bindings.newDummyTrafficManager(
        cmixId = cMix.id,
        maxNumMessages = MAX_NUM_MESSAGES,
        avgSendDeltaMS = AVG_SEND_DELTA_MS,
        randomRangeMS = RANDOM_RANGE_MS
    )

    //Network
    override fun startNetworkFollower() {
        messenger.start
    }

    override fun stopNetworkFollower() {
        messenger.stop
    }

    override fun registerNetworkHealthCb(callback: NetworkHealthCallback) {
        cMix.setHealthListener(
            object : NetworkHealthListener {
                override fun onHealthUpdate(isHealthy: Boolean) {
                    callback.callback(isHealthy)
                }
            }
        )
    }

    override fun getNetworkFollowerStatus(): Long {
        return cMix.getNetworkFollowerStatus().code
    }

    override fun isNetworkHealthy(): Boolean {
        return cMix.isNetworkHealthy()
    }

    //Messaging
    override fun registerMessageListener(messageReceivedListener: MessageReceivedListener) {
        messenger.registerMessageListener(messageReceivedListener)
    }

    override fun registerAuthCallback(
        registerAuthCallback: (contact: ByteArray) -> Unit,
        authConfirmCallback: (contact: ByteArray) -> Unit,
        authResetCallback: (contact: ByteArray) -> Unit
    ) {
        messenger.registerAuthCallbacks(
            object : AuthEventListener {
                override fun onConfirm(
                    contact: ByteArray?,
                    receptionId: ByteArray?,
                    ephemeralId: Long,
                    roundId: Long
                ) {
                    contact?.let { authConfirmCallback(it) }
                }

                override fun onRequest(
                    contact: ByteArray?,
                    receptionId: ByteArray?,
                    ephemeralId: Long,
                    roundId: Long
                ) {
                    contact?.let { registerAuthCallback(it) }
                }

                override fun onReset(
                    contact: ByteArray?,
                    receptionId: ByteArray?,
                    ephemeralId: Long,
                    roundId: Long
                ) {
                    contact?.let { authResetCallback(it) }
                }
            }
        )
    }

    override fun sendE2E(
        recipientId: ByteArray,
        payload: ByteArray,
        msgType: MsgType
    ): SendReportBase? {
        return messenger.sendMessage(
            recipientId,
            payload
        )?.let {
            SendReportBindings(it)
        }
    }

    override fun requestAuthenticatedChannel(
        marshalledRecipient: ByteArray,
        marshalledUser: ByteArray
    ): Long {
        return e2e.requestAuthenticatedChannel(
            ContactAdapter(marshalledRecipient),
            ContactAdapter(marshalledUser).getFactsFromContact()
        )
    }

    override fun confirmAuthenticatedChannel(marshalledContact: ByteArray): Long {
        return e2e.confirmReceivedRequest(
            ContactAdapter(marshalledContact)
        )
    }

    override fun waitForRoundCompletion(
        roundId: Long,
        timeoutMillis: Long,
        onRoundCompletionCallback: ((Long, Boolean, Boolean) -> Unit)
    ) {
        TODO("Wait for round completion")
    }

    override fun waitForMessageDelivery(
        sentReport: ByteArray,
        timeoutMillis: Long,
        onMessageDeliveryCallback: ((ByteArray, Boolean, Boolean, ByteArray) -> Unit)
    ) {
        cMix.waitForRoundResult(
            sentReport,
            timeoutMillis,
            object : MessageDeliveryListener {
                override fun onMessageSent(
                    delivered: Boolean,
                    timedOut: Boolean,
                    roundResults: ByteArray?
                ) {
                    onMessageDeliveryCallback(
                        byteArrayOf(),
                        delivered,
                        timedOut,
                        roundResults ?: byteArrayOf()
                    )
                }

            }
        )
    }

    override fun registerForNotifications(token: String) {
        messenger.registerForNotifications(token)
    }

    override fun unregisterForNotifications() {
        messenger.unregisterForNotifications
    }

    override fun getUser(): UserBase {
        return UserBindings(messenger)
    }

    override fun getUserId(): ByteArray {
        return getUser().getReceptionId()
    }

    override fun getPreImages(): String {
        TODO()
//        return client.getPreimages(client.user.receptionID)
    }

    override fun verifyOwnership(receivedContact: ByteArray, verifiedContact: ByteArray): Boolean =
        e2e.verifyOwnership(
            ContactAdapter(receivedContact),
            ContactAdapter(verifiedContact),
            e2e.id
        )

    override fun enableDummyTraffic(enabled: Boolean) {
        dummyTrafficManager.enabled = enabled
    }

    companion object {
        // Cover Traffic "Dummy Traffic"
        private const val MAX_NUM_MESSAGES = 5L
        private const val AVG_SEND_DELTA_MS = 30_000L
        private const val RANDOM_RANGE_MS  = 25_000L
    }
}
package io.xxlabs.messenger.bindings.wrapper.client

import bindings.*
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.user.UserBase
import io.xxlabs.messenger.bindings.wrapper.user.UserBindings
import io.xxlabs.messenger.data.datatype.MsgType
import timber.log.Timber

class ClientWrapperBindings(
    /*override var client: Client*/
) : ClientWrapperBase {

    private val dummyTrafficManager: DummyTraffic = TODO()
//    Bindings.newDummyTrafficManager(
//        client,
//        MAX_NUM_MESSAGES,
//        AVG_SEND_DELTA_MS,
//        RANDOM_RANGE_MS
//    )

    //Network
    override fun startNetworkFollower() {
        Timber.v("Starting Network Follower....")
        TODO()
//        client.startNetworkFollower(10000)
    }

    override fun stopNetworkFollower() {
        Timber.v("Stopping Network Follower...")
        TODO()
//        client.stopNetworkFollower()
    }

    override fun registerNetworkHealthCb(callback: NetworkHealthCallback) {
        Timber.v("Registering network callback...")
        TODO()
//        client.registerNetworkHealthCB(callback)
    }

    override fun getNetworkFollowerStatus(): Long {
        TODO()
//        return client.networkFollowerStatus()
    }

    override fun isNetworkHealthy(): Boolean {
        TODO()
//        return client.isNetworkHealthy
    }

    //  0x00 - PENDING (Never seen by client)
    //  0x01 - PRECOMPUTING
    //  0x02 - STANDBY
    //  0x03 - QUEUED
    //  0x04 - REALTIME
    //  0x05 - COMPLETED
    //  0x06 - FAILED
    override fun registerDatadogListener() { }

    //Messaging
    override fun registerMessageListener(messageReceivedListener: MessageReceivedListener) {
        TODO()
//        val zeroUser = ByteArray(33) { 0 }
//        zeroUser[32] = 3
//        client.registerListener(
//            zeroUser,
//            MsgType.TEXT_MESSAGE.value.toLong(),
//            messageReceivedListener
//        )
    }

    override fun registerAuthCallback(
        registerAuthCallback: (contact: ByteArray) -> Unit,
        authConfirmCallback: ((contact: ByteArray) -> Unit),
        authResetCallback: ((contact: ByteArray) -> Unit)
    ) {
        TODO()
//        client.registerAuthCallbacks(
//            { contact -> registerAuthCallback.invoke(contact.marshal()) },
//            { contact -> authConfirmCallback.invoke(contact.marshal()) },
//            { contact -> authResetCallback(contact.marshal())}
//        )
    }

    override fun sendUnsafe(
        recipientId: ByteArray,
        payload: ByteArray,
        msgType: MsgType
    ): RoundListBase? {
        TODO()
//        return try {
//            Timber.v("Send Message TYPE: $msgType")
//            val round =
//                client.sendUnsafe(recipientId, payload, msgType.value.toLong(), "")
//            Timber.v("Successfully sent message to server :: round ${round.get(0)}")
//            RoundListBindings(round)
//        } catch (e: Exception) {
//            Timber.e(e, "Failed sending a message")
//            null
//        }
    }

    override fun sendCmix(
        recipientId: ByteArray,
        payload: String
    ): Boolean {
        TODO()
//        return try {
//            val round =
//                client.sendCmix(recipientId, payload.toByteArray(), "")
//            Timber.v("Successfully sent message to CMIX :: round $round")
//            true
//        } catch (e: Exception) {
//            Timber.e(e, "Failed sending a message CMIX")
//            false
//        }
    }

    override fun sendE2E(
        recipientId: ByteArray,
        payload: ByteArray,
        msgType: MsgType
    ): SendReportBase? {
        TODO()
//        return try {
//            Timber.v("Send Message TYPE (E2E): $msgType")
//            val sendReport =
//                client.sendE2E(recipientId, payload, msgType.value.toLong(), "")
//            Timber.v("Successfully sent message id ${sendReport.messageID.toBase64String()} to server :: round ${sendReport.roundList}")
//            SendReportBindings(sendReport)
//        } catch (e: Exception) {
//            Timber.e(e.localizedMessage)
//            null
//        }
    }

    override fun requestAuthenticatedChannel(
        marshalledRecipient: ByteArray,
        marshalledUser: ByteArray
    ): Long {
        TODO()
//        return client.requestAuthenticatedChannel(marshalledRecipient, marshalledUser, "")
    }

    override fun confirmAuthenticatedChannel(marshalledContact: ByteArray): Long {
        TODO()
//        return client.confirmAuthenticatedChannel(marshalledContact)
    }

    override fun waitForRoundCompletion(
        roundId: Long,
        timeoutMillis: Long,
        onRoundCompletionCallback: ((Long, Boolean, Boolean) -> Unit)
    ) {
        TODO()
//        client.waitForRoundCompletion(roundId, object : RoundCompletionCallback {
//            override fun eventCallback(roundId: Long, success: Boolean, timedOut: Boolean) {
//                onRoundCompletionCallback(roundId, success, timedOut)
//            }
//
//        }, timeoutMillis)
    }

    override fun waitForMessageDelivery(
        sentReport: ByteArray,
        timeoutMillis: Long,
        onMessageDeliveryCallback: ((ByteArray, Boolean, Boolean, ByteArray) -> Unit)
    ) { //msgID []byte, delivered, timedOut bool, roundResults []byte
        TODO()
//        client.waitForMessageDelivery(sentReport, object : MessageDeliveryCallback {
//            override fun eventCallback(
//                msgId: ByteArray,
//                delivered: Boolean,
//                timedOut: Boolean,
//                roundResults: ByteArray
//            ) {
//                onMessageDeliveryCallback(msgId, delivered, timedOut, roundResults)
//            }
//
//        }, timeoutMillis)
    }

    override fun registerForNotifications(token: String) {
        TODO()
//        client.registerForNotifications(token)
    }

    fun getPreferredBins(countryCode: String): String {
        TODO()
//        return client.getPreferredBins(countryCode)
    }

    fun setProxiedBins(bins: String) {
        TODO()
//        client.setProxiedBins(bins)
    }

    override fun unregisterForNotifications() {
        TODO()
//        client.unregisterForNotifications()
    }

    override fun getUser(): UserBase {
        TODO()
//        return UserBindings(client.user)
    }

    override fun getUserId(): ByteArray {
        return getUser().getReceptionId()
    }

    override fun getPreImages(): String {
        TODO()
//        return client.getPreimages(client.user.receptionID)
    }

    override fun verifyOwnership(receivedContact: ByteArray, verifiedContact: ByteArray): Boolean =
        TODO()
//        client.verifyOwnership(receivedContact, verifiedContact)

    override fun enableDummyTraffic(enabled: Boolean) {
        dummyTrafficManager.status = enabled
    }

    override fun getPartners(): ByteArray = TODO("client.partners")

    fun getNodeRegistrationStatus(): Pair<Long, Long> {
        TODO()
//        val registeredNodes = client.nodeRegistrationStatus.registered
//        val totalNodes = client.nodeRegistrationStatus.total
//        Timber.v("[NODE REGISTRATION STATUS] Registered: $registeredNodes, Total: $totalNodes")
//        return Pair(registeredNodes, totalNodes)
    }

    companion object {
        // Cover Traffic "Dummy Traffic"
        private const val MAX_NUM_MESSAGES = 5L
        private const val AVG_SEND_DELTA_MS = 30_000L
        private const val RANDOM_RANGE_MS  = 25_000L
    }
}
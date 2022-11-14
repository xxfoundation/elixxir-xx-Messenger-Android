package io.xxlabs.messenger.bindings.wrapper.client

import bindings.Client
import bindings.NetworkHealthCallback
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.report.SendReportMock
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListMock
import io.xxlabs.messenger.bindings.wrapper.user.UserBase
import io.xxlabs.messenger.bindings.wrapper.user.UserMock
import io.xxlabs.messenger.data.datatype.MsgType
import io.xxlabs.messenger.data.datatype.NetworkFollowerStatus
import io.xxlabs.messenger.data.room.model.ContactData
import timber.log.Timber
import kotlin.random.Random

class ClientWrapperMock(val contact: ContactData) : ClientWrapperBase {
    override val client: Client = Client()

    //Network
    override fun startNetworkFollower() {
        Timber.v("Starting Network Follower....")
    }

    override fun stopNetworkFollower() {
        Timber.v("Stopping Network Follower...")
    }

    override fun registerNetworkHealthCb(callback: NetworkHealthCallback) {
        Timber.v("Registering network callback...")
    }

    override fun getNetworkFollowerStatus(): Long {
        return NetworkFollowerStatus.RUNNING.value
    }

    override fun isNetworkHealthy(): Boolean {
        return true
    }

    override fun registerDatadogListener() {

    }

    //Messaging
    override fun registerMessageListener(messageReceivedListener: MessageReceivedListener) {

    }

    override fun registerAuthCallback(
        registerAuthCallback: (contact: ByteArray) -> Unit,
        authConfirmCallback: (contact: ByteArray) -> Unit,
        authResetCallback: (contact: ByteArray) -> Unit
    ) {}

    override fun sendUnsafe(
        recipientId: ByteArray,
        payload: ByteArray,
        msgType: MsgType
    ): RoundListBase {
        return RoundListMock()
    }

    override fun sendCmix(
        recipientId: ByteArray,
        payload: String
    ): Boolean {
        return true
    }

    override fun sendE2E(
        recipientId: ByteArray,
        payload: ByteArray,
        msgType: MsgType
    ): SendReportBase {
        return SendReportMock()
    }

    override fun requestAuthenticatedChannel(
        marshalledRecipient: ByteArray,
        marshalledUser: ByteArray
    ): Long {
        return Random.nextLong()
    }

    override fun confirmAuthenticatedChannel(marshalledContact: ByteArray): Long {
        return Random.nextLong()
    }

    override fun deleteRequest(marshalledContact: ByteArray) {}

    override fun waitForRoundCompletion(
        roundId: Long,
        timeoutMillis: Long,
        onRoundCompletionCallback: ((Long, Boolean, Boolean) -> Unit)
    ) {
        onRoundCompletionCallback(roundId, true, false)
    }

    override fun waitForMessageDelivery(
        sentReport: ByteArray,
        timeoutMillis: Long,
        onMessageDeliveryCallback: ((ByteArray, Boolean, Boolean, ByteArray) -> Unit)
    ) { //msgID []byte, delivered, timedOut bool, roundResults []byte
        onMessageDeliveryCallback("aaaa".toByteArray(), true, false, Random.nextBytes(32))
    }

    override fun registerForNotifications(token: String) {}

    override fun unregisterForNotifications() {}

    override fun getUser(): UserBase {
        return UserMock(contact)
    }

    override fun getUserId(): ByteArray {
        return getUser().getReceptionId()
    }

    override fun getPreImages(): String {
        TODO("Not yet implemented")
    }

    override fun getPartners(): ByteArray = byteArrayOf()

    override fun verifyOwnership(receivedContact: ByteArray, verifiedContact: ByteArray) = true
    override fun enableDummyTraffic(enabled: Boolean) { }
}
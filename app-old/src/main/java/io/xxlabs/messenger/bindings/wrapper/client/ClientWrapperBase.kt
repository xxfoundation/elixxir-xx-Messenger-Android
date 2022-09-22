package io.xxlabs.messenger.bindings.wrapper.client

//import bindings.Client
import bindings.NetworkHealthCallback
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.user.UserBase
import io.xxlabs.messenger.data.datatype.MsgType

interface ClientWrapperBase {
//    val client: Client

    //Network
    fun startNetworkFollower()
    fun stopNetworkFollower()
    fun registerNetworkHealthCb(callback: NetworkHealthCallback)
    fun getNetworkFollowerStatus(): Long
    fun isNetworkHealthy(): Boolean

    //Datadog
    fun registerDatadogListener()

    //Messaging
    fun registerMessageListener(messageReceivedListener: MessageReceivedListener)
    fun registerAuthCallback(
        registerAuthCallback: (contact: ByteArray) -> Unit,
        authConfirmCallback: (contact: ByteArray) -> Unit,
        authResetCallback: (contact: ByteArray) -> Unit
    )

    fun sendUnsafe(
        recipientId: ByteArray,
        payload: ByteArray,
        msgType: MsgType
    ): RoundListBase?

    fun sendCmix(
        recipientId: ByteArray,
        payload: String
    ): Boolean

    fun sendE2E(
        recipientId: ByteArray,
        payload: ByteArray,
        msgType: MsgType
    ): SendReportBase?

    fun requestAuthenticatedChannel(
        marshalledRecipient: ByteArray,
        marshalledUser: ByteArray
    ): Long

    fun confirmAuthenticatedChannel(marshalledContact: ByteArray): Long
    fun waitForRoundCompletion(
        roundId: Long,
        timeoutMillis: Long,
        onRoundCompletionCallback: (Long, Boolean, Boolean) -> Unit
    )

    fun waitForMessageDelivery(
        sentReport: ByteArray,
        timeoutMillis: Long,
        onMessageDeliveryCallback: (ByteArray, Boolean, Boolean, ByteArray) -> Unit
    )

    fun registerForNotifications(token: String)
    fun unregisterForNotifications()
    fun getUser(): UserBase
    fun getUserId(): ByteArray
    fun getPreImages(): String

    fun verifyOwnership(receivedContact: ByteArray, verifiedContact: ByteArray): Boolean
    fun enableDummyTraffic(enabled: Boolean)

    fun getPartners(): ByteArray
}
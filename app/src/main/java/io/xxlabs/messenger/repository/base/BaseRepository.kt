package io.xxlabs.messenger.repository.base

import bindings.NetworkHealthCallback
import io.reactivex.Maybe
import io.reactivex.Single
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.bindings.wrapper.groups.message.GroupMessageReceiveBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.GroupSendReportBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportBase
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.datatype.NetworkFollowerStatus
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.filetransfer.FileTransferRepository

interface BaseRepository {
    fun loginSingle(storageDir: String, password: ByteArray): Single<ByteArray>
    fun startNetworkFollower(): Single<Boolean>
    fun stopNetworkFollower(): Single<Boolean>
    fun getNetworkFollowerStatus(): NetworkFollowerStatus
    fun newUserDiscovery(): Single<Boolean>
    fun isLoggedIn(): Single<Boolean>

    @Throws(Exception::class)
    fun areNodesReady(): Boolean

    //Notifications =========================================================================
    fun registerNotificationsToken(): Single<String>
    fun unregisterForNotification(): Single<Boolean>

    //  User =================================================================================
    fun getUserId(): ByteArray
    fun getStoredUsername(): String
    fun getStoredEmail(): String
    fun getStoredPhone(): String
    fun getMashalledUser(): ByteArray
    fun deleteUser(): Single<Boolean>

    //  Callbacks ============================================================================
    fun registerAuthCallback(
        onContactReceived: ((contact: ByteArray) -> Unit),
        onConfirmationReceived: ((contact: ByteArray) -> Unit),
        onResetReceived: ((contact: ByteArray) -> Unit)
    ): Single<Boolean>

    fun registerNetworkHealthCb(networkHealthCallback: NetworkHealthCallback): Single<Boolean>

    //  Rounds ===============================================================================
    fun waitForRoundCompletion(
        roundId: Long,
        onRoundCompletionCallback: (Long, Boolean, Boolean) -> Unit,
        timeoutMillis: Long
    ): Single<Boolean>

    fun waitForMessageDelivery(
        sendReport: ByteArray,
        onMessageDeliveryCallback: (ByteArray, Boolean, Boolean, ByteArray) -> Unit,
        timeoutMillis: Long
    )

    //  Listeners ============================================================================
    fun registerMessageListener(): Single<Boolean>

    //  Ud ====================================================================================
    fun searchUd(input: String, type: FactType): Single<Pair<ContactWrapperBase?, String?>>
    fun searchUd(input: String, type: FactType, callback: (ContactWrapperBase?, String?) -> (Unit))
    fun getUdEmail(raw: Boolean = false): String?
    fun getUdPhone(raw: Boolean = false): String?
    fun registerUdPhone(phone: String): Single<String>
    fun registerUdEmail(email: String): Single<String>
    fun confirmFact(
        confirmationId: String,
        confirmationCode: String,
        fact: String,
        isEmailCode: Boolean
    ): Single<String>

    fun removeFact(factType: FactType): Single<Boolean>
    fun removeFactExclusive(factType: FactType): Pair<Boolean, Throwable?>

    //  Contact ===============================================================================
    fun deleteContact(marshalledContact: ByteArray): Single<ByteArray>
    fun unmarshallContact(rawData: ByteArray): ContactWrapperBase?
    fun getContactWrapper(contact: ByteArray): ContactWrapperBase
    fun requestAuthenticatedChannel(marshalledRecipient: ByteArray): Single<Long>
    fun confirmAuthenticatedChannel(data: ByteArray): Single<Long>
    fun verifyOwnership(receivedContact: Contact, verifiedContact: ContactWrapperBase): Boolean

    //  Groups ===============================================================================

    fun getGroupData(groupId: ByteArray): Single<GroupData>

    fun makeGroup(
        name: String,
        idsList: List<ByteArray>,
        initialMessage: String?
    ): Single<NewGroupReportBase>

    fun resendInvite(
        groupId: ByteArray
    ): Single<NewGroupReportBase>

    fun resendInviteLocal(
        groupId: ByteArray
    ): NewGroupReportBase

    fun initGroupManager(
        onGroupReceived: (GroupBase) -> (Unit),
        onMessageReceived: (GroupMessageReceiveBase) -> (Unit)
    )

    fun sendGroupMessage(
        senderId: ByteArray,
        recipientId: ByteArray,
        groupId: ByteArray,
        payload: String
    ): Maybe<GroupSendReportBase>

    fun acceptGroupInvite(serializedGroup: ByteArray): Single<Boolean>
    fun rejectGroupInvite(serializedGroup: ByteArray): Single<Boolean>
    fun leaveGroup(serializedGroup: ByteArray): Single<Boolean>
    fun getMembersUsername(
        ids: List<ByteArray>,
        callback: (List<ContactWrapperBase>?, ids: IdListBase, String?) -> Unit
    )

    fun userLookup(
        userId: ByteArray,
        callback: (ContactWrapperBase?, String?) -> Unit
    )

    fun userDbLookup(userId: ByteArray): Maybe<ContactData>

    fun sendViaClientE2E(
        senderId: ByteArray,
        recipientId: ByteArray,
        payload: String
    ): Maybe<SendReportBase>

    fun unmarshallSendReport(marshalledReport: ByteArray): SendReportBase

    fun enableDummyTraffic(enabled: Boolean)

    // File Transfer
    val fileRepository: FileTransferRepository

    fun replayRequests()

    suspend fun getPartners(): List<String>
}
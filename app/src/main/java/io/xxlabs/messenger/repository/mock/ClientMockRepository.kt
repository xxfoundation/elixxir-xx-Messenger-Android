package io.xxlabs.messenger.repository.mock

import android.content.Context
import bindings.NetworkHealthCallback
import com.google.gson.Gson
import io.reactivex.Maybe
import io.reactivex.Single
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperMock
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.bindings.wrapper.groups.message.GroupMessageReceiveBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.GroupSendReportBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.GroupSendReportMock
import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportMock
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.report.SendReportMock
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListMock
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.datatype.MsgType
import io.xxlabs.messenger.data.datatype.NetworkFollowerStatus
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.filetransfer.FileTransferRepository
import io.xxlabs.messenger.filetransfer.MockFileTransferManager
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import timber.log.Timber
import java.util.*
import kotlin.random.Random

class ClientMockRepository(
    val preferences: PreferencesRepository
) : BaseRepository {

    override fun loginSingle(storageDir: String, password: ByteArray): Single<ByteArray> {
        preferences.setUserId(preferences.name.toByteArray())
        isLoggedIn = true
        return Single.just(preferences.getUserId())
    }

    override fun startNetworkFollower(): Single<Boolean> {
        return Single.create {
            it.onSuccess(true)
        }
    }

    override fun stopNetworkFollower(): Single<Boolean> {
        return Single.create {
            it.onSuccess(true)
        }
    }

    override fun getNetworkFollowerStatus(): NetworkFollowerStatus {
        return if (!XxMessengerApplication.isUserDiscoveryRunning) {
            NetworkFollowerStatus.STOPPED
        } else {
            NetworkFollowerStatus.RUNNING
        }
    }

    override fun newUserDiscovery(): Single<Boolean> {
        return Single.create { emitter -> emitter.onSuccess(true) }
    }

    override fun isLoggedIn(): Single<Boolean> {
        return Single.create { emitter ->
            emitter.onSuccess(isLoggedIn)
        }
    }

    override fun areNodesReady(): Boolean {
        return true
    }

    override fun registerNotificationsToken(): Single<String> {
        return Single.create { emitter ->
            emitter.onSuccess("12345")
        }
    }

    override fun unregisterForNotification(): Single<Boolean> {
        return Single.create { emitter ->
            emitter.onSuccess(false)
        }
    }

    //  User =================================================================================
    override fun getMashalledUser(): ByteArray {
        return "1234567812345678123456781234567812345678".toByteArray()
    }

    override fun getUserId(): ByteArray {
        return "12345678".toByteArray()
    }

    override fun deleteUser(): Single<Boolean> {
        return Single.just(true)
    }

    override fun getStoredUsername(): String {
        val factList = importUserFactsHash()
        return factList[FactType.USERNAME]?.substring(1) ?: ""
    }

    override fun getStoredEmail(): String {
        val factList = importUserFactsHash()
        return factList[FactType.EMAIL]?.substring(1) ?: ""
    }

    override fun getStoredPhone(): String {
        val factList = importUserFactsHash()
        return factList[FactType.PHONE]?.substring(1) ?: ""
    }

    //  Callbacks ============================================================================
    override fun registerAuthCallback(
        onContactReceived: (contact: ByteArray) -> Unit,
        onConfirmationReceived: (contact: ByteArray) -> Unit,
        onResetReceived: (contact: ByteArray) -> Unit
    ): Single<Boolean> {
        return Single.create {
            it.onSuccess(true)
        }
    }

    override fun registerNetworkHealthCb(networkHealthCallback: NetworkHealthCallback): Single<Boolean> {
        return Single.create {
            it.onSuccess(true)
        }
    }

    override fun waitForRoundCompletion(
        roundId: Long,
        onRoundCompletionCallback: (Long, Boolean, Boolean) -> Unit,
        timeoutMillis: Long
    ): Single<Boolean> {
        return Single.create {
            it.onSuccess(true)
        }
    }

    override fun waitForMessageDelivery(
        sendReport: ByteArray,
        onMessageDeliveryCallback: (ByteArray, Boolean, Boolean, ByteArray) -> Unit,
        timeoutMillis: Long
    ) {
        onMessageDeliveryCallback.invoke(sendReport, true, false, byteArrayOf())
    }

    //  Listeners ============================================================================
    override fun registerMessageListener(): Single<Boolean> {
        return Single.create { emitter ->
            Timber.v("FRIEND REQUEST NUM: ${MsgType.FRIEND_REQUEST.value}")
            Timber.v("TEXT MSG NUM: ${MsgType.TEXT_MESSAGE.value}")
            Timber.v("DELETE REQUEST NUM: ${MsgType.DELETE_REQUEST.value}")
            emitter.onSuccess(true)
        }
    }

    override fun getUdEmail(raw: Boolean): String {
        val factList = importUserFactsHash()
        return factList[FactType.EMAIL] ?: ""
    }

    override fun getUdPhone(raw: Boolean): String {
        val factList = importUserFactsHash()
        return factList[FactType.PHONE] ?: ""
    }

    private fun importUserContactMock(): List<String>? {
        val userData = preferences.userData
        Timber.v("[MOCK REPO] User data: $userData")
        if (userData.isBlank() || userData.isEmpty()) {
            return null
        }

        var splitData: List<String> = userData.split(',', ';')
        splitData = splitData.filter { fact ->
            fact.isNotBlank()
        }
        Timber.v("[MOCK REPO] Split data: $splitData")

        return splitData
    }

    private fun importUserFactsHash(): HashMap<FactType, String?> {
        val factsList = importUserContactMock()
        val hashmap = HashMap<FactType, String?>()
        hashmap[FactType.USERNAME] = null
        hashmap[FactType.EMAIL] = null
        hashmap[FactType.PHONE] = null

        factsList?.forEach { fact ->
            when {
                fact[0] == 'U' -> {
                    hashmap[FactType.USERNAME] = fact.substring(1)
                }
                fact[0] == 'E' -> {
                    hashmap[FactType.EMAIL] = fact.substring(1)
                }
                fact[0] == 'P' -> {
                    hashmap[FactType.PHONE] = fact.substring(1)
                }
            }
        }

        return hashmap
    }

    //  Udb ==================================================================================
    override fun searchUd(
        input: String,
        type: FactType
    ): Single<Pair<ContactWrapperBase?, String?>> {
        return Single.create { emitter ->
            val cleanInput: String
            val status: RequestStatus = when {
                input.contains("_SENT") -> {
                    cleanInput = input.substringBefore("_SENT")
                    RequestStatus.SENT
                }
                input.contains("_RECEIVED") -> {
                    cleanInput = input.substringBefore("_RECEIVED")
                    RequestStatus.VERIFIED
                }
                input.contains("_FAILED") -> {
                    cleanInput = input.substringBefore("_FAILED")
                    RequestStatus.SEND_FAIL
                }

                input.contains("_ACCEPTED") -> {
                    cleanInput = input.substringBefore("_ACCEPTED")
                    RequestStatus.ACCEPTED
                }

                else -> {
                    cleanInput = input
                    RequestStatus.ACCEPTED
                }
            }

            if (cleanInput.length < 4) {
                emitter.onSuccess(Pair(null, "NO RESULTS FOUND"))
            } else {
                val contact = ContactData()
                contact.status = status.value
                contact.userId = Random.nextBytes(32)
                val mockWrapper = ContactWrapperMock(contact)

                when (type) {
                    FactType.EMAIL -> {
                        mockWrapper.addUsername(cleanInput)
                        mockWrapper.addEmail(cleanInput)
                    }
                    FactType.PHONE -> {
                        mockWrapper.addUsername(cleanInput)
                        mockWrapper.addPhone(cleanInput)
                    }
                    FactType.USERNAME -> {
                        mockWrapper.addUsername(cleanInput)
                    }
                }

                emitter.onSuccess(Pair(mockWrapper, null))
            }
        }
    }

    override fun searchUd(
        input: String,
        type: FactType,
        callback: (ContactWrapperBase?, String?) -> Unit
    ) {
        val cleanInput: String
        val status: RequestStatus = when {
            input.contains("_SENT") -> {
                cleanInput = input.substringBefore("_SENT")
                RequestStatus.SENT
            }

            input.contains("_RECEIVED") -> {
                cleanInput = input.substringBefore("_RECEIVED")
                RequestStatus.VERIFIED
            }

            input.contains("_FAILED") -> {
                cleanInput = input.substringBefore("_FAILED")
                RequestStatus.SEND_FAIL
            }

            input.contains("_ACCEPTED") -> {
                cleanInput = input.substringBefore("_ACCEPTED")
                RequestStatus.ACCEPTED
            }

            else -> {
                cleanInput = input
                RequestStatus.ACCEPTED
            }
        }

        if (cleanInput.length < 5) {
            callback.invoke(null, "NO RESULTS FOUND")
        } else {
            val contact = ContactData()
            contact.status = status.value
            contact.userId = Random.nextBytes(32)
            val mockWrapper = ContactWrapperMock(contact)

            when (type) {
                FactType.EMAIL -> {
                    mockWrapper.addUsername(cleanInput)
                    mockWrapper.addEmail(cleanInput)
                }
                FactType.PHONE -> {
                    mockWrapper.addUsername(cleanInput)
                    mockWrapper.addPhone(cleanInput)
                }
                FactType.USERNAME -> {
                    mockWrapper.addUsername(cleanInput)
                }
            }
            mockWrapper.addName(cleanInput)
            callback.invoke(mockWrapper, null)
        }
    }

    override fun registerUdPhone(phone: String): Single<String> {
        val factsList = importUserFactsHash()
        factsList[FactType.PHONE] = "P$phone"
        exportUserContact(factsList)
        return Single.create { emitter -> emitter.onSuccess(phone) }
    }

    override fun registerUdEmail(email: String): Single<String> {
        val factsList = importUserFactsHash()
        factsList[FactType.EMAIL] = "E$email"
        exportUserContact(factsList)
        return Single.create { emitter -> emitter.onSuccess(email) }
    }

    override fun confirmFact(
        confirmationId: String,
        confirmationCode: String,
        fact: String,
        isEmailCode: Boolean
    ): Single<String> {
        val import = importUserFactsHash()
        if (isEmailCode) {
            import[FactType.EMAIL] = fact
        } else {
            import[FactType.PHONE] = fact
        }
        exportUserContact(import)
        return Single.create { emitter -> emitter.onSuccess(confirmationCode) }
    }

    override fun removeFact(factType: FactType): Single<Boolean> {
        val import = importUserFactsHash()
        import.remove(factType)
        return Single.create { emitter -> emitter.onSuccess(true) }
    }

    override fun removeFactExclusive(factType: FactType): Pair<Boolean, Throwable?> {
        val import = importUserFactsHash()
        import.remove(factType)
        return Pair(true, null)
    }

    private fun exportUserContact(factsList: HashMap<FactType, String?>) {
        val facts = importUserFactsHash()
        Timber.d("[MOCK REPO] exporting user facts: $facts")
        var buildString = "U${factsList[FactType.USERNAME]}" ?: ""
        val email = "E${factsList[FactType.EMAIL]}"
        val phone = "P${factsList[FactType.PHONE]}"

        if (!email.isNullOrBlank()) {
            buildString += ",$email"
        }

        if (!phone.isNullOrBlank()) {
            buildString += ",$phone"
        }

        buildString += ";"
        preferences.userData = buildString
    }

    //  Contact ===============================================================================
    override fun deleteContact(marshalledContact: ByteArray): Single<ByteArray> {
        return Single.create { emitter ->
            emitter.onSuccess(marshalledContact)
        }
    }

    override fun unmarshallContact(rawData: ByteArray): ContactWrapperBase {
        return ContactWrapperBase.from(rawData)
    }

    override fun getContactWrapper(contact: ByteArray): ContactWrapperBase {
        val contactObj = ContactData()
        return ContactWrapperMock(contactObj)
    }

    override fun requestAuthenticatedChannel(marshalledRecipient: ByteArray): Single<Long> {
        return Single.create { emitter -> emitter.onSuccess(1) }
    }

    override fun confirmAuthenticatedChannel(data: ByteArray): Single<Long> {
        return Single.create { emitter -> emitter.onSuccess(1) }
    }

    override fun getGroupData(groupId: ByteArray): Single<GroupData> {
        return Single.create { emitter -> emitter.onSuccess(GroupData(name="Mock Group"))}
    }

    override fun makeGroup(
        name: String,
        idsList: List<ByteArray>,
        initialMessage: String?
    ): Single<NewGroupReportBase> {
        return Single.create { emitter ->
            val group = GroupData(
                name = name,
                groupId = UUID.randomUUID().toString().encodeToByteArray()
            )
            Timber.v("[MAKE GROUP MOCK] $group")
            idsList.toMutableList().add(0, preferences.getUserId())
            emitter.onSuccess(NewGroupReportMock(group, idsList))
        }
    }

    override fun resendInvite(groupId: ByteArray): Single<NewGroupReportBase> {
        return Single.create { emitter ->
            emitter.onSuccess(
                NewGroupReportMock(
                    GroupData(name = ""),
                    listOf()
                )
            )
        }
    }

    override fun resendInviteLocal(groupId: ByteArray): NewGroupReportBase {
        return NewGroupReportMock(GroupData(name = ""), listOf())
    }

    override fun initGroupManager(
        onGroupReceived: (GroupBase) -> Unit,
        onMessageReceived: (GroupMessageReceiveBase) -> Unit
    ) {

    }

    override fun sendGroupMessage(
        senderId: ByteArray,
        recipientId: ByteArray,
        groupId: ByteArray,
        payload: String
    ): Maybe<GroupSendReportBase> {
        return Maybe.just(GroupSendReportMock())
    }

    override fun acceptGroupInvite(serializedGroup: ByteArray): Single<Boolean> {
        return Single.just(true)
    }

    override fun rejectGroupInvite(serializedGroup: ByteArray): Single<Boolean> {
        return Single.just(true)
    }

    override fun leaveGroup(serializedGroup: ByteArray): Single<Boolean> {
        return Single.just(true)
    }

    override fun getMembersUsername(
        ids: List<ByteArray>,
        callback: (List<ContactWrapperBase>?, ids: IdListBase, String?) -> Unit
    ) {
        Timber.v("[MULTI USER LOOKUP] Mock Repo - Get members Multilookup")
    }

    override fun userLookup(userId: ByteArray, callback: (ContactWrapperBase?, String?) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun userDbLookup(userId: ByteArray): Maybe<ContactData> =
        Maybe.create { emitter -> emitter.onSuccess(ContactData())}

    // Message =====================================================================
    override fun sendViaClientUnsafe(
        senderId: String,
        recipientId: String,
        payload: String
    ): Maybe<RoundListBase> {
        return Maybe.create { emitter ->
            Thread.sleep(2000)
            val random = (0..100).random()

            if (random > 20) {
                emitter.onSuccess(RoundListMock())
            } else {
                emitter.onError(Exception("Failed to send"))
            }
        }
    }

    override fun sendViaClientE2E(
        senderId: ByteArray,
        recipientId: ByteArray,
        payload: String
    ): Maybe<SendReportBase> {
        return Maybe.create { emitter ->
            Thread.sleep(1000)
            val random = (1..100).random()

            when {
                random > 20 -> {
                    emitter.onSuccess(SendReportMock())
                }
                random in 10..20 -> {
                    emitter.onError(Exception())
                }
                else -> {
                    emitter.onComplete()
                }
            }
        }
    }

    override fun unmarshallSendReport(marshalledReport: ByteArray): SendReportBase {
        val gson = Gson()
        return gson.fromJson(marshalledReport.decodeToString(), SendReportMock::class.java)
    }

    override val fileRepository: FileTransferRepository by lazy {
        MockFileTransferManager()
    }

    override fun verifyOwnership(
        receivedContact: Contact,
        verifiedContact: ContactWrapperBase
    ): Boolean = true

    override fun enableDummyTraffic(enabled: Boolean) {}

    override fun replayRequests() { }

    override suspend fun getPartners(): List<String> = listOf()

    companion object {
        var isLoggedIn = false
    }
}
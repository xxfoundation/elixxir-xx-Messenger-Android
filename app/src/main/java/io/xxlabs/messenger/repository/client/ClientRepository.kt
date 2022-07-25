package io.xxlabs.messenger.repository.client

import android.content.Context
import bindings.Bindings
import bindings.NetworkHealthCallback
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Maybe
import io.reactivex.Single
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.backup.bindings.BackupService
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.groups.chat.GroupChatBindings
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.bindings.wrapper.groups.manager.GroupsManagerBindings
import io.xxlabs.messenger.bindings.wrapper.groups.message.GroupMessageReceiveBase
import io.xxlabs.messenger.bindings.wrapper.groups.message.GroupMessageReceiveBindings
import io.xxlabs.messenger.bindings.wrapper.groups.report.GroupSendReportBase
import io.xxlabs.messenger.bindings.wrapper.groups.report.NewGroupReportBase
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.bindings.wrapper.round.RoundListBase
import io.xxlabs.messenger.bindings.wrapper.ud.UserDiscoveryWrapperBindings
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.datatype.MsgType
import io.xxlabs.messenger.data.datatype.NetworkFollowerStatus
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.filetransfer.FileTransferManager
import io.xxlabs.messenger.filetransfer.FileTransferRepository
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BasePreferences
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.util.Utils
import timber.log.Timber
import javax.inject.Inject
import bindings.Contact as ContactBindings

class ClientRepository @Inject constructor(
    val schedulers: SchedulerProvider,
    val daoRepo: DaoRepository,
    private val preferences: BasePreferences,
    private val messageReceivedListener: MessageReceivedListener,
    private val backupService: BackupService
) : BaseRepository {

    // Initialization ========================================================================
    override fun doesBindingsFolderExists(): Boolean {
        val appFolder = BindingsWrapperBindings.getSessionFolder(appContext())
        return (appFolder.exists() && appFolder.length() > 0)
    }

    override fun newClientSingle(storageDir: String, password: ByteArray): Single<Boolean> {
        return Single.create { emitter ->
            try {
                BindingsWrapperBindings.newClient(storageDir, password)
                emitter.onSuccess(true)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun newClient(storageDir: String, password: ByteArray) {
        BindingsWrapperBindings.newClient(storageDir, password)
        login(storageDir, password)
    }

    override fun login(storageDir: String, password: ByteArray): ByteArray {
        Timber.v("[LOGIN]Logging in...")
        clientWrapper = BindingsWrapperBindings.login(storageDir, password) as ClientWrapperBindings
        clientWrapper.registerDatadogListener()
        Timber.v("[LOGIN]Logged in!")
        val userContact = getBaseUser().marshal()
        userWrapper = ContactWrapperBase.from(
            BindingsWrapperBindings.unmarshallContact(userContact),
            importUserContact()
        ) as ContactWrapperBindings
        Timber.d("[LOGIN] Username: ${userWrapper.getUsernameFact()}")
        Timber.d("[LOGIN] Email: ${userWrapper.getEmailFact()}")
        Timber.d("[LOGIN] Phone: ${userWrapper.getPhoneFact()}")
        val userId = clientWrapper.getUserId()
        Timber.d("[LOGIN]UserId: $userId")

        if (userId.isNotEmpty() && !preferences.getUserId().contentEquals(userId)) {
            Timber.v("[LOGIN]userId updated!")
            preferences.setUserId(userId)
        }
        preferences.preImages = getPreImages()
        initPreImageCallback()
        initFileTransferManager()
        return clientWrapper.getUser().getReceptionId()
    }

    override fun loginSingle(storageDir: String, password: ByteArray): Single<ByteArray> {
        return Single.create { emitter ->
            try {
                Timber.v("[LOGIN]Logging in...")
                clientWrapper =
                    BindingsWrapperBindings.login(storageDir, password) as ClientWrapperBindings
                clientWrapper.registerDatadogListener()
                Timber.v("[LOGIN]Logged in!")
                val userContact = getBaseUser().marshal()
                userWrapper = ContactWrapperBase.from(
                    BindingsWrapperBindings.unmarshallContact(userContact),
                    importUserContact()
                ) as ContactWrapperBindings

                Timber.d("[LOGIN] Username: ${userWrapper.getUsernameFact()}")
                Timber.d("[LOGIN] Email: ${userWrapper.getEmailFact()}")
                Timber.d("[LOGIN] Phone: ${userWrapper.getPhoneFact()}")
                val userId = clientWrapper.getUserId()
                Timber.d("[LOGIN]UserId: $userId")

                if (userId.isNotEmpty() && !preferences.getUserId().contentEquals(userId)) {
                    Timber.v("[LOGIN]userId updated!")
                    preferences.setUserId(userId)
                }
                preferences.preImages = getPreImages()
                initPreImageCallback()
                initFileTransferManager()
                emitter.onSuccess(clientWrapper.getUser().getReceptionId())
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    private fun initPreImageCallback() {
        val userReceptionId = clientWrapper.getUser().getReceptionId()
        clientWrapper.client.registerPreimageCallback(userReceptionId) { receptionId, _ ->
            Timber.v("[PREIMAGE] Pre image has been updated")
            if (receptionId.contentEquals(receptionId)) {
                preferences.preImages = getPreImages()
            }
        }
    }

    private fun initFileTransferManager() {
        fileRepository = FileTransferManager(this)
    }

    override fun initGroupManager(
        onGroupReceived: (GroupBase) -> Unit,
        onMessageReceived: (GroupMessageReceiveBase) -> Unit
    ) {
        groupManager = GroupsManagerBindings.initManager(clientWrapper, { group ->
            onGroupReceived(group)
        }, { message ->
            onMessageReceived(GroupMessageReceiveBindings(message))
        }) as GroupChatBindings
    }

    override fun sendGroupMessage(
        senderId: ByteArray,
        recipientId: ByteArray,
        groupId: ByteArray,
        payload: String
    ): Maybe<GroupSendReportBase> {
        return Maybe.create { emitter ->
            try {
                val sendReport = groupManager.send(
                    groupId,
                    payload.fromBase64toByteArray()
                )
                Timber.v("[CLIENT REPO] Group message sent $sendReport")

                emitter.onSuccess(sendReport)
            } catch (err: Exception) {
                Timber.e(err, "[CLIENT REPO] Error sending msg")
                emitter.onError(err)
            }
        }
    }

    override fun startNetworkFollower(): Single<Boolean> {
        return Single.create { emitter ->
            try {
                resumeBackup()
                clientWrapper.startNetworkFollower()
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun resumeBackup() {
        if (preferences.isBackupEnabled) {
            backupService.resumeBackup()
            backupService.backupUserFacts(userWrapper)
        }
    }

    override fun stopNetworkFollower(): Single<Boolean> {
        return Single.create { emitter ->
            try {
                clientWrapper.stopNetworkFollower()
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun getNetworkFollowerStatus(): NetworkFollowerStatus {
        if (!::fileRepository.isInitialized) initFileTransferManager()
        return NetworkFollowerStatus.from(clientWrapper.getNetworkFollowerStatus())
    }

    override fun newUserDiscovery(): Single<Boolean> {
        return Single.create { emitter ->
            try {
                udWrapperBindings = BindingsWrapperBindings
                    .newUserDiscovery(clientWrapper) as UserDiscoveryWrapperBindings
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun isLoggedIn(): Single<Boolean> {
        return hasInitialized()
    }

    override fun getNodeRegistrationStatus(): Pair<Long, Long> {
        return clientWrapper.getNodeRegistrationStatus()
    }

    override fun registerNotificationsToken(): Single<String> {
        return Single.create { emitter ->
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener(OnCompleteListener { task ->
                    try {
                        if (!task.isSuccessful) {
                            Timber.v("[CLIENT REPO] getInstanceId failed ${task.exception}")
                            emitter.onError(Exception("[CLIENT REPO] Could not complete firebase token register"))
                            return@OnCompleteListener
                        }

                        // Get new Instance ID token
                        val token = task.result

                        // Log and toast
                        Timber.d("[CLIENT REPO] TOKEN: $token")

                        Timber.v("[CLIENT REPO] Call for registerForNotifications START")
                        if (token.isNullOrEmpty()) {
                            emitter.onError(Exception("[CLIENT REPO] Token is null"))
                        }
                        clientWrapper.registerForNotifications(token!!)
                        Timber.v("[CLIENT REPO] Call for registerForNotifications OK")
                        emitter.onSuccess(token)
                    } catch (e: Exception) {
                        Timber.v("[CLIENT REPO] Call for registerForNotifications ERROR")
                        emitter.onError(e)
                    }
                })
        }
    }

    override fun unregisterForNotification(): Single<Boolean> {
        return Single.create { emitter ->
            try {
                Timber.v("[CLIENT REPO] Call for unregisterForNotifications START")
                clientWrapper.unregisterForNotifications()
                Timber.v("[CLIENT REPO] Call for unregisterForNotifications OK")
                emitter.onSuccess(true)
            } catch (e: Exception) {
                Timber.v("[CLIENT REPO] Call for unregisterForNotifications ERROR")
                emitter.onError(e)
            }
        }
    }

    //  User =================================================================================
    override fun getBioKey(): String {
        return preferences.userBiometricKey
    }

    override fun setBioKey(newKey: String) {
        preferences.userBiometricKey = newKey
    }

    override fun deleteUser(): Single<Boolean> {
        return Single.create { emitter ->
            try {
                udWrapperBindings.deleteUser(getStoredUsername())
                emitter.onSuccess(true)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun getUserReceptionId(): ByteArray {
        return clientWrapper.getUser().getReceptionId()
    }

    override fun getPreImages(): String {
        val preImage = clientWrapper.getPreImages()
        Timber.v("[PREIMAGE] Getting preimage: $preImage")
        return preImage
    }

    override fun getUserId(): ByteArray {
        return userWrapper.getId()
    }

    override fun getStoredUsername(): String {
        val factList = importUserFactsHash()
        return factList[FactType.USERNAME] ?: ""
    }

    override fun getStoredEmail(): String {
        val factList = importUserFactsHash()
        return factList[FactType.EMAIL] ?: ""
    }

    override fun getStoredPhone(): String {
        val factList = importUserFactsHash()
        return factList[FactType.PHONE] ?: ""
    }

    override fun getMashalledUser(): ByteArray {
        val userContact = getBaseUser()
        val factsHash = importUserFactsHash()
        userContact.addUsername(factsHash[FactType.USERNAME] ?: "")
        Timber.d("[CLIENT REPO] Facts: ${userContact.getStringifiedFacts()}")

        if (preferences.shareEmailWhenRequesting) {
            factsHash[FactType.EMAIL]?.let { email ->
                userContact.addEmail(email)
            }
        }

        if (preferences.sharePhoneWhenRequesting) {
            factsHash[FactType.PHONE]?.let { phone ->
                userContact.addPhone(phone)
            }
        }

        Timber.d("[CLIENT REPO] Facts after: ${userContact.getStringifiedFacts()}")
        return userContact.marshal()
    }

    private fun getBaseUser(): ContactWrapperBase {
        return clientWrapper.getUser().getContact()
    }

    //  Callbacks ============================================================================
    override fun registerAuthCallback(
        onContactReceived: (contact: ByteArray) -> Unit,
        onConfirmationReceived: (contact: ByteArray) -> Unit,
        onResetReceived: (contact: ByteArray) -> Unit
    ): Single<Boolean> {
        return Single.create { emitter ->
            try {
                clientWrapper.registerAuthCallback(
                    onContactReceived, onConfirmationReceived, onResetReceived
                )
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun registerNetworkHealthCb(networkHealthCallback: NetworkHealthCallback): Single<Boolean> {
        return Single.create { emitter ->
            try {
                clientWrapper.registerNetworkHealthCb(networkHealthCallback)
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun waitForRoundCompletion(
        roundId: Long,
        onRoundCompletionCallback: (Long, Boolean, Boolean) -> Unit,
        timeoutMillis: Long
    ): Single<Boolean> {
        return Single.create { emitter ->
            try {
                clientWrapper.waitForRoundCompletion(
                    roundId,
                    timeoutMillis,
                    onRoundCompletionCallback
                )
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun waitForMessageDelivery(
        sendReport: ByteArray,
        onMessageDeliveryCallback: (ByteArray, Boolean, Boolean, ByteArray) -> Unit,
        timeoutMillis: Long
    ) {
        clientWrapper.waitForMessageDelivery(
            sendReport,
            timeoutMillis,
            onMessageDeliveryCallback
        )
    }

    //  Listeners ============================================================================
    override fun registerMessageListener(): Single<Boolean> {
        return Single.create { emitter ->
            try {
                clientWrapper.registerMessageListener(messageReceivedListener)
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun getUdUsername(raw: Boolean): String {
        return udWrapperBindings.getUdUsername(raw)
    }

    override fun getUdEmail(raw: Boolean): String? {
        return udWrapperBindings.getUdEmail(raw)
    }

    override fun getUdPhone(raw: Boolean): String? {
        return if (raw) {
            udWrapperBindings.getUdPhone(true)
        } else {
            Country.toFormattedNumber(udWrapperBindings.getUdPhone(true))
        }
    }

    //  Ud ====================================================================================
    override fun searchUd(
        input: String,
        type: FactType
    ): Single<Pair<ContactWrapperBase?, String?>> {
        return Single.create { emitter ->
            try {
                if (!areNodesReady()) {
                    emitter.onError(throwNodeError())
                    return@create
                }

                udWrapperBindings.search(
                    input,
                    type
                ) { contact: ContactWrapperBase?, error: String? ->
                    emitter.onSuccess(Pair(contact, error))
                }
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun searchUd(
        input: HashMap<FactType, String>,
        callback: (ContactWrapperBase?, String?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    private fun throwNodeError(): Throwable {
        return NodeErrorException()
    }

    override fun searchUd(
        input: String,
        type: FactType,
        callback: (ContactWrapperBase?, String?) -> (Unit)
    ) {
        return try {
            if (!areNodesReady()) throw(throwNodeError())
            
            udWrapperBindings.searchSingle(input, callback)
        } catch (e: Exception) {
            callback.invoke(null, e.message)
        }
    }

    override fun registerUdUsername(username: String): Single<String> {
        return Single.create { emitter ->
            try {
                if (!areNodesReady()) {
                    emitter.onError(throwNodeError())
                    return@create
                }

                udWrapperBindings.registerUdUsername(username)
                udWrapperBindings.addUsernameToContact(username)
                userWrapper.addUsername(username)
                Timber.v("[CLIENT REPO] Username: ${udWrapperBindings.getUdUsername()}")
                exportUserContact()
                emitter.onSuccess(username)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun registerUdEmail(email: String): Single<String> {
        return Single.create { emitter ->
            try {
                if (!areNodesReady()) {
                    emitter.onError(throwNodeError())
                    return@create
                }

                val confirmationId = udWrapperBindings.registerUdEmail(email)
                userWrapper.addEmail(email)
                Timber.v("[CLIENT REPO] ConfirmationId for email: $confirmationId")
                emitter.onSuccess(confirmationId)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun registerUdPhone(phone: String): Single<String> {
        return Single.create { emitter ->
            try {
                if (!areNodesReady()) {
                    emitter.onError(throwNodeError())
                    return@create
                }

                val confirmationId = udWrapperBindings.registerUdPhone(phone)
                userWrapper.addPhone(phone)
                Timber.v("[CLIENT REPO] ConfirmationId for phone: $confirmationId")
                emitter.onSuccess(confirmationId)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun registerNickname(nickname: String): Single<String> {
        return Single.create { emitter ->
            try {
                userWrapper.addName(nickname)
                exportUserContact()
                emitter.onSuccess(nickname)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun confirmFact(
        confirmationId: String,
        confirmationCode: String,
        fact: String,
        isEmailCode: Boolean
    ): Single<String> {
        return Single.create { emitter ->
            try {
                udWrapperBindings.confirmFact(confirmationId, confirmationCode)
                if (isEmailCode) {
                    udWrapperBindings.addEmailToContact(fact)
                } else {
                    udWrapperBindings.addPhoneToContact(fact)
                }
                Timber.v("[CLIENT REPO] Stringified Facts: ${userWrapper.getStringifiedFacts()}")
                exportUserContact()
                emitter.onSuccess(confirmationId)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun removeFactExclusive(factType: FactType): Pair<Boolean, Throwable?> {
        return try {
            if (!areNodesReady()) {
                return Pair(false, throwNodeError())
            }

            val removed = udWrapperBindings.removeFact(factType)
            if (removed) {
                removeFactFromUser(factType)
                exportUserContact()
            }
            Pair(removed, null)
        } catch (err: Exception) {
            err.printStackTrace()
            Pair(false, err)
        }
    }

    override fun removeFact(factType: FactType): Single<Boolean> {
        return Single.create { emitter ->
            try {
                if (!areNodesReady()) {
                    emitter.onError(throwNodeError())
                    return@create
                }

                udWrapperBindings.removeFact(factType)
                removeFactFromUser(factType)
                exportUserContact()
                emitter.onSuccess(true)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun removeFactFromUser(factType: FactType) {
        val userContact = getBaseUser()
        val factsHash = importUserFactsHash()
        if (factsHash.containsKey(factType)) {
            factsHash.remove(factType)
        }

        factsHash[FactType.USERNAME]?.let { username ->
            userContact.addUsername(username)
        }

        factsHash[FactType.EMAIL]?.let { email ->
            userContact.addEmail(email)
        }

        factsHash[FactType.PHONE]?.let { phone ->
            userContact.addPhone(phone)
        }

        userWrapper = userContact as ContactWrapperBindings
    }

    //  Contact ===============================================================================
    override fun deleteContact(marshalledContact: ByteArray): Single<ByteArray> {
        return Single.create { emitter ->
            try {
                clientWrapper.client.deleteContact(marshalledContact)
                emitter.onSuccess(marshalledContact)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun unmarshallContact(rawData: ByteArray): ContactWrapperBase {
        return ContactWrapperBase.from(BindingsWrapperBindings.unmarshallContact(rawData) as ContactBindings)
    }

    override fun getContactWrapper(contact: ByteArray): ContactWrapperBase {
        return ContactWrapperBase.from(unmarshallContact(contact))
    }

    override fun requestAuthenticatedChannel(marshalledRecipient: ByteArray): Single<Long> {
        return Single.create { emitter ->
            try {
                if (!areNodesReady()) {
                    emitter.onError(throwNodeError())
                    return@create
                }

                val roundId = clientWrapper.requestAuthenticatedChannel(
                    marshalledRecipient,
                    getMashalledUser()
                )
                emitter.onSuccess(roundId)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun confirmAuthenticatedChannel(data: ByteArray): Single<Long> {
        return Single.create { emitter ->
            try {
                if (!areNodesReady()) {
                    emitter.onError(throwNodeError())
                    return@create
                }

                val roundId = clientWrapper.confirmAuthenticatedChannel(data)
                emitter.onSuccess(roundId)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun getGroupData(groupId: ByteArray): Single<GroupData> {
        return daoRepo.getGroup(groupId)
    }

    override fun makeGroup(
        name: String,
        idsList: List<ByteArray>,
        initialMessage: String?
    ): Single<NewGroupReportBase> {
        return Single.create { emitter ->
            try {
                val groupReport = groupManager.makeGroup(idsList, name, initialMessage)

                val group = groupReport.getGroup()
                val status = groupReport.getStatus()
                Timber.v(
                    "[GROUPS CREATION] Group created ID: ${
                        group.getID().toBase64String()
                    }"
                )
                Timber.v("[GROUPS CREATION] Status: $status")
                if (status == 0L) {
                    Timber.v("[GROUPS CREATION] Error did not send any invite")
                    emitter.onError(Exception("Could not create group, try again"))
                } else if (status == 1L || status == 2L) {
                    Timber.v("[GROUPS CREATION] Resending missing requests")
                    var resendReport: NewGroupReportBase
                    var resendStatus: Long
                    var trials = 5
                    do {
                        Timber.v("[GROUPS CREATION] Resending invites...")
                        resendReport = resendInviteLocal(group.getID())
                        resendStatus = resendReport.getStatus()
                        Timber.v("[GROUPS CREATION] New status:  $resendStatus")
                        trials--
                    } while (resendStatus != 3L || trials > 0)
                    emitter.onSuccess(resendReport)
                } else {
                    Timber.v("[GROUPS CREATION] Successfully sent all the requests!")
                    emitter.onSuccess(groupReport)
                }
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun resendInviteLocal(groupId: ByteArray): NewGroupReportBase {
        return groupManager.resendRequest(groupId)
    }

    override fun resendInvite(groupId: ByteArray): Single<NewGroupReportBase> {
        return Single.create { emitter ->
            try {

                val report = groupManager.resendRequest(groupId)
                emitter.onSuccess(report)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun acceptGroupInvite(serializedGroup: ByteArray): Single<Boolean> {
        return Single.create { emitter ->
            try {
                groupManager.joinGroup(serializedGroup)
                emitter.onSuccess(true)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun rejectGroupInvite(serializedGroup: ByteArray): Single<Boolean> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(true)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun leaveGroup(serializedGroup: ByteArray): Single<Boolean> {
        return Single.create { emitter ->
            try {
                groupManager.leaveGroup(serializedGroup)
                emitter.onSuccess(true)
            } catch (err: Exception) {
                emitter.onError(err)
            }
        }
    }

    override fun getMembersUsername(
        ids: List<ByteArray>,
        callback: (List<ContactWrapperBase>?, ids: IdListBase, String?) -> Unit
    ) {
        Timber.v("[MULTI USER LOOKUP] Client Repo - Get members Multilookup")
        udWrapperBindings.multiLookup(ids) { contacts, idsList, error ->
            if (error.isNullOrEmpty() && contacts != null) {
                Timber.v("[MULTI USER LOOKUP] Found!")
                callback.invoke(contacts, idsList, error)
            } else {
                Timber.v("[MULTI USER LOOKUP] Error: $error")
                callback.invoke(null, idsList, error)
            }
        }
    }

    override fun userLookup(
        userId: ByteArray,
        callback: (ContactWrapperBase?, String?) -> Unit
    ) {
        if (areNodesReady()) {
            val executionTime = Utils.getCurrentTimeStamp()
            udWrapperBindings.userLookup(userId) { contact, error ->
                if (error.isNullOrEmpty() && contact != null) {
                    Timber.v("[USER LOOKUP] User found id ${contact.getId().toBase64String()}")
                    Timber.v("[USER LOOKUP] Found!")
                    callback.invoke(contact, error)
                } else {
                    Timber.v("[USER LOOKUP] Error: $error")
                    callback.invoke(null, error)
                }
                Timber.v("[USER LOOKUP] Total execution time: ${Utils.getCurrentTimeStamp() - executionTime}")
            }
        }
    }

    override fun userDbLookup(userId: ByteArray): Maybe<ContactData> =
        daoRepo.getContactByUserId(userId)

    // Message ================================================================================
    override fun sendViaClientUnsafe(
        senderId: String,
        recipientId: String,
        payload: String
    ): Maybe<RoundListBase> {
        return Maybe.create { emitter ->
            try {
                val roundList = clientWrapper.sendUnsafe(
                    recipientId.toByteArray(),
                    payload.toByteArray(),
                    MsgType.TEXT_MESSAGE
                )

                if (roundList != null) {
                    emitter.onSuccess(roundList)
                } else {
                    emitter.onComplete()
                }
            } catch (err: Exception) {
                Timber.e(err, "[CLIENT REPO] Error sending msg")
                emitter.onError(err)
            }
        }
    }

    override fun sendViaClientE2E(
        senderId: ByteArray,
        recipientId: ByteArray,
        payload: String
    ): Maybe<SendReportBase> {
        return Maybe.create { emitter ->
            try {
                val sendReport = clientWrapper.sendE2E(
                    recipientId,
                    payload.fromBase64toByteArray(),
                    MsgType.TEXT_MESSAGE
                )

                if (sendReport != null) {
                    emitter.onSuccess(sendReport)
                } else {
                    emitter.onComplete()
                }
            } catch (err: Exception) {
                Timber.e(err, "[CLIENT REPO] Error sending msg")
                emitter.onError(err)
            }
        }
    }

    override fun createSessionFolder(context: Context): String {
        return BindingsWrapperBindings.createSessionFolder(context).absolutePath
    }

    override fun getSessionFolder(context: Context): String {
        return BindingsWrapperBindings.getSessionFolder(context).absolutePath
    }

    override fun unmarshallSendReport(marshalledReport: ByteArray): SendReportBase {
        return BindingsWrapperBindings.unmarshallSendReport(marshalledReport)
    }

    private fun exportUserContact() {
        val facts = userWrapper.getStringifiedFacts()
        Timber.d("[CLIENT REPO] exporting user facts: $facts")
        if (wereFactsModified(facts, preferences.userData)) {
            preferences.isUserProfileBackedUp = false
            preferences.userData = facts
            backupService.backupUserFacts(userWrapper)
        }
    }

    private fun wereFactsModified(original: String, new: String) = original != new

    private fun importUserContact(): List<String>? {
        //Stringified Facts: Uemu8,Ebbbb@bbb.com,P2109135060NZ;
        val userData = preferences.userData
        if (userData.isBlank() || userData.isEmpty()) {
            return null
        }

        var splitData: List<String> = userData.split(',', ';')
        splitData = splitData.filter { fact ->
            fact.isNotBlank()
        }
        Timber.v("[CLIENT REPO] Split data: $splitData")

        return splitData
    }

    private fun importUserFactsHash(): HashMap<FactType, String?> {
        val factsList = importUserContact()
        val hashmap = HashMap<FactType, String?>()
        hashmap[FactType.USERNAME] = null
        hashmap[FactType.EMAIL] = null
        hashmap[FactType.PHONE] = null

        factsList?.forEach { fact ->
            Timber.v("[CLIENT REPO] Fact: $fact")
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

    override fun areNodesReady(): Boolean = recursiveAreNodesReady()

    private fun recursiveAreNodesReady(retries: Int = 0): Boolean {
        val status = clientWrapper.getNodeRegistrationStatus()
        val rate: Double = ((status.first.toDouble() / status.second))
        Timber.v("[NODE REGISTRATION STATUS] Registration rate: $rate")

        return if (rate < NODES_READY_MINIMUM_RATE && retries <= NODES_READY_MAX_RETRIES) {
            Thread.sleep(NODES_READY_POLL_INTERVAL)
            recursiveAreNodesReady(retries+1)
        } else {
            rate >= NODES_READY_MINIMUM_RATE
        }
    }

    override lateinit var fileRepository: FileTransferRepository

    override fun verifyOwnership(
        receivedContact: Contact,
        verifiedContact: ContactWrapperBase
    ): Boolean {
        return receivedContact.marshaled?.let{
            try { clientWrapper.verifyOwnership(it, verifiedContact.marshal()) }
            catch (e: Exception) { false }
        } ?: false
    }

    override fun enableDummyTraffic(enabled: Boolean) = clientWrapper.enableDummyTraffic(enabled)

    private var shouldReplay = true

    override fun replayRequests() {
        try {
            if (shouldReplay) {
                clientWrapper.client.replayRequests()
                shouldReplay = false
            }
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    override suspend fun getPartners(): List<String> {
        return unmarshallPartners(clientWrapper.getPartners()).toList()
    }

    companion object {
        @Volatile
        private var instance: ClientRepository? = null
        const val NODES_READY_POLL_INTERVAL = 1_000L
        const val NODES_READY_MAX_RETRIES = 29
        const val NODES_READY_MINIMUM_RATE = 0.70

        lateinit var clientWrapper: ClientWrapperBindings
        lateinit var userWrapper: ContactWrapperBindings
        lateinit var udWrapperBindings: UserDiscoveryWrapperBindings
        lateinit var groupManager: GroupChatBindings

        fun hasInitialized(): Single<Boolean> {
            return Single.create { emitter ->
                val isInitialized =
                    this::clientWrapper.isInitialized && this::userWrapper.isInitialized
                Timber.v("[CLIENT REPO] initialized: $isInitialized")
                if (!isInitialized) {
                    try {
                        Timber.v("[CLIENT REPO] Trying to get client singleton...")
                        val clientSingleton = Bindings.getClientSingleton()
                        clientWrapper.client = clientSingleton
                        userWrapper = clientWrapper.getUser().getContact() as ContactWrapperBindings
                        emitter.onSuccess(true)
                    } catch (err: Exception) {
                        Timber.e(err.localizedMessage)
                        Timber.e("[CLIENT REPO] Singleton client does not exist, initiating new...")
                        emitter.onSuccess(false)
                    }
                }

                emitter.onSuccess(true)
            }
        }

        fun getInstance(
            schedulers: SchedulerProvider,
            daoRepo: DaoRepository,
            preferencesRepository: PreferencesRepository,
            messageReceivedListener: MessageReceivedListener,
            backupService: BackupService
        ): ClientRepository {
            return instance ?: synchronized(this) {
                val client = ClientRepository(
                    schedulers,
                    daoRepo,
                    preferencesRepository,
                    messageReceivedListener,
                    backupService
                )
                instance = client
                return client
            }
        }
    }
}


class NodeErrorException : Exception()
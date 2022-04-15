package io.xxlabs.messenger.backup.bindings

import bindings.*
import io.xxlabs.messenger.backup.data.BackupReport
import io.xxlabs.messenger.backup.data.RestoreLogger
import io.xxlabs.messenger.bindings.listeners.MessageReceivedListener
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.client.ClientWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.bindings.wrapper.ud.UserDiscoveryWrapperBindings
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Restores an account by calling methods exposed in Bindings.
 */
class BindingsRestoreHandler(
    private val preferences: PreferencesRepository,
    private val daoRepo: DaoRepository,
    private val messageReceivedListener: MessageReceivedListener,
    private val backupHandler: BindingsBackupHandler
) {

    private val scope = CoroutineScope(
        CoroutineName("RestoreHandler") +
                Job() +
                Dispatchers.IO
    )

    private lateinit var client: Client

    private val backupReportPath: String
        get() = File(appContext().filesDir, "backup_report.xxm").path

    private var restoreCallback: RestoreTaskCallback? = null

    /**
     * Displays events related to restore progress.
     */
    val restoreLogger: RestoreLogger = RestoreLogger()
    private fun log(event: String?) {
        event?.let { restoreLogger.log(it) }
    }

    suspend fun restoreAccount(
        restoreParams: RestoreParams,
        callback: RestoreTaskCallback
    ) {
        restoreCallback = callback
        val backupReport = restoreBackup(restoreParams)
        initializeClient(restoreParams, backupReport)

        if (backupReport.hasContacts) {
            val contactIds = backupReport.contacts.map {
                ContactId(it.fromBase64toByteArray())
            }
            val contactProfiles = lookupAndSaveContacts(contactIds)
            resetContacts(contactProfiles)
        } else return restoreComplete()
    }

    private fun restoreComplete() {
        scope.launch {
            createDefaultUserDiscovery()
            restoreCallback?.onProgressUpdate(
                100, 100, 100, null
            )
        }
    }

    private val BackupReport.hasContacts: Boolean
        get() {
            log("Found ${contacts.size} contacts in backup report.")
            return contacts.isNotEmpty()
        }

    private suspend fun lookupAndSaveContacts(contactIds: List<ContactId>): List<ContactData> =
        suspendCoroutine { continuation ->
            lookupContacts(contactIds) { contacts, _, _ ->
                val contactList = mutableListOf<ContactData>()
                contacts?.let {
                    for (contact in it) {
                        log("Found ${contact.getUsernameFact()}")
                        val contactData = createContactData(contact, RequestStatus.RESET_SENT)
                        log("Saving ${contact.getUsernameFact()} as a sent request")
                        saveContact(contactData)
                        contactList.add(contactData)
                    }
                }
                continuation.resume(contactList)
            }
        }

    private fun lookupContacts(
        contactIds: List<ContactId>,
        callback: (List<ContactWrapperBase>?, IdListBase, String?) -> Unit,
    ) {
        val byteArrayList = contactIds.map { it.value }
        ClientRepository.udWrapperBindings.multiLookup(byteArrayList, callback)
    }

    private fun resetContacts(contacts: List<ContactData>) {
        for (contact in contacts) {
            try {
                client.resetSession(
                    contact.marshaled,
                    client.user.contact.marshal(),
                    ""
                )
            } catch (e: Exception) {
                log("Error while resetting sessions.")
            }
        }
        restoreComplete()
    }

    private suspend fun restoreBackup(
        restoreParams: RestoreParams
    ) : BackupReport = withContext(scope.coroutineContext) {
        log("Parsing backup data.")
        with (restoreParams) {
            val jsonBytes = Bindings.newClientFromBackup(
                ndf, appDirectory, sessionPassword, backupPassword, account.data
            )
            BackupReport.unmarshall(jsonBytes, backupReportPath)
        }
    }

    private suspend fun initializeClient(restoreParams: RestoreParams, backupReport: BackupReport) =
        withContext(scope.coroutineContext) {
            with (ClientRepository.Companion) {
                client = Bindings.login(
                    restoreParams.appDirectory,
                    restoreParams.sessionPassword,
                    ""
                )

                clientWrapper = ClientWrapperBindings(client).apply {
                    log("Logged in.")
                    userWrapper = getUser().getContact() as ContactWrapperBindings
                    log("Getting user.")

                    preferences.setUserId(getUserId())
                    preferences.preImages = getPreImages()

                    registerCallbacks(this)
                    startNetworkFollower()
                    log("Network follower started.")

                    udWrapperBindings = BindingsWrapperBindings.newUserDiscoveryFromBackup(
                        this,
                        backupReport.userEmail ?: "",
                        backupReport.userPhone ?: ""
                    ) as UserDiscoveryWrapperBindings
                    log("User Discovery from backup initialized.")

                    if (areNodesReady(this)) {
                        log("Starting new backup process to handoff restored profile.")
                        backupHandler.initializeBackupDuringRestore(client)
                        fetchUserProfile(userWrapper, udWrapperBindings, backupReport)
                    }
                }
            }
        }

    private fun registerCallbacks(clientWrapper: ClientWrapperBindings) {
        log("Registering callbacks.")
        registerPreImageCallback(clientWrapper)
        registerAuthCallback(clientWrapper)
    }

    private fun registerPreImageCallback(clientWrapper: ClientWrapperBindings) {
        val userReceptionId = clientWrapper.getUser().getReceptionId()
        clientWrapper.client.registerPreimageCallback(userReceptionId) { receptionId, _ ->
            Timber.v("[PREIMAGE] Pre image has been updated")
            if (receptionId.contentEquals(receptionId)) {
                preferences.preImages = clientWrapper.getPreImages()
            }
        }
    }

    private fun registerAuthCallback(clientWrapper: ClientWrapperBindings) {
        clientWrapper.registerAuthCallback(
            ::onRequestReceived,
            {
                try {
                    val contactWrapper = unmarshallContact(it)
                    log("Confirm received from ${contactWrapper.getId().toBase64String()}!")
                    updateContact(contactWrapper, RequestStatus.ACCEPTED)
                    removeContactRequest(contactWrapper)
                } catch (e: Exception) {
                    log("Error occurred during confirmAuthenticatedChannel().")
                }
            },
            ::onResetReceived
        )
    }

    private fun unmarshallContact(rawData: ByteArray): ContactWrapperBase {
        return ContactWrapperBase.from(BindingsWrapperBindings.unmarshallContact(rawData) as Contact)
    }

    private fun onRequestReceived(contact: ByteArray) {
        Timber.d("Request received from $contact")
    }

    private fun onResetReceived(contact: ByteArray) {
        Timber.d("Reset received from $contact")
    }

    private fun registerMessageListeners(clientWrapper: ClientWrapperBindings) {
        log("Registering message listener.")
        clientWrapper.registerMessageListener(messageReceivedListener)
    }

    private suspend fun areNodesReady(
        clientWrapper: ClientWrapperBindings,
        retries: Int = 0
    ): Boolean {
        return try {
            delay(ClientRepository.NODES_READY_POLL_INTERVAL)
            val status = clientWrapper.getNodeRegistrationStatus()
            val rate: Double = ((status.first.toDouble() / status.second))
            log("[NODE REGISTRATION STATUS]\n\nRegistration rate: $rate")

            return if (rate < ClientRepository.NODES_READY_MINIMUM_RATE
                && retries <= ClientRepository.NODES_READY_MAX_RETRIES
            ) {
                areNodesReady(clientWrapper, retries+1)
            } else {
                (rate >= ClientRepository.NODES_READY_MINIMUM_RATE)
            }
        } catch (e: Exception) {
            log(e.message)
            areNodesReady(clientWrapper, retries)
        }
    }

    private suspend fun fetchUserProfile(
        user: ContactWrapperBindings,
        ud: UserDiscoveryWrapperBindings,
        backupReport: BackupReport

    ): ContactWrapperBase? = suspendCoroutine { continuation ->
        log("Restoring user profile.")
        ud.userLookup(user.getId()) { contact, error ->
            if (!error.isNullOrEmpty()) {
                scope.launch {
                    log("Retrying restoring user profile.")
                    val retry = withContext(coroutineContext) {
                        fetchUserProfile(user, ud, backupReport)
                    }
                    continuation.resume(retry)
                }
            }
            contact?.run {
                preferences.name = getUsernameFact()

                backupReport.userEmail?.let {
                    if (it.isNotBlank()) {
                        val rawEmail = it.substring(1, it.length)
                        user.addEmail(rawEmail)
                        addEmail(rawEmail)
                    }
                }
                backupReport.userPhone?.let {
                    if (it.isNotBlank()) {
                        val rawPhone = it.substring(1, it.length)
                        user.addPhone(rawPhone)
                        addPhone(rawPhone)
                    }
                }

                log("Successfully restored user profile for '${preferences.name}'.")
                preferences.setUserId(getId())
                preferences.userData = getStringifiedFacts()
                continuation.resume(contact)
            }
        }
    }

    private fun createDefaultUserDiscovery() {
        ClientRepository.udWrapperBindings = BindingsWrapperBindings
            .newUserDiscovery(ClientRepository.clientWrapper) as UserDiscoveryWrapperBindings
        log("Main User Discovery initialized.")
    }

    private fun saveContact(contactData: ContactData): Long {
        val rows = daoRepo.addNewContact(contactData).blockingGet()
        val resultString =
            if (rows > 0) "saved".also { addContactRequest(contactData) }
            else "already exists"

        log("${contactData.displayName} $resultString" + " in app database.")
        return rows
    }

    private fun updateContact(contact: ContactWrapperBase, requestStatus: RequestStatus): Int    {
        val rows = daoRepo.updateContactState(contact.getId(), requestStatus).blockingGet()
        if (rows > 0) {
            log("${contact.getId().toBase64String()} updated " +
                        "to ${requestStatus.name} in app database.")
        }
        return rows
    }

    private fun addContactRequest(contactData: ContactData) {
        preferences.addContactRequest(
            contactData.userId,
            contactData.username,
            -1,
            true
        )
    }

    private fun removeContactRequest(contact: ContactWrapperBase) {
        preferences.removeContactRequests(contact.getId())
    }

    private fun createContactData(contact: ContactWrapperBase, status: RequestStatus): ContactData {
        return with(contact) {
            ContactData(
                userId = getId(),
                username = getUsernameFact(),
                nickname = getNameFact() ?: getUsernameFact(),
                marshaled = contact.marshal(),
                email = getEmailFact() ?: "",
                phone = getPhoneFact() ?: "",
                status = status.value
            )
        }
    }
}

@JvmInline
private value class ContactId(val value: ByteArray)

interface RestoreTaskCallback {
    fun onProgressUpdate(contactsFound: Long, contactsRestored: Long, total: Long, error: String?)
}
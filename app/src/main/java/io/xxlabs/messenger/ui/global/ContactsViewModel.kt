package io.xxlabs.messenger.ui.global

import android.app.Application
import android.util.Pair
import androidx.lifecycle.*
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperMock
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.PayloadWrapper
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.datatype.RequestStatus.*
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.requests.data.contact.ContactRequestData
import io.xxlabs.messenger.requests.data.contact.ContactRequestsRepository
import io.xxlabs.messenger.requests.data.contact.RequestMigrator
import io.xxlabs.messenger.requests.data.group.InvitationMigrator
import io.xxlabs.messenger.support.extensions.combineWith
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.support.util.value
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ContactsViewModel @Inject constructor(
    private val app: Application,
    private val repo: BaseRepository,
    private val daoRepo: DaoRepository,
    private val preferences: PreferencesRepository,
    private val schedulers: SchedulerProvider,
    private val requestsDataSource: ContactRequestsRepository
) : ViewModel() {

    val showToast: Flow<ToastUI?> by ::_showToast
    private val _showToast = MutableStateFlow<ToastUI?>(null)

    val navigateToChat: Flow<Contact?> by ::_navigateToChat
    private val _navigateToChat = MutableStateFlow<Contact?>(null)

    var subscriptions = CompositeDisposable()
    var newAuthRequestSent = MutableLiveData<SimpleRequestState<Any>>()
    var newConfirmRequestSent = MutableLiveData<DataRequestState<Any>>()
    var newIncomingRequestReceived = MutableLiveData<SimpleRequestState<ByteArray?>>()
    var newConfirmationRequestReceived = MutableLiveData<SimpleRequestState<ByteArray>>()
    var newGroupRequestSent = MutableLiveData<DataRequestState<Boolean>>()
    var acceptGroupRequest = MutableLiveData<DataRequestState<GroupData>>()
    var rejectGroup = MutableLiveData<DataRequestState<Int>>()

    val contactsData = daoRepo.getAllContactsLive()
    val groupsData = daoRepo.getAllGroupsLive()
    val requestsCount = requestsDataSource.unreadCount.asLiveData()
    val combinedContactGroupsData = contactsData.combineWith(groupsData) { contacts, groups ->
        Pair(contacts ?: listOf(), groups ?: listOf())
    }

    val navigateToGroup: LiveData<ByteArray?> by ::_navigateToGroup
    private val _navigateToGroup = MutableLiveData<ByteArray?>(null)

    init {
        Timber.v("isAuthCallbackRegistered: ${isAuthCallbackRegistered()}")
        migrateOldRequests()
        if (BuildConfig.DEBUG) listContacts()
    }

    private fun listContacts() {
        viewModelScope.launch {
            daoRepo.getAllContacts().value().forEach { contactData ->
                Timber.d("Found contact: ${contactData.displayName}/${contactData.userId.toBase64String()}")
            }
        }
    }

    private fun migrateOldRequests() {
        viewModelScope.launch {
            RequestMigrator.performMigration(preferences, requestsDataSource, daoRepo)
        }
    }

    fun registerAuthCallback() {
        Timber.v("[MAIN] Registering auth callback...")
        if (!isAuthCallbackRegistered()) {
            Timber.v("[MAIN] nor initialized, initializing network callback...")
            subscriptions.add(
                repo.registerAuthCallback(
                    ::onRequestReceived,
                    ::onConfirmationReceived,
                    ::onResetReceived
                ).doOnSuccess {
                    setAuthCallbackRegistered()
                    Timber.v("Successfully registered AuthCallback")
                }.doOnError { err ->
                    Timber.e("Error registering AuthCallback: ${err.localizedMessage}}")
                }
                    .subscribeOn(schedulers.io)
                    .subscribe()
            )
        } else {
            Timber.v("[MAIN] Authcallback is already initialized...")
        }
    }

    private fun onRequestReceived(contact: ByteArray) {
        val id = getBindingsContactId(contact)
        Timber.v("Request received from: ${id.toBase64String()}")
        newRequest(contact)
    }

    private fun onConfirmationReceived(contact: ByteArray) {
        val id = getBindingsContactId(contact)
        Timber.v("Request feedback received from: ${id.toBase64String()}")
        confirmRequest(contact)
    }

    private fun onResetReceived(contact: ByteArray) {
        Timber.v("Reset connection received from ${getBindingsContactId(contact)}")
    }

    fun resetSession(contact: ContactData) {
        try {
            val roundId = ClientRepository.clientWrapper.client.resetSession(
                contact.marshaled,
                repo.getMashalledUser(),
                ""
            )
            if (roundId > 0) {
                updateContactStatus(contact.userId, RESET_SENT)
                saveRequest(contact)
            }
        } catch (e: Exception) {
            Timber.d("Failed to reset session: ${e.message}")
        }
    }

    fun updateAndRequestAuthChannel(contact: ContactData) {
        if (isMockVersion()) {
            newAuthRequestSent.postValue(SimpleRequestState.Success(Any()))
            return
        }
        requestAuthenticatedChannel(contact)
    }

    private fun requestAuthenticatedChannel(contact: ContactData) {
        val contactWrapper = repo.unmarshallContact(contact.marshaled!!)
        val bindingsId = contactWrapper!!.getId()
        subscriptions.add(
            repo.requestAuthenticatedChannel(contact.marshaled!!)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .doOnSuccess { roundId ->
                    if (isMockVersion()) {
                        updateContactMock(bindingsId, contactWrapper)
                        Timber.v("${contact.displayName} became ${(contactWrapper as ContactWrapperMock).contact.status}!")
                        newAuthRequestSent.postValue(SimpleRequestState.Success(Any()))
                    } else {
                        Timber.v("contact request sent to: ${bindingsId.toBase64String()}")

                        saveContact(contact, SENT)
                        saveRequest(contact)
                        newAuthRequestSent.postValue(SimpleRequestState.Success(Any()))
                    }
                }
                .doOnError { err ->
                    Timber.e("Request error for ${bindingsId.toBase64String()}: ${err.localizedMessage}")
                    newAuthRequestSent.postValue(SimpleRequestState.Error(
                        Exception("Your contact request to ${contact.displayName} has failed.")
                    ))
                    handleRequestAuthChannelError(err, contactWrapper)
                }.subscribe()
        )
    }

    private fun saveContact(contact: ContactData, status: RequestStatus) {
        viewModelScope.launch {
            try {
                if (daoRepo.getContactByUserId(contact.userId).value() == null) {
                    val rowId = daoRepo.addNewContact(contact.copy(status = status.value)).value()
                    Timber.d("Saved ${contact.displayName} to row $rowId in DB.")
                } else {
                    updateContactStatus(contact.userId, status) {
                        Timber.d("Updated ${contact.displayName} with status $status in DB.")
                    }
                }
            } catch (e: Exception) {
                Timber.d("Exception saving ${contact.displayName}: ${e.message}")
            }
        }
    }

    fun confirmAuthenticatedChannel(contact: ContactData) {
        val contactWrapper = repo.unmarshallContact(contact.marshaled!!)!!
        contactWrapper.addUsername(contact.username)
        val bindingsId = getBindingsContactId(contactWrapper.marshal())
        Timber.v("Confirming authentication channel with ${bindingsId.toBase64String()}")
        newConfirmRequestSent.postValue(DataRequestState.Start())
        subscriptions.add(
            repo.confirmAuthenticatedChannel(contactWrapper.marshal())
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.io)
                .flatMap { roundId ->
                    daoRepo.updateContact(contact)
                }
                .observeOn(schedulers.main)
                .doOnSuccess {
                    if (isMockVersion()) {
                        Timber.v("Authentication channel confirm sent! wait for its round to complete...")
                        updateContactStatus(
                            contactWrapper.getId(),
                            requestStatus = ACCEPTED
                        )
                        newConfirmRequestSent.postValue(DataRequestState.Success(contact))
                        return@doOnSuccess
                    }

                    Timber.v("Authentication channel confirm sent! wait for its round to complete...")
                    newConfirmRequestSent.postValue(DataRequestState.Success(Any()))
                    deleteRequest(contact)
                }.doOnError { err ->
                    Timber.e("Request error for ${bindingsId.toBase64String()}: ${err.localizedMessage}")
                    updateContactStatus(
                        contactWrapper.getId(),
                        CONFIRM_FAIL
                    )
                    newConfirmRequestSent.postValue(DataRequestState.Error(
                        Exception("Failed to accept contact request from ${contactWrapper.getUsernameFact()}")
                    ))
                }.subscribe()
        )
    }

    private fun handleRequestAuthChannelError(err: Throwable, contactWrapper: ContactWrapperBase) {
        when {
            err.localizedMessage?.contains("already exists") == true -> {
                Timber.e("Request is still open")
                updateContactStatus(
                    contactWrapper.getId(),
                    SENT
                )
            }
            err.localizedMessage?.contains("timed out") == true -> {
                Timber.e("Request timed out!")
                updateContactStatus(
                    contactWrapper.getId(),
                    SEND_FAIL
                )
            }
            err.localizedMessage?.contains("Cannot request authenticated channel") == true -> {
                Timber.e("Request failed!")
                updateContactStatus(
                    contactWrapper.getId(),
                    SEND_FAIL
                )
            }

            err.localizedMessage?.contains("Request is still open") == true -> {
                Timber.e("Request is still open...")
            }
        }
    }

    private fun newRequest(marshalledData: ByteArray) {
        Timber.v("[RECEIVED REQUEST] Confirming contact...")
        val newContact = generateContact(marshalledData)
        Timber.v("[RECEIVED REQUEST] Generated contact: $newContact")
    }

    private fun generateContact(
        marshalledData: ByteArray
    ): ContactData {
        Timber.v("Unmarshalling contact")
        val unmarshalledContact = repo.unmarshallContact(marshalledData)
        Timber.v("Unmarshalled success!")
        val userId = unmarshalledContact!!.getId()
        Timber.v("Getting username...")
        val contactUsername = unmarshalledContact.getUsernameFact()
        Timber.v("Username is $contactUsername")
        Timber.v("Getting name...")
        val contactName = unmarshalledContact.getNameFact() ?: contactUsername
        val contactEmail = unmarshalledContact.getEmailFact(true) ?: ""
        val contactPhone = unmarshalledContact.getPhoneFact(true) ?: ""
        Timber.v("Name is $contactName")

        val contact =  ContactData(
            userId = userId,
            username = contactUsername,
            nickname = contactName ?: "",
            marshaled = marshalledData,
            email = contactEmail,
            phone = contactPhone,
            status = VERIFYING.value
        )

        subscriptions.add(
            daoRepo.addNewContact(contact)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onError = { err ->
                        Timber.e(err)
                        Timber.v("Couldn't save contact: already exists")
                    },
                    onSuccess = {
                        Timber.v("${contact.userId.toBase64String()} has sent a new contact request!")
                        saveRequest(contact, true)
                        verifyNewRequest(contact)
                    })
        )

        return contact
    }

    private fun saveRequest(contact: ContactData, unread: Boolean = false) {
        val contactRequest = ContactRequestData(contact, unread)
        requestsDataSource.save(contactRequest)
    }

    fun confirmRequest(marshalledContact: ByteArray) {
        viewModelScope.launch {
            val contactBindings = repo.unmarshallContact(marshalledContact)
            contactBindings?.let {
                Timber.v("Trying to update the contact ${contactBindings.getId()}...")
                getContact(contactBindings.getId())?.let {
                    updateContactStatus(it.userId, ACCEPTED) {
                        deleteRequest(it)
                        showRequestAccepted(it)
                    }
                }
            }
        }
    }

    private suspend fun getContact(userId: ByteArray): Contact? =
        daoRepo.getContactByUserId(userId).value()

    private fun showRequestAccepted(contact: Contact) {
        val requestAccepted = ToastUI.create(
            header = contact.displayName,
            body = "Accepted your request",
            leftIcon = R.drawable.ic_check,
            iconTint = R.color.accent_success,
            actionText = "Send message",
            actionClick = { navigateToChat(contact) }
        )
        _showToast.value = requestAccepted
    }

    fun onToastShown() {
        _showToast.value = null
    }

    private fun navigateToChat(contact: Contact) {
        _navigateToChat.value = contact
    }

    fun onNavigateHandled() {
        _navigateToChat.value = null
    }

    private fun getBindingsContactId(data: ByteArray): ByteArray {
        val contact = repo.unmarshallContact(data)
        Timber.v("Contact id: ${contact?.getId()?.toBase64String()}")
        return contact!!.getId()
    }

    fun verifyNewRequest(
        contact: ContactData
    ) {
        Timber.v("[RECEIVED REQUEST] Verifying Request ${contact.userId.toBase64String()}...")
        if (contact.hasFacts()) { //UD Search
            verifyContactViaSearch(contact)
        } else { // UD Lookup
            verifyContactViaLookup(contact)
        }
    }

    private fun verifyContactViaSearch(contact: ContactData) {
        Timber.v("[RECEIVED REQUEST] User have facts - UD Search")
        val factPair: Pair<String, FactType> = when {
            contact.phone.isNotBlank() -> Pair(contact.phone, FactType.PHONE)
            contact.email.isNotBlank() -> Pair(contact.email, FactType.EMAIL)
            else -> return
        }

        repo.searchUd(factPair.first, factPair.second) { searchResult, error ->
            if (error.isNullOrEmpty()) {
                continueVerificationStep(contact, searchResult)
            } else {
                // An error occurred while searching UD, the contact is still unverified.
                Timber.d("Search UD error: $error")
                onFailedToVerify(contact)
            }
        }
    }

    private fun continueVerificationStep(contact: ContactData, searchResult: ContactWrapperBase?) {
        if (searchResult == null) { // Fraudulent
            Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is FRAUDULENT")
            deleteFraudulentContact(contact)
        } else {
            if (searchResult.getId().contentEquals(contact.userId)) { // Verified
                Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is VERIFIED")
                verifyContact(contact, searchResult)
            } else { // Fraudulent
                Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is FRAUDULENT")
                deleteFraudulentContact(contact)
            }
        }
    }

    private fun verifyContactViaLookup(contact: ContactData) {
        Timber.v("[RECEIVED REQUEST] User does not have facts - UD Lookup")
        repo.userLookup(contact.userId) { searchResult, error ->
            if (error.isNullOrEmpty()) {
                continueVerificationStep(contact, searchResult)
            } else {
                Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is UNVERIFIED")
                // An error occurred while searching UD, the contact is still unverified.
                Timber.d("Search UD error: $error")
                onFailedToVerify(contact)
            }
        }
    }

    private fun verifyContact(receivedContact: ContactData, verifiedContact: ContactWrapperBase) {
        if (repo.verifyOwnership(receivedContact, verifiedContact)) {
            onContactVerified(receivedContact)
        } else {
            deleteFraudulentContact(receivedContact)
        }
    }

    private fun onContactVerified(contact: ContactData) {
        saveContact(contact, VERIFIED)
        saveRequest(contact, true)
        Timber.v("${contact.userId.toBase64String()} has sent a new contact request!")
        newIncomingRequestReceived.postValue(SimpleRequestState.Success(contact.marshaled))
    }

    private fun onFailedToVerify(contact: ContactData) {
        saveContact(contact, VERIFICATION_FAIL)
        saveRequest(contact, true)
    }

    private fun deleteFraudulentContact(contact: ContactData) {
        updateToDeleting(contact)

        subscriptions.add(
            daoRepo.deleteContact(contact)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .doOnError { Timber.v("[RECEIVED REQUEST] Error deleting Fraudulent contact ${contact.userId.toBase64String()}") }
                .doOnSuccess {
                    Timber.v("[RECEIVED REQUEST] Fraudulent contact has been deleted!")
                    deleteRequest(contact)
                }
                .subscribe()
        )
    }

    private fun deleteRequest(contact: Contact) {
        val contactRequest = ContactRequestData(contact, true)
        requestsDataSource.delete(contactRequest)
    }

    private fun updateToDeleting(contact: ContactData) {
        updateContactStatus(contact.userId, DELETING)
    }

    fun rejectContact(contact: ContactData) {
        hideRequest(contact)
    }

    private fun hideRequest(contact: ContactData) {
        updateContactStatus(contact.userId, HIDDEN) {
            requestsDataSource.reject(ContactRequestData(contact))
        }
    }

    private fun updateContactStatus(
        contactId: ByteArray,
        requestStatus: RequestStatus,
        onSuccess: (() -> (Unit))? = null
    ) {
        Timber.v("Contact to update: ${contactId.toBase64String()}, friendStatus: $requestStatus...")
        subscriptions.add(
            daoRepo.updateContactState(contactId, requestStatus)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .doOnSuccess {
                    Timber.v("${contactId.toBase64String()} became $requestStatus!")
                    onSuccess?.invoke()
                }
                .doOnError { err ->
                    Timber.e("Failed to update ${contactId.toBase64String()}: ${err.localizedMessage}")
                }.subscribe()
        )
    }

    private fun updateContactMock(
        contactId: ByteArray,
        contactWrapper: ContactWrapperBase
    ) {
        val status = RequestStatus.from((contactWrapper as ContactWrapperMock).contact.status)
        subscriptions.add(
            daoRepo.updateContactState(contactId, status)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .doOnSuccess {
                    Timber.v("${contactWrapper.getId().toBase64String()} became a friend!")
                }
                .doOnError {
                    Timber.e("Request timed out for ${contactWrapper.getId()}: ${it.localizedMessage}")
                }.subscribe()
        )
    }

    private fun isAuthCallbackRegistered(): Boolean {
        return XxMessengerApplication.isAuthCallbackRegistered
    }

    private fun setAuthCallbackRegistered() {
        XxMessengerApplication.isAuthCallbackRegistered = true
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }

    fun createGroup(name: String, initialMsg: String?, contactsList: List<ContactData>) {
        newGroupRequestSent.value = DataRequestState.Start()
        val idsList = contactsList.map { contacts ->
            contacts.userId
        }
        Timber.v("[GROUPS CREATION] IdsList: $idsList")
        var groupId = byteArrayOf()
        var createdMs = Utils.getCurrentTimeStamp()
        subscriptions.add(
            repo.makeGroup(name, idsList, initialMsg)
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.io)
                .flatMap { groupReport ->
                    val group = groupReport.getGroup()
                    groupId = group.getID()
                    createdMs = group.getCreatedMs()
                    daoRepo.createUserGroup(group)
                }.flatMap {
                    Timber.v("[GROUPS CREATION] Getting group...")
                    daoRepo.getGroup(groupId)
                }.flatMap {
                    val membersList = contactsList.map { member ->
                        GroupMember(
                            groupId = groupId,
                            userId = member.userId,
                            username = member.username
                        )
                    }.toMutableList()

                    membersList.add(
                        GroupMember(
                            groupId = groupId,
                            userId = preferences.getUserId(),
                            username = repo.getStoredUsername()
                        )
                    )
                    daoRepo.addAllMembers(groupId, membersList)
                }.flatMap {
                    if (!initialMsg.isNullOrEmpty()) {
                        val payload: String = if (isMockVersion()) {
                            PayloadWrapper(initialMsg).toString()
                        } else {
                            ChatMessage.buildCmixMsg(initialMsg)
                        }

                        daoRepo.insertGroupMessage(
                            GroupMessageData(
                                groupId = groupId,
                                receiver = groupId,
                                sender = preferences.getUserId(),
                                status = MessageStatus.SENT.value,
                                payload = payload,
                                timestamp = createdMs,
                            )
                        )
                    } else {
                        Single.just(1)
                    }
                }.observeOn(schedulers.main)
                .doOnError { err ->
                    Timber.e("[GROUP] Error on creating group: ${err.localizedMessage}")
                    newGroupRequestSent.value = DataRequestState.Error(err)
                }.doOnSuccess {
                    newGroupRequestSent.value = DataRequestState.Success(true)
                    _navigateToGroup.value = groupId
                }.subscribe()
        )
    }

    fun onNavigateToGroupHandled() {
        _navigateToGroup.value = null
    }

    fun acceptGroup(group: GroupData) {
        Timber.v("[GROUPS CREATION] Accepting group request")
        subscriptions.add(
            repo.acceptGroupInvite(group.serial)
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.io)
                .flatMap {
                    daoRepo.acceptGroup(group)
                }
                .observeOn(schedulers.main)
                .doOnError { err ->
                    Timber.e("[GROUP ACCEPT] Error on accepting group: ${err.localizedMessage}")
                    acceptGroupRequest.postValue(DataRequestState.Error(
                        Exception("Failed to join group ${group.name}")
                    ))
                }
                .doOnSuccess {
                    Timber.v("[GROUP ACCEPT] Finished with success!")
                    //getMembers(group.groupId)
                    acceptGroupRequest.value = DataRequestState.Success(group)
                }.subscribe()
        )
    }

    fun rejectGroup(group: GroupData, position: Int) {
        Timber.v("[GROUPS CREATION] Rejecting group request")
        subscriptions.add(
            repo.rejectGroupInvite(group.serial)
                .subscribeOn(schedulers.single)
                .flatMap {
                    daoRepo.deleteGroup(group)
                }
                .observeOn(schedulers.io)
                .doOnError { err ->
                    Timber.e("[GROUP] Error on creating group: ${err.localizedMessage}")
                    rejectGroup.value = DataRequestState.Error(err)
                }
                .doOnSuccess {
                    rejectGroup.value = DataRequestState.Success(position)
                }
                .observeOn(schedulers.main)
                .subscribe()
        )
    }
}
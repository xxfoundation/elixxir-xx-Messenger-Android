package io.xxlabs.messenger.ui.global

import android.app.Application
import android.util.Pair
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperMock
import io.xxlabs.messenger.data.data.ContactRoundRequest
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.PayloadWrapper
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.data.datatype.ContactRequestState
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.FactType
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.repository.client.ClientRepository
import io.xxlabs.messenger.support.extensions.combineWith
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.util.Utils
import timber.log.Timber
import javax.inject.Inject

class ContactsViewModel @Inject constructor(
    val app: Application,
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    val preferences: PreferencesRepository,
    val schedulers: SchedulerProvider,
) : ViewModel() {
    var subscriptions = CompositeDisposable()
    var newAuthRequestSent = MutableLiveData<SimpleRequestState<Any>>()
    var newConfirmRequestSent = MutableLiveData<DataRequestState<Any>>()
    var newIncomingRequestReceived = MutableLiveData<SimpleRequestState<ByteArray>>()
    var newConfirmationRequestReceived = MutableLiveData<SimpleRequestState<ByteArray>>()
    var newGroupRequestSent = MutableLiveData<DataRequestState<Boolean>>()
    var acceptGroupRequest = MutableLiveData<DataRequestState<GroupData>>()
    var rejectGroup = MutableLiveData<DataRequestState<Int>>()

    val contactsData = daoRepo.getAllContactsLive()
    val groupsData = daoRepo.getAllGroupsLive()
    val requestsCount = MutableLiveData<Int>()
    val combinedContactGroupsData = contactsData.combineWith(groupsData) { contacts, groups ->
        Pair(contacts ?: listOf(), groups ?: listOf())
    }

    init {
        Timber.v("isAuthCallbackRegistered: ${isAuthCallbackRegistered()}")
        requestsCount.value = preferences.contactsCount
    }

    fun addRequestCount() {
        val currVal = preferences.contactsCount
        Timber.v("Current requests val $currVal")
        preferences.contactsCount = currVal + 1
        requestsCount.postValue(preferences.contactsCount)
    }

    fun viewAllRequests() {
        val currVal = preferences.contactsCount
        Timber.v("Current val $currVal")
        preferences.contactsCount = 0
        requestsCount.value = 0
    }

    fun viewSingleRequest() {
        val currVal = preferences.contactsCount
        Timber.v("Current val $currVal")
        if (currVal > 0) {
            preferences.contactsCount = currVal - 1
        } else {
            preferences.contactsCount = 0
        }
        requestsCount.value = preferences.contactsCount
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
        newConfirmationRequestReceived.postValue(SimpleRequestState.Success(contact))
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
                updateContactStatus(contact.userId, RequestStatus.RESET_SENT)
                preferences.removeContactRequests(contact.userId)
                preferences.addContactRequest(
                    contact.userId,
                    contact.username,
                    roundId,
                    true
                )

            }
        } catch (e: Exception) {
            Timber.d("Failed to reset session: ${e.message}")
        }
    }

    fun updateAndRequestAuthChannel(contact: ByteArray) {
        if (isMockVersion()) {
            newAuthRequestSent.postValue(SimpleRequestState.Success(Any()))
            return
        }

        requestAuthenticatedChannel(contact)
    }

    fun requestAuthenticatedChannel(contact: ByteArray) {
        val contactWrapper = repo.unmarshallContact(contact)
        val bindingsId = contactWrapper!!.getId()
        subscriptions.add(
            repo.requestAuthenticatedChannel(contact)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .doOnSuccess { roundId ->
                    if (isMockVersion()) {
                        updateContactMock(bindingsId, contactWrapper)
                        Timber.v("${contact.toBase64String()} became ${(contactWrapper as ContactWrapperMock).contact.status}!")
                        newAuthRequestSent.postValue(SimpleRequestState.Success(Any()))
                    } else {
                        Timber.v("contact request sent to: ${bindingsId.toBase64String()}")

                        updateContactStatus(
                            bindingsId,
                            RequestStatus.SENT
                        )

                        val request = preferences.getContactRequest(bindingsId)
                        if (request != null && request.isSent) {
                            preferences.removeContactRequest(request)
                        }

                        val username = contactWrapper.getUsernameFact()
                        preferences.addContactRequest(
                            bindingsId,
                            username,
                            roundId,
                            true
                        )

                        preferences.getContactRequest(bindingsId)?.apply {
                            completeRound(this)
                        }
//                        waitForRoundCompletion(bindingsId, roundId)
                        newAuthRequestSent.postValue(SimpleRequestState.Success(Any()))
                    }
                }
                .doOnError { err ->
                    Timber.e("Request error for ${bindingsId.toBase64String()}: ${err.localizedMessage}")
                    newAuthRequestSent.postValue(SimpleRequestState.Error())
                    handleRequestAuthChannelError(err, contactWrapper)
                }.subscribe()
        )
    }

    fun confirmAuthenticatedChannel(contact: ContactData) {
        val contactWrapper = repo.unmarshallContact(contact.marshaled!!)!!
        contactWrapper.addUsername(contact.username)
        val bindingsId = getBindingsContactId(contactWrapper.marshal())
        Timber.v("Confirming authentication channel with ${bindingsId.toBase64String()}")
        newConfirmRequestSent.postValue(DataRequestState.Start())
        var roundId: Long = -1L
        subscriptions.add(
            repo.confirmAuthenticatedChannel(contactWrapper.marshal())
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.io)
                .flatMap { newRoundId ->
                    roundId = newRoundId
                    daoRepo.updateContact(contact)
                }
                .observeOn(schedulers.main)
                .doOnSuccess {
                    if (isMockVersion()) {
                        Timber.v("Authentication channel confirm sent! wait for its round to complete...")
                        updateContactStatus(
                            contactWrapper.getId(),
                            requestStatus = RequestStatus.ACCEPTED
                        )
                        newConfirmRequestSent.postValue(DataRequestState.Success(contact))
                        return@doOnSuccess
                    }

                    Timber.v("Authentication channel confirm sent! wait for its round to complete...")
                    newConfirmRequestSent.postValue(DataRequestState.Success(Any()))

                    preferences.removeContactRequests(contact.userId)

                    preferences.addContactRequest(
                        bindingsId,
                        contactWrapper.getUsernameFact(),
                        roundId,
                        false
                    )

                    preferences.getContactRequest(bindingsId)?.apply {
                        completeRound(this)
                    }
                }.doOnError { err ->
                    Timber.e("Request error for ${bindingsId.toBase64String()}: ${err.localizedMessage}")
                    updateContactStatus(
                        contactWrapper.getId(),
                        RequestStatus.CONFIRM_FAIL
                    )
                    roundRequestFail(contactWrapper)
                    newConfirmRequestSent.postValue(DataRequestState.Error(err))
                }.subscribe()
        )
    }

    private fun handleRequestAuthChannelError(err: Throwable, contactWrapper: ContactWrapperBase) {
        when {
            err.localizedMessage?.contains("already exists") == true -> {
                Timber.e("Request is still open")
                updateContactStatus(
                    contactWrapper.getId(),
                    RequestStatus.SENT
                )
            }
            err.localizedMessage?.contains("timed out") == true -> {
                Timber.e("Request timed out!")
                updateContactStatus(
                    contactWrapper.getId(),
                    RequestStatus.SEND_FAIL
                )
                roundRequestFail(contactWrapper)
            }
            err.localizedMessage?.contains("Cannot request authenticated channel") == true -> {
                Timber.e("Request failed!")
                updateContactStatus(
                    contactWrapper.getId(),
                    RequestStatus.SEND_FAIL
                )
                roundRequestFail(contactWrapper)
            }

            err.localizedMessage?.contains("Request is still open") == true -> {
                Timber.e("Request is still open...")
            }
        }
    }

    private fun roundRequestFail(contactWrapper: ContactWrapperBase) {
        val contactRequest = preferences.getContactRequest(contactWrapper.getId())
        if (contactRequest != null) {
            contactRequest.verifyState = ContactRequestState.FAILED
            preferences.updateContactRequest(contactRequest)
        } else {
            val newContactRequest = ContactRoundRequest(
                contactId = contactWrapper.getId(),
                contactUsername = contactWrapper.getUsernameFact(),
                roundId = -1,
                isSent = true,
                verifyState = ContactRequestState.FAILED
            )
            Timber.e("Round request not found for ${contactWrapper.getId().toBase64String()}!")
            preferences.addContactRequest(newContactRequest)
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
        val contactEmail = unmarshalledContact.getEmailFact() ?: ""
        val contactPhone = unmarshalledContact.getPhoneFact() ?: ""
        Timber.v("Name is $contactName")

        val contact =  ContactData(
            userId = userId,
            username = contactUsername,
            nickname = contactName ?: "",
            marshaled = marshalledData,
            email = contactEmail,
            phone = contactPhone,
            status = RequestStatus.UNVERIFIED.value
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
                        addRequestCount()
                        Timber.v("${contact.userId.toBase64String()} has sent a new contact request!")
                        newIncomingRequestReceived.postValue(SimpleRequestState.Success(marshalledData))
                        verifyNewRequest(contact)
                    })
        )

        return contact
    }

    fun confirmRequest(marshalledContact: ByteArray) {
        val contact = repo.unmarshallContact(marshalledContact)
        val bindingsId = contact!!.getId()
        Timber.v("Trying to update the contact $bindingsId...")
        preferences.removeContactRequests(contact.getId())
        updateContactStatus(bindingsId, RequestStatus.ACCEPTED)
    }

    private fun addReceivedContactRequest(
        contact: ContactWrapperBase
    ) {
        val contactRoundRequest = ContactRoundRequest(
            contactId = contact.getId(),
            contactUsername = contact.getUsernameFact(),
            roundId = -1,
            isSent = false,
            verifyState = ContactRequestState.RECEIVED
        )

        preferences.removeContactRequests(contact.getId())
        preferences.addContactRequest(contactRoundRequest)
    }

    private fun getBindingsContactId(data: ByteArray): ByteArray {
        val contact = repo.unmarshallContact(data)
        Timber.v("Contact id: ${contact?.getId()?.toBase64String()}")
        return contact!!.getId()
    }

    fun verifyNewRequest(
        contact: ContactData
    ) {
        updateContactStatus(contact.userId, RequestStatus.VERIFYING)
        addVerifyingContactRequest(contact)
        Timber.v("[RECEIVED REQUEST] Verifying Request ${contact.userId.toBase64String()}...")
        if (contact.hasFacts()) { //UD Search
            verifyContactViaSearch(contact)
        } else { // UD Lookup
            verifyContactViaLookup(contact)
        }
    }

    private fun addVerifyingContactRequest(contact: ContactData) {
        val contactRoundRequest = ContactRoundRequest(
            contactId = contact.userId,
            contactUsername = contact.username,
            roundId = -1,
            isSent = false,
            verifyState = ContactRequestState.VERIFYING
        )

        preferences.removeContactRequests(contact.userId)
        preferences.addContactRequest(contactRoundRequest)
    }

    private fun verifyContactViaSearch(contact: ContactData) {
        Timber.v("[RECEIVED REQUEST] User have facts - UD Search")
        val factPair: Pair<String, FactType> = when {
            contact.phone.isNotBlank() -> Pair(contact.phone, FactType.PHONE)
            contact.email.isNotBlank() -> Pair(contact.email, FactType.EMAIL)
            else -> return
        }

        repo.searchUd(factPair.first, factPair.second) { newContact, error ->
            if (newContact == null) { //Fraudulent
                Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is FRAUDULENT")
                deleteFraudulentContact(contact)
            } else {
                if (newContact.getId().contentEquals(contact.userId)) { //Verified
                    Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is VERIFIED")
                    verifyContact(contact, newContact)
                } else { //Fraudulent
                    Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is FRAUDULENT")
                    deleteFraudulentContact(contact)
                }
            }
        }
    }

    private fun verifyContactViaLookup(contact: ContactData) {
        Timber.v("[RECEIVED REQUEST] User does not have facts - UD Lookup")
        repo.userLookup(contact.userId) { newContact, error ->
            if (newContact == null) { //Fraudulent
                Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is UNVERIFIED")
                onContactUnverified(contact)
            } else {
                if (newContact.getId().contentEquals(contact.userId)) { //Verified
                    Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is VERIFIED")
                    verifyContact(contact, newContact)
                } else { //Fraudulent
                    Timber.v("[RECEIVED REQUEST] Contact ${contact.userId.toBase64String()} is FRAUDULENT")
                    deleteFraudulentContact(contact)
                }
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
        updateContactStatus(contact.userId, RequestStatus.RECEIVED) {
            addReceivedContactRequest(ContactWrapperBase.from(contact))
            Timber.v("${contact.userId.toBase64String()} has sent a new contact request!")
        }
    }

    private fun onContactUnverified(contact: ContactData) {
        updateContactStatus(contact.userId, RequestStatus.UNVERIFIED)
    }

    private fun deleteFraudulentContact(contact: ContactData) {
        viewSingleRequest()
        subscriptions.add(
            daoRepo.deleteContact(contact)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .doOnError { Timber.v("[RECEIVED REQUEST] Error deleting Fraudulent contact ${contact.userId.toBase64String()}") }
                .doOnSuccess { Timber.v("[RECEIVED REQUEST] Fraudulent contact has been deleted!") }
                .subscribe()
        )
    }

    fun rejectContact(contactId: ByteArray) {
        deleteContact(contactId)
    }

    private fun deleteContact(contactId: ByteArray) {
        val request = preferences.getContactRequest(contactId)
        request?.let { preferences.removeContactRequest(it) }

        subscriptions.add(
            daoRepo.deleteContactFromDb(contactId)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .subscribe()
        )
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


    private fun waitForRoundCompletion(contactId: ByteArray, searchRoundId: Long) {
        Timber.v("Waiting for round $searchRoundId | contact ${contactId.toBase64String()}...")
        subscriptions.add(
            repo.waitForRoundCompletion(
                searchRoundId,
                onRoundCompletionCallback = { roundId, isSuccessful, timedOut ->
                    Timber.v("RoundCompletionCallback ($roundId): successful $isSuccessful timedOut $timedOut")
                    val request = preferences.getContactRequest(contactId, roundId)
                    if (request != null) {
                        if (isSuccessful) completeRound(request)
                        else failRound(request)
                    }
                },
                timeoutMillis = 15000
            )
                .subscribeOn(schedulers.io)
                .subscribe()
        )
    }

    private fun completeRound(roundRequest: ContactRoundRequest) {
        Timber.v("Contact round request tracker for ${roundRequest.contactId.toBase64String()} was removed")
        roundRequest.verifyState = ContactRequestState.SUCCESS
        preferences.updateContactRequest(roundRequest)
        if (roundRequest.isSent) {
            updateContactStatus(roundRequest.contactId, RequestStatus.SENT)
        } else {
            updateContactStatus(roundRequest.contactId, RequestStatus.ACCEPTED)
        }
    }

    private fun failRound(roundRequest: ContactRoundRequest) {
        roundRequest.verifyState = ContactRequestState.FAILED
        preferences.updateContactRequest(roundRequest)
        Timber.v("Contact round request tracker for ${roundRequest.contactId.toBase64String()} was updated")

        if (roundRequest.isSent) {
            updateContactStatus(roundRequest.contactId, RequestStatus.SEND_FAIL)
        } else {
            updateContactStatus(roundRequest.contactId, RequestStatus.CONFIRM_FAIL)
        }
    }

    private fun checkWaitForRounds() {
        val requests = ContactRoundRequest.toRoundRequestsSet(preferences.contactRoundRequests)
        for (contactRequest in requests) {
            Timber.v("Round request waiting: $contactRequest")
            contactRequest.apply {
                when (verifyState) {
                    ContactRequestState.SUCCESS -> {
                        preferences.removeContactRequest(this)
                    }
                    ContactRequestState.VERIFYING -> {
                        updateContactStatus(contactRequest.contactId, RequestStatus.UNVERIFIED)
                        requests.remove(this)
                    }
                }
            }
        }
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

    fun updateContactName(temporaryContact: ContactData) {
        subscriptions.add(
            daoRepo.updateContactName(temporaryContact)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribe()
        )
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
                }.subscribe()
        )
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
                    acceptGroupRequest.value = DataRequestState.Error(err)
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
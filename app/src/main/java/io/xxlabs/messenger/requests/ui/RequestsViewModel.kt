package io.xxlabs.messenger.requests.ui

import android.graphics.Bitmap
import androidx.annotation.StringRes
import androidx.lifecycle.*
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.RequestStatus.*
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.requests.data.contact.ContactRequestsRepository
import io.xxlabs.messenger.requests.data.group.GroupRequestsRepository
import io.xxlabs.messenger.requests.model.ContactRequest
import io.xxlabs.messenger.requests.model.GroupInvitation
import io.xxlabs.messenger.requests.ui.accepted.contact.RequestAccepted
import io.xxlabs.messenger.requests.ui.accepted.contact.RequestAcceptedListener
import io.xxlabs.messenger.requests.ui.accepted.RequestAcceptedUI
import io.xxlabs.messenger.requests.ui.accepted.group.InvitationAccepted
import io.xxlabs.messenger.requests.ui.accepted.group.InvitationAcceptedListener
import io.xxlabs.messenger.requests.ui.details.contact.RequestDetails
import io.xxlabs.messenger.requests.ui.details.contact.RequestDetailsListener
import io.xxlabs.messenger.requests.ui.details.contact.RequestDetailsUI
import io.xxlabs.messenger.requests.ui.details.group.InvitationDetails
import io.xxlabs.messenger.requests.ui.details.group.InvitationDetailsListener
import io.xxlabs.messenger.requests.ui.details.group.InvitationDetailsUI
import io.xxlabs.messenger.requests.ui.details.group.adapter.MemberItem
import io.xxlabs.messenger.requests.ui.list.adapter.*
import io.xxlabs.messenger.requests.ui.nickname.SaveNickname
import io.xxlabs.messenger.requests.ui.nickname.SaveNicknameListener
import io.xxlabs.messenger.requests.ui.nickname.SaveNicknameUI
import io.xxlabs.messenger.requests.ui.send.*
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.support.util.value
import io.xxlabs.messenger.support.view.BitmapResolver
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RequestsViewModel @Inject constructor(
    private val userDataSource: BaseRepository,
    private val daoRepository: DaoRepository,
    private val requestsDataSource: ContactRequestsRepository,
    private val invitationsDataSource: GroupRequestsRepository,
    private val preferences: PreferencesRepository
) : ViewModel(),
    RequestItemListener,
    RequestDetailsListener,
    RequestAcceptedListener,
    InvitationDetailsListener,
    InvitationAcceptedListener,
    SendRequestListener,
    SaveNicknameListener
{
    private val myUserId: ByteArray by lazy { preferences.userId.fromBase64toByteArray() }
    private val groupInviteCache: MutableMap<ByteArray, GroupInviteItem> = mutableMapOf()
    private val contactsCache = MutableStateFlow<List<ContactData>>(listOf())
    private val hiddenRequests = MutableStateFlow<List<RequestItem>>(listOf())
    private var hiddenRequestJob: Job? = null
    private val isLoadingGroupMembers = MutableLiveData(true)

    val showReceivedRequestDetails: StateFlow<ContactRequest?> by ::_showReceivedRequestDetails
    private val _showReceivedRequestDetails = MutableStateFlow<ContactRequest?>(null)

    val showConnectionAccepted: StateFlow<Contact?> by ::_showConnectionAccepted
    private val _showConnectionAccepted = MutableStateFlow<Contact?>(null)

    val navigateToMessages: StateFlow<Contact?> by ::_navigateToMessages
    private val _navigateToMessages = MutableStateFlow<Contact?>(null)

    val showInvitationDetails: StateFlow<GroupInvitation?> by ::_showInvitationDetails
    private val _showInvitationDetails = MutableStateFlow<GroupInvitation?>(null)

    val showGroupAccepted: StateFlow<Group?> by ::_showGroupAccepted
    private val _showGroupAccepted = MutableStateFlow<Group?>(null)

    val navigateToGroupChat: StateFlow<Group?> by ::_navigateToGroupChat
    private val _navigateToGroupChat = MutableStateFlow<Group?>(null)

    // TODO: Implement send request in this ViewModel.
    val sendContactRequest: StateFlow<ContactData?> by ::_sendContactRequest
    private val _sendContactRequest = MutableStateFlow<ContactData?>(null)

    val showCreateNickname: StateFlow<OutgoingRequest?> by ::_showCreateNickname
    private val _showCreateNickname = MutableStateFlow<OutgoingRequest?>(null)

    val customToast: Flow<ToastUI?> by ::_customToast
    private val _customToast = MutableStateFlow<ToastUI?>(null)

    val verifyingInfoDialogUI: LiveData<InfoDialogUI?> by ::_verifyingInfoDialogUI
    private val _verifyingInfoDialogUI = MutableLiveData<InfoDialogUI?>(null)

    // A debounce implementation to prevent running actionClick logic again before completion
    private val actionQueue: MutableList<ByteArray> = mutableListOf()

    private val verifyingInfoDialog: InfoDialogUI by lazy {
        InfoDialogUI.create(
            title = appContext().getString(R.string.request_verifying_popup_title),
            body = appContext().getString(R.string.request_verifying_popup_message)
        )
    }

    init {
        viewModelScope.launch { cacheContactsList() }
        requestsDataSource.resetResentRequests()
        invitationsDataSource.resetResentRequests()
    }

    private suspend fun cacheContactsList() {
        daoRepository.getAllAcceptedContactsLive().asFlow().collect {
            contactsCache.value = it
        }
    }

    private suspend fun getAllRequests() =
        getContactRequests().combine(getGroupInvites()){ requests, invites ->
            requests + invites
        }

    private suspend fun getContactRequests() =
        requestsDataSource.getRequests().map { requestsList ->
            requestsList.map { request ->
                ContactRequestItem(
                    request,
                    resolveBitmap(request.model.photo)
                )
            }
        }.flowOn(Dispatchers.IO)

    private suspend fun resolveBitmap(data: ByteArray?): Bitmap? = withContext(Dispatchers.IO) {
        BitmapResolver.getBitmap(data)
    }

    private suspend fun getGroupInvites() =
        invitationsDataSource.getRequests().map { invitationsList ->
            invitationsList.map { invitation ->
                GroupInviteItem(
                    invitation,
                    getUsername(invitation.model.leader),
                ).also {
                    groupInviteCache[invitation.requestId] = it
                }
            }
        }.stateIn(viewModelScope)

    suspend fun fetchMembers(invitation: GroupInvitation): Flow<List<MemberItem>> {
        isLoadingGroupMembers.value = true
        val members = daoRepository
            .getAllMembers(invitation.model.groupId).value()
            .filterNot { it.userId.contentEquals(myUserId) }
        val fetchedProfiles = fetchProfiles(members)
        return flowOf(getMemberItems(fetchedProfiles, invitation.model))
    }

    private fun GroupMember.toContactData() =
        ContactData(userId = userId, username = username ?: "")

    private suspend fun fetchProfiles(
        members: List<GroupMember>
    ): List<ContactData> {
        val cachedMembers = mutableListOf<ContactData>()
        val unknownMembers = mutableListOf<GroupMember>()

        members.forEach {
            if (it.username.isNullOrBlank()) unknownMembers.add(it)
            else cachedMembers.add(it.toContactData())
        }

        return if (unknownMembers.isEmpty()) cachedMembers
        else cachedMembers + queryUserDiscovery(members)
    }



    private suspend fun queryUserDiscovery(
        members: List<GroupMember>
    ): List<ContactData> = suspendCoroutine { continuation ->
        val userIds = members.map { it.userId }
        userDataSource.getMembersUsername(userIds) { profiles, _, error ->
            if (error.isNullOrBlank()) {
                profiles?.map { user ->
                    ContactData.from(user, VERIFYING)
                }?.run {
                    saveMembersToDatabase(members, this)
                    continuation.resume(this)
                } ?: continuation.resume(listOf())
            } else {
                continuation.resume(listOf())
            }
        }
    }

    private fun saveMembersToDatabase(
        members: List<GroupMember>,
        profiles: List<ContactData>
    ) {
        viewModelScope.launch {
            val groupId = members.firstOrNull()?.groupId ?: return@launch
            val membersToSave = profiles.map {
               GroupMember(
                   groupId = groupId,
                   userId = it.userId,
                   username = it.username
               )
            }
            daoRepository.updateMemberNames(membersToSave).value()
        }
    }

    private suspend fun getMemberItems(
        profiles: List<ContactData>,
        group: Group
    ): List<MemberItem> = profiles.map {
        getContactOrNull(it.userId)?.let { contact ->
            memberFromContact(contact, group)
        } ?: memberFromContact(it, group)
    }
        .sortedByDescending { it.isCreator }
        .apply { isLoadingGroupMembers.value = false }

    private suspend fun getContactOrNull(
        userId: ByteArray
    ): ContactData? = withContext(Dispatchers.Default) {
        contactsCache.value.firstOrNull {
            it.userId.contentEquals(userId)
        }
    }

    private suspend fun memberFromContact(
        contact: Contact,
        group: Group
    ) = MemberItem.from(contact, group, resolveBitmap(contact.photo))

    private suspend fun getUsername(userId: ByteArray): String =
        getUsernameFromCache(userId) ?: getUsernameFromUd(userId)

    private suspend fun getUsernameFromCache(userId: ByteArray): String? =
        withContext(Dispatchers.Default) {
            getContactOrNull(userId)?.displayName
        }

    private suspend fun getUsernameFromUd(userId: ByteArray): String  {
        return try {
            getUser(userId)?.displayName ?: "xxm User"
        } catch (e: Exception) {
            delay(1000)
            getUsernameFromUd(userId)
        }
    }

    private suspend fun getUser(userId: ByteArray): ContactData? =
        suspendCoroutine { continuation ->
            userDataSource.userLookup(userId) { contact, error ->
                val contactData = contact?.let { ContactData.from(it) }
                if (error.isNullOrBlank()) continuation.resume(contactData)
                else continuation.resumeWithException(Exception(error))
            }
        }

    suspend fun getFailedRequests() : Flow<List<RequestItem>> =
        getAllRequests().map { requests ->
            requests
                .filter { it.isFailed() }
                .ifEmpty { showPlaceholder(R.string.requests_empty_placeholder_failed) }
        }

    private fun RequestItem.isFailed(): Boolean {
        return when (request.requestStatus) {
            RESET_FAIL, SEND_FAIL -> true
            else -> false
        }
    }

    suspend fun getSentRequests() : Flow<List<RequestItem>> =
        getAllRequests().map { requests ->
            requests
                .filter { it.isOutgoing() }
                .ifEmpty { showPlaceholder(R.string.requests_empty_placeholder_sent) }
        }

    private fun RequestItem.isOutgoing(): Boolean {
        return when (request.requestStatus) {
            SENT, RESET_SENT, SENDING, RESENT -> true
            else -> false
        }
    }

    suspend fun getReceivedRequests() : Flow<List<RequestItem>> =
        getShownRequests().combine(hiddenRequests) { shown, hidden ->
            shown + hidden
        }

    private suspend fun getShownRequests(): Flow<List<RequestItem>> =
        getAllRequests().map { requests ->
            requests
                .filter { it.isIncoming() }
                .ifEmpty { showPlaceholder(R.string.requests_empty_placeholder_received) }
                .plus(HiddenRequestToggleItem())
        }

    private fun RequestItem.isIncoming(): Boolean {
        return when (request.requestStatus) {
            VERIFYING, VERIFIED, VERIFICATION_FAIL -> true
            else -> false
        }
    }

    override fun onShowHiddenToggled(enabled: Boolean) {
        if (!enabled) {
            hiddenRequestJob?.cancel()
            hiddenRequests.value = listOf()
            return
        }

        hiddenRequestJob = viewModelScope.launch {
            getAllRequests().cancellable().collect { requests ->
                val hiddenList = requests
                    .filter { it.isHidden() }
                    .ifEmpty { showPlaceholder(R.string.requests_empty_placeholder_hidden) }
                hiddenRequests.value = hiddenList
            }
        }
    }

    private fun showPlaceholder(@StringRes text: Int): List<RequestItem> =
        listOf(EmptyPlaceholderItem(text = appContext().getString(text)))

    private fun RequestItem.isHidden(): Boolean {
        return when (request.requestStatus) {
            HIDDEN -> true
            else -> false
        }
    }

    override fun onItemClicked(request: RequestItem) {
        when (request.request.requestStatus) {
            VERIFYING -> showVerifyingInfo()
            VERIFIED, HIDDEN -> showDetails(request)
        }
    }

    private fun showDetails(item: RequestItem) {
        when (item) {
            is ContactRequestItem -> showRequestDialog(item.contactRequest)
            is GroupInviteItem -> showInvitationDialog(item.invite)
        }
    }

    override fun onActionClicked(request: RequestItem) {
        if (actionQueue.contains(request.id)) return
        else actionQueue.add(request.id)

        when (request.request.requestStatus) {
            VERIFYING -> showVerifyingInfo().also { actionQueue.remove(request.id) }
            SEND_FAIL, SENT -> resendRequest(request)
            VERIFICATION_FAIL -> retryVerification(request)
        }
    }

    private fun showVerifyingInfo() {
        _verifyingInfoDialogUI.value = verifyingInfoDialog
    }

    fun onVerifyingInfoHandled() {
        _verifyingInfoDialogUI.value = null
    }

    override fun markAsSeen(item: RequestItem) {
        if (item.request.unread) {
            when (item) {
                is ContactRequestItem -> requestsDataSource.markAsSeen(item.contactRequest)
                is GroupInviteItem -> invitationsDataSource.markAsSeen(item.invite)
            }
        }
    }

    private fun showRequestDialog(request: ContactRequest) {
        _showReceivedRequestDetails.value = request
    }

    fun onRequestDialogShown() {
        _showReceivedRequestDetails.value = null
    }

    fun onNewConnectionShown() {
        _showConnectionAccepted.value = null
    }

    private fun retryVerification(item: RequestItem) {
        viewModelScope.launch {
            (item.request as? ContactRequest)?.let {
                when (requestsDataSource.verify(it)) {
                    true -> {
                        Timber.d("Verification successful")
                        actionQueue.remove(item.id)
                    }
                    false -> {
                        Timber.d("Verification failed")
                        actionQueue.remove(item.id)
                    }
                }
            }
        }
    }

    private fun resendRequest(item: RequestItem) {
        when (item) {
            is ContactRequestItem -> requestsDataSource.send(item.request as ContactRequest)
            is GroupInviteItem -> invitationsDataSource.send(item.request as GroupInvitation)
        }
        onResend(item)
    }

    private fun onResend(item: RequestItem) {
        val request = when(item) {
            is GroupInviteItem -> "Invitation"
            else -> "Request"
        }
        _customToast.value = ToastUI.create(
            body = "$request successfully resent to ${item.request.name}"
        )
        actionQueue.remove(item.id)
    }

    suspend fun getRequestDetails(contactRequest: ContactRequest): Flow<RequestDetailsUI?> =
        getContactRequests().flatMapLatest { requestsList ->
            flow {
                requestsList.firstOrNull { request ->
                    request.id.contentEquals(contactRequest.requestId)
                }?.run {
                    emit(RequestDetails(contactRequest, this@RequestsViewModel))
                }
            }
        }

    fun getRequestAccepted(contact: Contact): RequestAcceptedUI =
        RequestAccepted(contact, this@RequestsViewModel)

    override fun acceptRequest(request: ContactRequest, nickname: String?) {
        viewModelScope.launch {
            saveContact(request)?.let { contact ->
                if (requestsDataSource.accept(request)) {
                    val updatedContact = updateNickname(request.model, nickname)
                    showNewConnectionDialog(updatedContact ?: contact)
                }
            }
        }
    }

    private suspend fun updateNickname(user: Contact, nickname: String?): ContactData? {
        return nickname?.let { nick ->
            (user as? ContactData)?.let { contactData ->
                val updatedContact = contactData.copy(nickname = nick)
                val rowsUpdated = daoRepository
                    .updateContactNickname(updatedContact)
                if (rowsUpdated > 0) updatedContact
                else contactData
            }
        }
    }

    private suspend fun saveContact(request: ContactRequest): Contact? {
        val rowsUpdated = updateContactStatus(request.model.userId, ACCEPTED)
        return if (rowsUpdated > 0) request.model else null
    }

    private fun showNewConnectionDialog(contact: Contact) {
        _showConnectionAccepted.value = contact
    }

    private suspend fun updateContactStatus(userId: ByteArray, status: RequestStatus): Int =
        daoRepository.updateContactState(userId, status).value()

    override fun hideRequest(request: ContactRequest) {
        viewModelScope.launch {
            if (ignoreContact(request) > 0) requestsDataSource.reject(request)
        }
    }

    private suspend fun ignoreContact(request: ContactRequest): Int =
        updateContactStatus(request.model.userId, HIDDEN)

    override fun sendMessage(contact: Contact) {
        _navigateToMessages.value = contact
    }

    fun onNavigateToMessagesHandled() {
        _navigateToMessages.value = null
    }

    private fun showInvitationDialog(invite: GroupInvitation) {
        _showInvitationDetails.value = invite
    }

    fun onInvitationDialogShown() {
        _showInvitationDetails.value = null
    }

    suspend fun getInvitationDetails(invitation: GroupInvitation): Flow<InvitationDetailsUI?> {
        return getCachedDetails(invitation)?.let { flowOf(it) }
            ?: getGroupInvites().flatMapLatest { invitesList ->
                flow {
                    invitesList.firstOrNull { invite ->
                        invite.id.contentEquals(invitation.requestId)
                    }?.run {
                        emit(InvitationDetails(
                            this,
                            this@RequestsViewModel,
                            isLoadingGroupMembers)
                        )
                    }
                }
            }
    }

    private fun getCachedDetails(invitation: GroupInvitation): InvitationDetailsUI? =
        groupInviteCache[invitation.requestId]?.let {
            InvitationDetails(it, this, isLoadingGroupMembers)
        }

    fun getInvitationAccepted(group: Group): RequestAcceptedUI =
        InvitationAccepted(group, this@RequestsViewModel)

    override fun acceptInvitation(invitation: GroupInvitation) {
        viewModelScope.launch {
            joinGroup(invitation)?.let { group ->
                if (invitationsDataSource.accept(invitation)) showGroupAcceptedDialog(group)
            }
        }
    }

    private suspend fun joinGroup(invitation: GroupInvitation): Group? {
        val successful = userDataSource.acceptGroupInvite(invitation.model.serial).value()
        return if (successful) invitation.model else null
    }

    private fun showGroupAcceptedDialog(group: Group) {
        (group as? GroupData)?.copy(status = ACCEPTED.value)?.let { acceptedGroup ->
            _showGroupAccepted.value = acceptedGroup
        }
    }

    fun onGroupAcceptedShown() {
        _showGroupAccepted.value = null
    }

    override fun hideInvitation(invitation: GroupInvitation) {
        invitationsDataSource.reject(invitation)
    }

    override fun openGroupChat(group: Group) {
        _navigateToGroupChat.value = group
    }

    fun onNavigateToGroupHandled() {
        _navigateToGroupChat.value = null
    }

    fun contactRequestTo(user: Contact): SendRequestUI =
        SendRequest(
            sender = CurrentUser(
                userDataSource.getStoredEmail().ifBlank { null },
                userDataSource.getStoredPhone().ifBlank { null }
            ),
            receiver = user,
            listener = this@RequestsViewModel
        )

    override fun sendRequest(request: OutgoingRequest) {
        with (request.sender) {
            preferences.shareEmailWhenRequesting = !email.isNullOrBlank()
            preferences.sharePhoneWhenRequesting = !phone.isNullOrBlank()
        }

        _sendContactRequest.value = request.receiver as? ContactData
        _showCreateNickname.value = request
    }

    fun onSendRequestHandled() {
        _sendContactRequest.value = null
    }

    fun getSaveNickname(request: OutgoingRequest): SaveNicknameUI =
        SaveNickname(request, this)

    fun onShowCreateNicknameHandled() {
        _showCreateNickname.value = null
    }

    override fun saveNickname(request: OutgoingRequest, nickname: String?) {
        viewModelScope.launch {
            updateNickname(request.receiver, nickname)
        }
    }


    fun onShowToastHandled() {
        _customToast.value = null
    }
}

data class CurrentUser(
    override val email: String?,
    override val phone: String?
) : RequestSender
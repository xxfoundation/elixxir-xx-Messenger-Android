package io.xxlabs.messenger.ui.main.groups

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.view.Gravity
import androidx.lifecycle.*
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.bindings.wrapper.groups.id.IdListBase
import io.xxlabs.messenger.data.data.AvatarWrapper
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.*
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.misc.DummyGenerator
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.ui.main.chat.ChatMessagesViewModel
import timber.log.Timber

class GroupMessagesViewModel @AssistedInject constructor(
    repo: BaseRepository,
    daoRepo: DaoRepository,
    schedulers: SchedulerProvider,
    preferences: PreferencesRepository,
    application: Application,
    @Assisted val groupId: ByteArray,
    @Assisted val cachedGroup: GroupData? = null
) : ChatMessagesViewModel<GroupMessage>(
    repo, daoRepo, schedulers, preferences, application, groupId
) {
    /* Group Messages */

    private val groupData = MutableLiveData<GroupData?>()

    val memberAvatars: LiveData<List<AvatarWrapper>> get() = _memberAvatars
    private val _memberAvatars = MutableLiveData<List<AvatarWrapper>>()
    private val missingAvatars = mutableListOf<UserId>()

    val groupMembers: LiveData<UserIdToUsernameMap> get() = _groupMembers
    private val _groupMembers = MutableLiveData<UserIdToUsernameMap>()

    private var memberIdToUsernameMap: UserIdToUsernameMap = UserIdToUsernameMap(hashMapOf())

    /* UI */

    init {
        cachedGroup?.let {
            groupData.postValue(it)
            getGroupMembers(it.groupId)
            getMessages(it.groupId)
        } ?: run {
            startSubscription(groupId)
        }
    }

    override fun startSubscription(chatId: ByteArray) {
        getGroupData(chatId)
        getGroupMembers(chatId)
        getMessages(chatId)
    }

    private fun getGroupData(groupId: ByteArray) {
        subscriptions.add(
            daoRepo.getGroup(groupId)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onError = { Timber.e(it) },
                    onSuccess = { groupData.postValue(it) }
                )
        )
    }

    private fun getGroupMembers(groupId: ByteArray) {
        subscriptions.add(
            daoRepo.getAllMembers(groupId)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onError = { err ->
                        Timber.e(err)
                    },
                    onSuccess = { memberList ->
                        getMemberUsernames(memberList)
                    }
                )
        )
    }

    private fun getMemberUsernames(members: List<GroupMember>) {
        members.forEach {
            val userId = UserId.from(it.userId)
            val username = Username(it.username ?: "Retrieving user...")
            memberIdToUsernameMap[userId] = username
        }
        val membersMissingUsername = members.filter {
            it.username.isNullOrBlank() && !it.userId.contentEquals(preferences.getUserId())
        }
        val userIdsToFetch = membersMissingUsername.map { it.userId }
        repo.getMembersUsername(userIdsToFetch, ::onUsernamesFetched)

        _groupMembers.value = memberIdToUsernameMap
    }

    private fun onUsernamesFetched(
        users: List<ContactWrapperBase>?,
        ids: IdListBase,
        error: String?
    ) {
        users?.apply {
            forEach {
                val userId = UserId.from(it.getId())
                val username = Username(it.getUsernameFact())
                memberIdToUsernameMap[userId] = username
            }
            cacheUsernames()
            loadAvatars()
            _groupMembers.postValue(memberIdToUsernameMap)
        }
    }

    private fun cacheUsernames() {
       subscriptions.add(
            daoRepo.getAllMembers(groupId)
                .flatMap { members ->
                    for (member in members) {
                        val userId = member.userId.decodeToString()
                        if (memberIdToUsernameMap.containsKey(userId)) {
                            member.username = memberIdToUsernameMap[userId]
                        }
                    }
                    daoRepo.updateMemberNames(members)
                }
                .subscribeOn(schedulers.io)
                .doOnError { err ->
                    Timber.e("[GROUP CHAT] Update members error: ${err.localizedMessage}")
                }
                .doOnSuccess { Timber.v("[GROUP CHAT] Update members SUCCESS") }
                .subscribe()
        )
    }

    private fun loadAvatars() {
        val usernamesList = memberIdToUsernameMap.values
            .map { it.value }
        subscriptions.add(
            daoRepo.getContacts(usernamesList)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .doOnError { Timber.e(it) }
                .doOnSuccess { contacts ->
                    _memberAvatars.value = mutableListOf()
                    missingAvatars.addAll(memberIdToUsernameMap.keys)

                    createContactAvatars(contacts)
                    createMyAvatar()
                    createRemainingAvatars()
                }.subscribe()
        )
    }

    private fun createContactAvatars(
        contacts: List<ContactData>
    ) {
        val contactAvatars = mutableListOf<AvatarWrapper>()
        for (contact in contacts) {
            contactAvatars.add(
                AvatarWrapper(
                    contact.username,
                    contact.userId,
                    contact.photo
                )
            )
            missingAvatars.remove(UserId.from(contact.userId))
        }
        _memberAvatars.value = _memberAvatars.value?.plus(contactAvatars)
    }

    private fun createMyAvatar() {
        val myProfilePhoto = when {
            preferences
                .userPicture
                .fromBase64toByteArray()
                .contentEquals(byteArrayOf()) -> null
            else -> preferences.userPicture.fromBase64toByteArray()
        }
        _memberAvatars.value = _memberAvatars.value?.plus(
            AvatarWrapper(
                repo.getStoredUsername(),
                getUserId(),
                myProfilePhoto
            )
        )
        missingAvatars.remove(UserId.from(getUserId()))
    }

    private fun createRemainingAvatars() {
        val remainingAvatars = mutableListOf<AvatarWrapper>()
        for (entry in memberIdToUsernameMap) {
            if (missingAvatars.contains(entry.key)) {
                remainingAvatars.add(
                    AvatarWrapper(
                        entry.value.value,
                        entry.key.value.encodeToByteArray(),
                        null
                    )
                )
            }
            missingAvatars.remove(entry.key)
        }
        _memberAvatars.value = _memberAvatars.value?.plus(remainingAvatars)
    }

    override fun getMessages(chatId: ByteArray) {
        Timber.v("Subscribing to conversation id: %s", chatId.toBase64String())
        val groupMessages = daoRepo.getGroupMessages(chatId)
        _chatData = groupMessages
//            .mapByPage {
//                it.apply { verifyUnsentMessages(this) }
//            }
            .toLiveData(
                Config(
                    pageSize = 50,
                    prefetchDistance = 100,
                    enablePlaceholders = true
                )
            ) as LiveData<PagedList<GroupMessage>>
    }

    override fun sendMessage(msg: GroupMessage) {
        subscriptions.add(
            daoRepo.insertGroupMessage(msg as GroupMessageData)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .subscribeBy(
                    onError = { t ->
                        Timber.e("Error saving msg ${t.localizedMessage}")
                    },
                    onSuccess = { msgId ->
                        Timber.v("Message was saved into existing chat with id $msgId")
                        msg.id = msgId
                        sendGroupMessageOnClient(msg)
                    })
        )
    }

    private fun sendGroupMessageOnClient(msg: GroupMessage) {
        subscriptions.add(
            repo.sendGroupMessage(
                preferences.getUserId(),
                groupId,
                groupId,
                msg.payload
            ).subscribeOn(schedulers.single)
                .observeOn(schedulers.io)
                .subscribeBy(
                    onError = { t ->
                        Timber.e(t)
                        Timber.v("Error sending msg")
                        msg.status = MessageStatus.FAILED.value
                        updateMessage(msg)
                    },
                    onSuccess = { sendReport ->
                        if (isMockVersion()) createMockedMsg()
                        val sentMessage = (msg as GroupMessageData).copy(
                            uniqueId = sendReport.getMessageID(),
                            roundUrl = sendReport.getRoundUrl()
                        )
                        messageSent(sentMessage)
                    }
                )
        )
    }

    private fun messageSent(msg: GroupMessage) {
        msg.status = MessageStatus.SENT.value
        updateMessage(msg)
    }

    override fun updateMessage(
        msg: GroupMessage,
        isResending: Boolean
    ) {
        subscriptions.add(daoRepo.updateGroupMessage(msg as GroupMessageData)
            .subscribeOn(schedulers.io)
            .subscribeBy(
                onError = { t ->
                    Timber.e("Error updating msg with id ${msg.id} \n${t.localizedMessage}")
                },
                onSuccess = {
                    Timber.v("Successfully updated msg with id: $it")

                    if (isResending) {
                        Timber.v("Resending msg: $msg")
                        sendGroupMessageOnClient(msg)
                    }
                }
            )
        )
    }

    override fun createMockedMsg() {
        val dummyMsg = DummyGenerator.getMessageDummy()
        Timber.v("Inserting dummy: $dummyMsg")
        val random = (0..10).random()
        var senderId: ByteArray
        do {
            senderId = memberIdToUsernameMap.keys.random().encodeToByteArray()
        } while (senderId.contentEquals(preferences.getUserId()))

        if (random < 6) {
            Timber.v("Dummy inserted: $dummyMsg")
            subscriptions.add(
                daoRepo.insertGroupMessage(
                    GroupMessageData(
                        groupId = groupId,
                        sender = senderId,
                        receiver = groupId,
                        payload = dummyMsg,
                        timestamp = Utils.getCurrentTimeStamp(),
                        status = MessageStatus.RECEIVED.value
                    )
                ).subscribeOn(schedulers.io).subscribe()
            )
        } else {
            Timber.v("Dummy NOT inserted")
        }
    }

    override fun deleteMessagesById(messageIds: List<Long>) {
        subscriptions.add(daoRepo.deleteGroupMessages(messageIds)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { it.printStackTrace() },
                onSuccess = {
                    messageIds.forEach { _messageDeleted.value = it }
                    onSelectionCleared()
                    Timber.v("Messages were deleted successfully: $messageIds")
                }
            )
        )
    }

    override fun onDeleteAll() {
        subscriptions.addAll(
            daoRepo.deleteAllGroupMessages(groupId)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onError = { it.printStackTrace() },
                    onSuccess = { Timber.v("Messages were deleted successfully") }
                )
        )
    }

    companion object {
        fun provideFactory(
            assistedFactory: GroupMessagesViewModelFactory,
            groupId: ByteArray,
            group: GroupData? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(groupId, group) as T
            }
        }
    }

    override val messageText = MutableLiveData<String?>()

    override val isContactPending = Transformations.map(groupData) { group ->
        group?.let { it.status != RequestStatus.ACCEPTED.value }
            ?: false
    }

    override val attachButtonVisible = MutableLiveData(false)

    override val cancelAttachmentVisible = MutableLiveData(false)

    override val audioPreviewVisible = MutableLiveData(false)

    override val audioPreviewPauseVisible = MutableLiveData(false)

    override val messageInputVisible = MutableLiveData(true)

    override val messageInputEnabled = Transformations.map(groupData) { group ->
        group?.let {
            it.status == RequestStatus.ACCEPTED.value
        } ?: false
    }

    override val sendButtonVisible = MutableLiveData(true)

    override fun onSendButtonClicked() = onSendMessage()
    
    override val sendButtonEnabled = Transformations.map(messageText) {
        it.isValidMessage()
    }

    override val contactPendingMessage: LiveData<Spanned?> = Transformations.map(groupData) { group ->
        group?.let {
            if (it.status != RequestStatus.ACCEPTED.value) {
                getGroupPendingSpanned()
            } else null
        }
    }

    private fun getGroupPendingSpanned(): Spanned {
        val app = getApplication<XxMessengerApplication>()
        val placeholderText = app.getString(R.string.group_invitation_pending)
        return SpannableString(placeholderText)
    }

    override val chatEmptyMessage: LiveData<Spanned?> = MutableLiveData()

    override val emptyChatPlaceholder = MutableLiveData(R.drawable.ellipse_4)

    override val micButtonVisible = MutableLiveData(false)
    override val stopRecordingVisible = MutableLiveData(false)

    override fun onStopRecordingClicked() {}

    override fun onRemoveAttachmentClicked(uri: Uri) {}

    override val GroupMessage.failedDelivery: Boolean
        get() {
            return when (status) {
                MessageStatus.TIMEOUT.value -> true
                MessageStatus.PENDING.value -> {
                    (System.currentTimeMillis() - timestamp) > DELIVERY_TIMEOUT_MS
                }
                else -> false
            }
        }

    override fun onSendMessage() {
        sendTextMessage()
        onMessageSent()
    }

    override val replyUsername = Transformations.map(reply) { message ->
        message?.sender?.let {
            when {
                it.contentEquals(getUserId()) -> "You"
                else -> memberIdToUsernameMap[UserId.from(it)].value
            }
        } ?: "Unknown user"
    }

    private fun sendTextMessage() {
        if (messageText.value.isValidMessage()) {
            val replyWrapper = reply.value?.let {
                wrapReply(it)
            }
            val payload = createPayload(replyWrapper, messageText.value!!)
            val message = GroupMessageData(
                groupId = groupId,
                status = MessageStatus.PENDING.value,
                payload = payload,
                timestamp = Utils.getCurrentTimeStamp(),
                unread = false,
                sender = preferences.getUserId(),
                receiver = groupId
            )

            sendMessage(message)
        }
    }

    override val chatTitle = Transformations.map(groupData) {
        it?.name ?: "Group Chat"
    }

    fun leaveGroup() {
        groupData.value?.let { onLeaveGroup(it) }
    }

    private fun onLeaveGroup(groupData: GroupData) {
        subscriptions.add(repo.leaveGroup(groupId)
            .flatMap { daoRepo.deleteGroup(groupData) }
            .flatMap { daoRepo.deleteAllGroupMessages(groupId) }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { _errorMessage.value = Exception(it.localizedMessage) },
                onSuccess = { _leftGroup.value = true }
            )
        )
    }

    val leftGroup: LiveData<Boolean> get() = _leftGroup
    private val _leftGroup = MutableLiveData(false)

    override val chatTitleGravity: Int = Gravity.CENTER_HORIZONTAL

    override val chatIcon = MutableLiveData<Bitmap?>()

    override val _openFileBrowser = MutableLiveData(false)
    override val _startCamera = MutableLiveData(false)
    override val _openGallery = MutableLiveData(false)
    override val _selectAttachmentsVisible = MutableLiveData(false)
    override val _startRecording = MutableLiveData(false)
    override val _recordingDuration = MutableLiveData(0)

    override fun onAttachFileButtonClicked() {}
    override fun onCancelAttachmentClicked() {}
    override fun onCameraButtonClicked() {}
    override fun onGalleryButtonClicked() {}
    override fun onFilesButtonClicked() {}
    override fun onFileBrowserHandled() {}
    override fun onCameraHandled() {}
    override fun onGalleryHandled() {}
    override fun onContactClicked() {}

    override val lastMessage: LiveData<GroupMessage?> =
        Transformations.switchMap(groupData) {
            daoRepo.getLastGroupMessageLiveData(it?.groupId ?: byteArrayOf())
                    as LiveData<GroupMessage?>
        }

    override fun GroupMessage.canBeReplied(): Boolean {
        return !roundUrl.isNullOrEmpty() &&
        (status == MessageStatus.SENT.value || status == MessageStatus.RECEIVED.value)
    }
}

@JvmInline
value class UserId(val value: String) {
    fun encodeToByteArray() = value.encodeToByteArray()

    companion object {
        fun from(byteArray: ByteArray) = UserId(byteArray.decodeToString())
    }
}

@JvmInline
value class Username(val value: String)

@JvmInline
value class UserIdToUsernameMap(private val value: MutableMap<UserId, Username>) {
    operator fun set(userId: UserId, username: Username) {
        value[userId] = username
    }

    operator fun get(userId: UserId): Username = value[userId] ?: Username("")

    operator fun get(userId: String): String = value[UserId(userId)]?.value ?: ""

    fun containsKey(userId: String): Boolean = value.containsKey(UserId(userId))

    val values
        get() = value.values

    val keys
        get() = value.keys

    operator fun iterator() = value.iterator()
}

@AssistedFactory
interface GroupMessagesViewModelFactory {
    fun create(groupId: ByteArray, groupData: GroupData? = null): GroupMessagesViewModel
}
package io.xxlabs.messenger.ui.main.chat

import android.app.Application
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import androidx.lifecycle.*
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import data.proto.CMIXText
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.data.room.model.PrivateMessageData
import io.xxlabs.messenger.filetransfer.*
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.misc.DummyGenerator
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.support.view.BitmapResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.NoSuchElementException
import kotlin.collections.HashMap
import io.xxlabs.messenger.support.appContext

private const val MINIMUM_RECORDING_DURATION_MS = 1000

class PrivateMessagesViewModel @AssistedInject constructor(
    repo: BaseRepository,
    daoRepo: DaoRepository,
    schedulers: SchedulerProvider,
    preferences: PreferencesRepository,
    application: Application,
    @Assisted private val contactId: ByteArray,
    @Assisted private val cachedContact: ContactData? = null
) : ChatMessagesViewModel<PrivateMessage>(
        repo, daoRepo, schedulers, preferences, application, contactId
) {

    /* File transfers */

    private val fileRepository: FileTransferRepository by lazy { repo.fileRepository }
    private val sentFilesMessagesMap: MutableMap<SentFile, PrivateMessage> = HashMap()
    private val _attachments: MutableLiveData<List<Uri>> = MutableLiveData()
    val attachments: LiveData<List<Uri>> = _attachments

    /**
     * Get a new instance of a [SentFileProgressCallback].
     */
    private val sentProgressListener
        get() = object: SentFileProgressCallback {
            override fun onProgressUpdate(
                isComplete: Boolean,
                chunksSent: Long,
                chunksDelivered: Long,
                chunksTotal: Long,
                error: Exception?
            ) {
                error?.let {
                    Timber.d("[File Transfer] Error: ${error.message ?: "none provided"}")
                    sentFilesMessagesMap.getAssociatedMessage(this)
                        ?.updateStatus(MessageStatus.FAILED)
                    return
                }
                if (isComplete) {
                    Timber.d("[File Transfer] Successful")
                    sentFilesMessagesMap.getAssociatedMessage(this)
                        ?.apply {
                            updateStatus(MessageStatus.SENT)
                            sentFilesMessagesMap.removeAssociatedSentFile(this)
                            fileRepository.closeSend(TransferId(uniqueId))
                        }
                    return
                }
                Timber.d("[File Transfer] Sent $chunksSent/$chunksTotal chunks.")
            }
        }

    /* Messages */

    lateinit var contact: ContactData
    private val contactData: MutableLiveData<ContactData> = MutableLiveData()

    init {
        cachedContact?.let {
            this.contact = it
            contactData.value = it
            getMessages(it.userId)
        } ?: run {
            startSubscription(contactId)
        }
    }

    /* UI */

    override val replyUsername = Transformations.map(reply) { message ->
        when {
            message?.sender.contentEquals(getUserId()) -> "You"
            ::contact.isInitialized -> contact.nickname
            else -> "Unknown"
        }
    }

    override fun PrivateMessage.canBeReplied(): Boolean {
        return isTextMessage()
                && (status == MessageStatus.SENT.value || status == MessageStatus.RECEIVED.value)
    }

    private fun PrivateMessage.isTextMessage(): Boolean = fileType.isNullOrEmpty()

    /**
     * Text entered by the user to send as a message.
     */
    override val messageText = MutableLiveData<String?>()

    /**
     * The title for the current chat, typically the contact name.
     */
    override val chatTitle = Transformations.map(contactData) {
        it.displayName
    }

    override val chatTitleGravity = Gravity.START

    /**
     * The contact's profile photo, or a default one if none was chosen.
     */
    override val chatIcon = Transformations.map(contactData) {
        it.photo?.let { contactPhoto ->
            BitmapResolver.getBitmap(contactPhoto)
        }
    }

    override val chatEmptyMessage: LiveData<Spanned?> = Transformations.map(_chatData) {
        if (it.isEmpty() && contactPendingMessage.value.isNullOrEmpty()) {
            getEmptyTextSpannable()
        } else null
    }

    private fun getEmptyTextSpannable(): Spanned {
        val app = getApplication<XxMessengerApplication>()
        val highlight = app.getColor(R.color.brand_default)
        val placeholderText = app.getString(
            R.string.chat_empty_placeholder,
            if (::contact.isInitialized) contact.displayName else "your connection"
        )

        val firstSpanText = "quantum-secure"
        val firstSpanStartIndex = placeholderText.indexOf(firstSpanText, ignoreCase = true)
        val firstSpanEndIndex = firstSpanStartIndex + firstSpanText.length

        val secondSpanText = "Say hello"
        val secondSpanStartIndex = placeholderText.indexOf(secondSpanText, ignoreCase = true)

        return SpannableString(placeholderText).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                firstSpanStartIndex,
                firstSpanEndIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(highlight),
                secondSpanStartIndex,
                placeholderText.length-1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    /**
     * Whether the contact has accepted the add request or not.
     */
    override val isContactPending = Transformations.map(contactData) {
        it.status != RequestStatus.ACCEPTED.value
    }

    /**
     * Controls the visibility of the button that allows selection of attachments.
     */
    override val attachButtonVisible: LiveData<Boolean> get() = _attachButtonEnabled
    val _attachButtonEnabled = MutableLiveData(true)

    /**
     * Controls the visibility of the button that cancels the selection of attachments.
     */
    override val cancelAttachmentVisible: LiveData<Boolean> get() = _cancelAttachmentVisible
    private val _cancelAttachmentVisible = MutableLiveData(false)

    /**
     * Controls the visibility of UI to play recorded audio before sending.
     */
    override val audioPreviewVisible: LiveData<Boolean> get() = _audioPreviewVisible
    private val _audioPreviewVisible = MutableLiveData(false)

    /**
     * Controls the visibility for the UI responsible for pausing audio preview playback.
     */
    override val audioPreviewPauseVisible: LiveData<Boolean> get() = _audioPreviewPauseVisible
    private val _audioPreviewPauseVisible = MutableLiveData(false)

    /**
     * Controls the visibility for the input area where the user writes a new message.
     */
    override val messageInputVisible: LiveData<Boolean> get() = _messageInputVisible
    private val _messageInputVisible = MutableLiveData(true)

    /**
     * Controls whether the input area should be enabled.
     */
    override val messageInputEnabled = Transformations.map(isContactPending) { pending ->
        !pending
    }

    /**
     * Controls the visibility of the send button.
     */
    override val sendButtonVisible: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        var textMessage = false
        var audioMessage = false

        addSource(messageText) { input ->
            textMessage = !input.isNullOrBlank()
            value = textMessage || audioMessage
        }
        addSource(_audioPreviewVisible) { isAudioMessage ->
            audioMessage = isAudioMessage
            value = textMessage || audioMessage
        }
    }

    /**
     * Controls whether the send buttons should be enabled.
     */
    override val sendButtonEnabled: LiveData<Boolean> = MediatorLiveData<Boolean>()
        .apply {
            var message: String? = null
            var pending = true
            var attachments = false
            var audioPreview = false

            addSource(messageText) {
                message = it
                value = (message.isValidMessage() || attachments || audioPreview) && !pending
            }
            addSource(isContactPending) { isPending ->
                pending = isPending
                value = (message.isValidMessage() || attachments || audioPreview) && !pending
            }
            addSource(_attachments) {
                attachments = it.isNotEmpty()
                value = (message.isValidMessage() || attachments || audioPreview) && !pending
            }
            addSource(_audioPreviewVisible) {
                audioPreview = it
                value = (message.isValidMessage() || attachments || audioPreview) && !pending
            }
        }

    /**
     * The placeholder image to be displayed depending on if the contact has accepted,
     * and if so, if no messages have yet been sent.
     */
    override val emptyChatPlaceholder = Transformations.map(isContactPending) { pending ->
        if (pending) R.drawable.ellipse_5
        else R.drawable.ellipse_4
    }

    override val stopRecordingVisible: LiveData<Boolean> get() = _stopRecordingVisible

    /**
     * Called when the stop button is clicked.
     */
    val previewRecording: LiveData<Boolean> get() = _previewRecording
    val sendAudioMessage: LiveData<Boolean> get() = _sendAudioMessage

    /**
     * The placeholder text to be displayed if the contact has not yet accepted.
     * Returns blank if the contact has accepted.
     */
    override val contactPendingMessage: LiveData<Spanned?> = Transformations.map(contactData) {
        if (it.status != RequestStatus.ACCEPTED.value) {
            getContactPendingSpannable(it.displayName)
        } else null
    }

    private fun getContactPendingSpannable(displayName: String): Spanned {
        val app = getApplication<XxMessengerApplication>()
        val highlight = app.getColor(R.color.brand_default)

        val placeholderText1 = app.getString(
            R.string.chat_contact_pending_placeholder,
            displayName,
        )
        val firstSpanStartIndex = placeholderText1.indexOf(displayName, ignoreCase = true)
        val firstSpanEndIndex = firstSpanStartIndex + displayName.length
        val spannedPart1 = SpannableString(placeholderText1).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                firstSpanStartIndex,
                firstSpanEndIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val placeholderText2 = app.getString(
            R.string.chat_contact_pending_placeholder2,
            displayName
        )
        val secondSpanStartIndex = placeholderText2.indexOf(displayName, ignoreCase = true)
        val secondSpanEndIndex = secondSpanStartIndex + displayName.length
        val spannedPart2 = SpannableString(placeholderText2).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                secondSpanStartIndex,
                secondSpanEndIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return SpannableStringBuilder().apply {
            append(spannedPart1)
            append(spannedPart2)
        }
    }

    override val _openFileBrowser = MutableLiveData(false)
    override val _startCamera = MutableLiveData(false)
    override val _openGallery = MutableLiveData(false)
    override val _selectAttachmentsVisible = MutableLiveData(false)
    private val _stopRecordingVisible = MutableLiveData(false)
    override val _startRecording = MutableLiveData(false)
    private val _previewRecording = MutableLiveData(false)
    override val _recordingDuration = MutableLiveData(0)
    private val _sendAudioMessage = MutableLiveData(false)

    /**
     * Controls visibility of the mic button.
     */
    override val micButtonVisible: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        var blankTextInput = true
        var audioPreviewHidden = true
        var recording = false

        addSource(messageText) { textMessage ->
            blankTextInput = textMessage.isNullOrBlank()
            value = blankTextInput && audioPreviewHidden && !recording
        }
        addSource(audioPreviewVisible) { previewingAudioMessage ->
            audioPreviewHidden = !previewingAudioMessage
            value = blankTextInput && audioPreviewHidden && !recording
        }
        addSource(stopRecordingVisible) { recordingAudioMessage ->
            recording = recordingAudioMessage
            value = blankTextInput && audioPreviewHidden && !recording
        }
    }

    val stopRecording: LiveData<Boolean> by ::_stopRecording
    private val _stopRecording = MutableLiveData(true)

    override val lastMessage: LiveData<PrivateMessage?> =
        Transformations.map(daoRepo.getLastMessageLiveData()) { it }

    /* Functions */

    private fun showRecordingUi() {
        _messageInputVisible.value = false
        _startRecording.value = false
        _stopRecordingVisible.value = true
        _attachButtonEnabled.value = false
    }

    private var timerJob: Job? = null

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch(Dispatchers.Main) {
            val totalSeconds = 60
            for (second in 0 until totalSeconds) {
                _recordingDuration.value = second * 1000
                delay(1000)
            }
            onStopRecordingClicked()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _recordingDuration.value = 0
    }

    override fun onAttachFileButtonClicked() {
        _attachButtonEnabled.value = false
        _selectAttachmentsVisible.value = true
    }

    override fun onCancelAttachmentClicked() {
        if (_audioPreviewVisible.value == true) {
            restoreMessagingUi()
        } else {
            _selectAttachmentsVisible.value = false
            _attachButtonEnabled.value = true
        }
    }

    override fun onCameraButtonClicked() {
        _startCamera.value = true
    }

    override fun onGalleryButtonClicked() {
        _openGallery.value = true
    }

    override fun onFilesButtonClicked() {
        _openFileBrowser.value = true
    }

    /**
     * Called after the file browser event was handled.
     */
    override fun onFileBrowserHandled() {
        _attachButtonEnabled.value = true
        _selectAttachmentsVisible.value = false
        _openFileBrowser.value = false
    }

    /**
     * Called after the camera event was handled.
     */
    override fun onCameraHandled() {
        _attachButtonEnabled.value = true
        _selectAttachmentsVisible.value = false
        _startCamera.value = false
    }

    /**
     * Called after the gallery event was handled.
     */
    override fun onGalleryHandled() {
        _attachButtonEnabled.value = true
        _selectAttachmentsVisible.value = false
        _openGallery.value = false
    }

    override fun startSubscription(contactId: ByteArray) {
        getContactInfo(contactId)
        getMessages(contactId)
        markContactAsInteracted(contactId)
    }

    private fun markContactAsInteracted(contactId: ByteArray) {
        daoRepo.deleteNewConnection(userId = contactId.toBase64String())
    }

    override fun getMessages(chatId: ByteArray) {
        Timber.v(
            "[INDIVIDUAL CHATS] Subscribing to conversation id: %s",
            chatId.toBase64String()
        )

        val msgs = daoRepo.getMessagesLiveData(chatId)
        _chatData = msgs
//            .mapByPage {
//                it.apply { verifyUnsentMessages(this) }
//            }
            .toLiveData(
                Config(
                    pageSize = 50,
                    prefetchDistance = 100,
                    enablePlaceholders = true
                )
            ) as LiveData<PagedList<PrivateMessage>>
    }

    private fun getContactInfo(contactId: ByteArray) {
        subscriptions.add(daoRepo.getContactByUserId(contactId)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { err ->
                    Timber.e(err)
                },
                onSuccess = { contact ->
                    this.contact = contact
                    contactData.value = contact
                }
            ))
    }

    /**
     * Caches the selected files to be sent.
     */
    fun onAttachmentsSelected(uriList: List<Uri>) {
        _attachments.value?.let {
            _attachments.value = it.plus(uriList)
        } ?: run { _attachments.value = uriList }
    }

    /**
     * Instantly sends the selected attachment.
     */
    fun onAttachmentSelected(uri: Uri) {
        sendAttachment(uri)
    }

    /**
     * Send a single attachment.
     */
    private fun sendAttachment(uri: Uri) {
        var sentFile: SentFile? = null
        try {
            sentFile = fileRepository.send(
                OutgoingFile(
                    uri,
                    Recipient(contactId),
                    sentProgressListener
                )
            )
            Timber.d("[File Transfer] Starting send of transfer ID ${sentFile.transferId.tid.toBase64String()}")

            val fileMessage = createMessage(sentFile)
            saveFileMessageToDb(fileMessage)
            onMessageSent(false)
            // Associate this SentFile with the Message derived from it.
            sentFilesMessagesMap[sentFile] = fileMessage
        } catch (e: Exception) {
            _errorMessage.value = e
            _errorMessage.value = null
            sentFile?.let {
                sentFilesMessagesMap[it]
                    ?.updateStatus(MessageStatus.FAILED)
            }
        }
    }

    private fun createMessage(sentFile: SentFile): PrivateMessage {
        val messagePayload = CMIXText.newBuilder().apply {
            text = sentFile.outgoingText()
        }.build()

        return PrivateMessageData(
            uniqueId = sentFile.transferId.tid,
            status = MessageStatus.PENDING.value,
            timestamp = Utils.getCurrentTimeStamp(),
            unread = false,
            sender = repo.getUserId(),
            receiver = contact.userId,
            payload = messagePayload.toByteArray().toBase64String(),
            fileType = sentFile.fileType.toString(),
            fileUri = sentFile.uri.toString()
        )
    }

    private fun SentFile.outgoingText(): String = when {
        isImage() -> appContext().getString(R.string.chat_image_sent_label)
        isVideo() -> appContext().getString(R.string.chat_video_sent_label)
        isAudio() -> appContext().getString(R.string.chat_audio_sent_label)
        isDocument() -> appContext().getString(R.string.chat_doc_sent_label)
        else -> appContext().getString(R.string.chat_file_sent_label)
    }


    private fun saveFileMessageToDb(fileMessage: PrivateMessage) {
        subscriptions.add(
            daoRepo.insertMessage(fileMessage as PrivateMessageData)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.single)
                .subscribeBy(
                    onError = { t ->
                        Timber.e("[INDIVIDUAL CHATS] Error saving msg ${t.localizedMessage}")
                    },
                    onSuccess = { msgId ->
                        Timber.v("[INDIVIDUAL CHATS] Message was saved into existing chat with id $msgId")
                        fileMessage.id = msgId
                    })
        )
    }

    private fun Map<SentFile, PrivateMessage>.getAssociatedSentFile(
        message: PrivateMessage
    ): SentFile? = try {
        filterValues { it === message }
            .keys
            .first()
    } catch (e: NoSuchElementException) {
        Timber.d("[File Transfer] Couldn't retrieve SentFile for this SentFileProgressCallback.")
        null
    }

    private fun Map<SentFile, PrivateMessage>.getAssociatedMessage(
        callback: SentFileProgressCallback
    ): PrivateMessage? = try {
        val sentFile = filterKeys { it.progressCallback === callback }
            .keys
            .first()

        this[sentFile]
    } catch (e: NoSuchElementException) {
        Timber.d("[File Transfer] Couldn't retrieve SentFile for this SentFileProgressCallback.")
        null
    }

    private fun MutableMap<SentFile, PrivateMessage>.removeAssociatedSentFile(
        message: PrivateMessage
    ) = remove(getAssociatedSentFile(message))

    private fun PrivateMessage.updateStatus(status: MessageStatus) {
        this.status = status.value
        updateMessage(this)
    }

    /**
     * Send a list of attachments.
     */
    private fun sendAttachments() {
        _attachments.value?.let { attachmentsList ->
            if (attachmentsList.isNotEmpty()) {
                for (uri in attachmentsList) {
                    sendAttachment(uri)
                }
            }
        }
    }

    private fun sendTextMessage() {
        if (messageText.value.isValidMessage()) {
            val replyWrapper = reply.value?.let {
                wrapReply(it)
            }
            val payload = createPayload(replyWrapper, messageText.value!!)
            val message = PrivateMessageData(
                status = MessageStatus.PENDING.value,
                payload = payload,
                timestamp = Utils.getCurrentTimeStamp(),
                unread = false,
                sender = getUserId(),
                receiver = contact.userId
            )

            sendMessage(message)
        }
    }

    private fun resendAttachment(failedMessage: PrivateMessage) {
        val transferId = TransferId(failedMessage.uniqueId)
        val reSentFile = SentFile(
            transferId,
            "",
            "",
            FileData(ByteArray(0)),
            null,
            Recipient(failedMessage.receiver),
            sentProgressListener
        )

        sentFilesMessagesMap[reSentFile] = failedMessage

        with (transferId) {
            fileRepository.resend(this)
            fileRepository.registerSendProgressCallback(
                this,
                reSentFile.progressCallback
            )
        }
    }

    override fun onCleared() {
        subscriptions.dispose()
        super.onCleared()
    }

    fun onCancelRecording() {
        stopTimer()
        restoreMessagingUi()
    }

    private fun onStopRecording() {
        _stopRecording.value = true
        stopTimer()
    }

    override fun onStopRecordingClicked() {
        val validDuration = (_recordingDuration.value ?: 0) > MINIMUM_RECORDING_DURATION_MS
        onStopRecording()

        if (validDuration) showAudioPreview()
        else restoreMessagingUi()
    }

    private fun showAudioPreview() {
        _previewRecording.value = true
        _stopRecordingVisible.value = false
        _audioPreviewVisible.value = true
        _audioPreviewPauseVisible.value = false
        _cancelAttachmentVisible.value = true
    }

    override fun onSendButtonClicked() {
        if (messageText.value.isNullOrBlank()) onSendAudio()
        else onSendMessage()
    }

    private fun onSendAudio() {
        _sendAudioMessage.value = true
    }

    fun onAudioSent() {
        _sendAudioMessage.value = false
        restoreMessagingUi()
    }

    private fun restoreMessagingUi() {
        _previewRecording.value = false
        _cancelAttachmentVisible.value = false
        _messageInputVisible.value = true
        _audioPreviewVisible.value = false
        _audioPreviewPauseVisible.value = false
        _attachButtonEnabled.value = true
        _stopRecordingVisible.value = false
    }

    override fun resetMessageInput() {
        super.resetMessageInput()
        _attachments.value = listOf()
    }

    override fun sendMessage(msg: PrivateMessage) {
        subscriptions.add(
            daoRepo.insertMessage(msg as PrivateMessageData)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.single)
                .subscribeBy(
                    onError = { t ->
                        Timber.e("[INDIVIDUAL CHATS] Error saving msg ${t.localizedMessage}")
                    },
                    onSuccess = { msgId ->
                        Timber.v("[INDIVIDUAL CHATS] Message was saved into existing chat with id $msgId")
                        msg.id = msgId
                        sendMessageOnClient(msg)
                    })
        )
    }

    private fun sendMessageOnClient(msg: PrivateMessage) {
        subscriptions.add(
            repo.sendViaClientE2E(preferences.getUserId(), contact.userId, msg.payload)
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.io)
                .doOnSuccess { sentReport ->
                    msg.sendReport = sentReport.marshal()
                    msg.roundUrl = sentReport.getRoundUrl()
                }
                .zipWith(
                    daoRepo.updateMessage(msg as PrivateMessageData).toMaybe(),
                    { report: SendReportBase, _: Int ->
                        Timber.v(
                            "[INDIVIDUAL CHATS] Zipped with update: ${
                                report.marshal().decodeToString()
                            }"
                        )
                        report
                    }).subscribeBy(
                    onError = { t ->
                        Timber.e(t)
                        Timber.v("[INDIVIDUAL CHATS] Error sending msg")
                        msg.status = MessageStatus.FAILED.value
                        updateMessage(msg)
                    },
                    onSuccess = { report ->
                        Timber.v("[INDIVIDUAL CHATS] Successfully sent msg")
                        Timber.v("[INDIVIDUAL CHATS] Server Timestamp: ${report.getTimestampMs()}")

                        val marshalledReport = report.marshal()
                        Timber.v(
                            "[INDIVIDUAL CHATS] Server Timestamp (unmarshalled): ${
                                repo.unmarshallSendReport(
                                    marshalledReport
                                ).getTimestampMs()
                            }"
                        )
                        if (isMockVersion()) {
                            createMockedMsg()
                        }
                        waitForMessageDelivery(msg)
                    },
                    onComplete = {
                        Timber.e("[INDIVIDUAL CHATS] SendReport was null, error sending msg")
                        msg.status = MessageStatus.FAILED.value
                        updateMessage(msg)
                    }
                )
        )
    }

    override fun updateMessage(
        msg: PrivateMessage,
        isResending: Boolean
    ) {
        subscriptions.add(daoRepo.updateMessage(msg as PrivateMessageData)
            .subscribeOn(schedulers.io)
            .subscribeBy(
                onError = { t ->
                    Timber.e("[INDIVIDUAL CHATS] Error updating msg with id ${msg.id} \n${t.localizedMessage}")
                },
                onSuccess = {
                    Timber.v("[INDIVIDUAL CHATS] Successfully updated msg with id: $it")

                    if (isResending) {
                        when {
                            msg.fileType.isNullOrEmpty() -> {
                                Timber.v("[INDIVIDUAL CHATS] Resending msg: $msg")
                                sendMessageOnClient(msg)
                            }
                            else -> resendAttachment(msg)
                        }
                    }
                }
            )
        )
    }

    override fun createMockedMsg() {
        if (isMockVersion()) {
            val dummyMsg = DummyGenerator.getMessageDummy()
            Timber.v("Inserting dummy: $dummyMsg")

            val random = (0..100).random()
            if (random < 60) {
                Timber.v("Dummy inserted: $dummyMsg")
                subscriptions.add(
                    daoRepo.insertMessage(
                        PrivateMessageData(
                            sender = contact.userId,
                            receiver = preferences.getUserId(),
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
    }

    fun onStartRecordingHandled() {
        _startRecording.value = false
    }

    fun onStopRecordingHandled() {
        _stopRecording.value = false
    }

    fun onRecordingStarted() {
        showRecordingUi()
        startTimer()
    }

    /**
     * Called when the selected attachment should be removed.
     */
    override fun onRemoveAttachmentClicked(uri: Uri) {
        _attachments.value?.let {
            _attachments.value = it.minus(uri)
        }
    }

    /**
     * Attempt to send a message.
     */
    override fun onSendMessage() {
        sendAttachments()
        sendTextMessage()
        onMessageSent()
    }

    override val PrivateMessage.failedDelivery: Boolean
        get() {
            if (!isTextMessage()) return false

            return when (status) {
                MessageStatus.TIMEOUT.value -> true
                MessageStatus.PENDING.value -> {
                    (System.currentTimeMillis() - timestamp) > DELIVERY_TIMEOUT_MS
                }
                else -> false
            }
        }

    override fun deleteMessagesById(messageIds: List<Long>) {
        subscriptions.add(daoRepo.deleteAllMessages(messageIds)
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = {
                    it.printStackTrace()
                },
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
            daoRepo.deleteAllMessages(contact.userId)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onError = {
                        it.printStackTrace()
                    },
                    onSuccess = {
                        Timber.v("[INDIVIDUAL CHATS] Messages were deleted successfully")
                        onSelectionCleared()
                    }
                )
        )
    }

    fun deleteContact() {
        contactData.value?.marshaled?.let {
            onDeleteContact(it)
        }
    }

    private fun onDeleteContact(marshalledContact: ByteArray) {
        subscriptions.add(repo.deleteContact(marshalledContact)
            .flatMap {
                daoRepo.deleteAllMessages(contactData.value?.userId!!)
            }
            .flatMap {
                daoRepo.deleteContact(contactData.value!!)
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .subscribeBy(
                onError = { _errorMessage.value = Exception(it.localizedMessage) },
                onSuccess = { _contactDeleted.value = true }
            ))
    }

    val contactDeleted: LiveData<Boolean> get() = _contactDeleted
    private val _contactDeleted = MutableLiveData(false)

    val fullScreenImageUri: LiveData<String?> get() = _fullScreenImageUri
    private val _fullScreenImageUri = MutableLiveData<String?>(null)

    fun onImageClicked(imageUri: String) {
        _fullScreenImageUri.value = imageUri
    }

    fun onFullScreenImageHandled() {
        _fullScreenImageUri.value = null
    }

    val navigateToProfile: LiveData<Boolean> get() = _navigateToProfile
    private val _navigateToProfile = MutableLiveData(false)

    override fun onContactClicked() {
        _navigateToProfile.value = true
    }

    fun onNavigateToProfileHandled() {
        _navigateToProfile.value = false
    }

    companion object {
        fun provideFactory(
            assistedFactory: PrivateMessagesViewModelFactory,
            contactId: ByteArray,
            cachedContact: ContactData? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(contactId, cachedContact) as T
            }
        }
    }
}

@AssistedFactory
interface PrivateMessagesViewModelFactory {
    fun create(contactId: ByteArray, cachedContact: ContactData? = null): PrivateMessagesViewModel
}
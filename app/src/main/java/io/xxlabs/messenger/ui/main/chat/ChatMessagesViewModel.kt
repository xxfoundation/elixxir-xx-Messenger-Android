package io.xxlabs.messenger.ui.main.chat

import android.app.Application
import android.graphics.Bitmap
import android.view.HapticFeedbackConstants
import androidx.lifecycle.*
import androidx.paging.PagedList
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.bindings.wrapper.report.SendReportBase
import io.xxlabs.messenger.data.data.PayloadWrapper
import io.xxlabs.messenger.data.data.ReplyWrapper
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.ioThread
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.ui.dialog.info.InfoDialogUI
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController.Companion.ALL_MESSAGES
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController.Companion.MAX_REPLY_PREVIEW_LENGTH
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

abstract class ChatMessagesViewModel<T: ChatMessage> (
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    val schedulers: SchedulerProvider,
    val preferences: PreferencesRepository,
    application: Application,
    private val chatId: ByteArray
) : AndroidViewModel(application), ChatMessagesUIController<T> {

    protected var subscriptions = CompositeDisposable()

    init {
        startSubscription(chatId)
    }

    /**
     * The messages for this chat.
     */
    override val chatData: LiveData<PagedList<T>> get() = _chatData
    protected lateinit var _chatData: LiveData<PagedList<T>>

    /**
     * Returns true if the content is loading.
     */
    override val isLoading: LiveData<Boolean> get() = _isLoading
    private val _isLoading = MutableLiveData<Boolean>()

    /**
     * Returns true if there are no messages in the current chat.
     */
    override val noContent = Transformations.map(_chatData) { it.isEmpty() }

    /**
     * Messages currently selected by the user.
     */
    protected val selectedMessages = MutableLiveData<List<T>>()
    override val messageToCopy: T? get() = selectedMessages.value?.first()

    private val _selectionMenuVisible = Transformations.map(selectedMessages) {
        it.isNotEmpty()
    }
    override val selectionMenuVisible = Transformations.distinctUntilChanged(_selectionMenuVisible)
    override val copyMenuOptionEnabled = Transformations.map(selectedMessages) {
        it.size == 1
    }
    override val deleteMenuOptionEnabled = Transformations.map(selectedMessages) {
        it.isNotEmpty()
    }
    override val replyMenuOptionEnabled = Transformations.map(selectedMessages) {
        it.size == 1 && it.first().canBeReplied()
    }

    abstract fun T.canBeReplied(): Boolean

    /**
     * Determines if the RecyclerView swipe selection should be enabled.
     */
    override val swipeSelectionEnabled = Transformations.map(selectionMenuVisible) { menuVisible ->
        !menuVisible
    }

    /**
     * Exposes the selection state to update the UI accordingly.
     */
    override val selectionMode = selectionMenuVisible

    /**
     * The current message that has been selected to be replied.
     */
    protected val reply = MutableLiveData<T?>()

    /**
     * The content of the message being replied to.
     */
    override val replyText = Transformations.map(reply) { message ->
        message?.payloadWrapper?.text?.apply {
            if (length > MAX_REPLY_PREVIEW_LENGTH) {
                substring(0, MAX_REPLY_PREVIEW_LENGTH)
            }
        }
    }

    /**
     * Image in the message being replied to.
     */
    override val replyImage = MutableLiveData<String?>(null)

    /**
     * Exposes the unique ID of the reply so the UI can scroll to it.
     */
    override val scrollToReply: LiveData<ByteArray> get() = _scrollToReply
    private val _scrollToReply = MutableLiveData<ByteArray>()

    /**
     * Exposes error messages to be presented by UI.
     */
    override val errorMessage: LiveData<Exception?> get() = _errorMessage
    protected val _errorMessage = MutableLiveData<Exception?>()

    /**
     * The title for the current chat, typically the contact name.
     */
    abstract override val chatTitle: LiveData<String>

    /**
     * The contact's profile photo, or a default one if none was chosen.
     */
    abstract override val chatIcon: LiveData<Bitmap?>


    /**
     * Exposes the "enter to send" setting in the user's preferences.
     */
    override val enterToSend = preferences.isEnterToSendEnabled

    /**
     * Exposes the "incognito keyboard" setting in the user's preferences.
     */
    override val incognitoKeyboard = preferences.isIncognitoKeyboardEnabled

    /**
     * Passes [HapticFeedbackConstants] to be performed by the view.
     */
    override val vibrateEvent: LiveData<Int?> get() = _vibrateEvent
    private val _vibrateEvent = MutableLiveData<Int?>()
    private val _beepEvent = MutableLiveData<Boolean>()
    override val beepEvent: LiveData<Boolean> get() = _beepEvent

    /**
     * Returns the id of a message that was selected/unselected,
     * or [ALL_MESSAGES] if all messages were selected, to
     * efficiently update the UI.
     */
    override val messageSelected: LiveData<Long?> get() = _messageSelected
    private val _messageSelected = MutableLiveData<Long>()

    /**
     * Returns the id of a message that was deleted,
     * or [ALL_MESSAGES] if all messages were selected, to
     * efficiently update the UI.
     */
    override val messageDeleted: LiveData<Long?> get() = _messageDeleted
    protected val _messageDeleted = MutableLiveData<Long>()

    /**
     * Determines if the file browser should be shown for selecting files to send.
     */
    val openFileBrowser: LiveData<Boolean> get() = _openFileBrowser
    protected abstract val _openFileBrowser: MutableLiveData<Boolean>

    /**
     * Determines if the camera should be launched for attaching photo/video.
     */
    val startCamera: LiveData<Boolean> get() = _startCamera
    protected abstract val _startCamera: MutableLiveData<Boolean>

    /**
     * Determines if the gallery should be shown for selecting media to send.
     */
    val openGallery: LiveData<Boolean> get() = _openGallery
    protected abstract val _openGallery: MutableLiveData<Boolean>

    /**
     * Controls visibility of attachment options (camera, gallery, etc.)
     */
    override val selectAttachmentsVisible: LiveData<Boolean> get() = _selectAttachmentsVisible
    protected abstract val _selectAttachmentsVisible: MutableLiveData<Boolean>

    /**
     * Called when the microphone button is clicked.
     */
    val startRecording: LiveData<Boolean> get() = _startRecording
    protected abstract val _startRecording: MutableLiveData<Boolean>

    /**
     * The duration of the current audio recording.
     */
    override val recordingDuration: LiveData<Int> get() = _recordingDuration
    protected abstract val _recordingDuration: MutableLiveData<Int>
    protected val verifyingMsgs = HashMap<ByteArray, Boolean>()

    override fun onStartRecording() {
        _startRecording.value = true
    }

    abstract override fun onAttachFileButtonClicked()

    abstract override fun onCancelAttachmentClicked()

    abstract override fun onCameraButtonClicked()

    abstract override fun onGalleryButtonClicked()

    abstract override fun onFilesButtonClicked()

    /**
     * Called after the file browser event was handled.
     */
    abstract fun onFileBrowserHandled()

    /**
     * Called after the camera event was handled.
     */
    abstract fun onCameraHandled()

    /**
     * Called after the gallery event was handled.
     */
    abstract fun onGalleryHandled()

    override fun onBeepComplete() {
        _beepEvent.value = false
    }

    override fun onVibrateComplete() {
        _vibrateEvent.value = null
    }

    abstract fun startSubscription(chatId: ByteArray)

    protected abstract fun getMessages(chatId: ByteArray)

    /**
     * Triggered when a chat message has been clicked.
     */
    override fun onMessageClicked(message: T) {
        if (selectionMode.value == true) {
            selectedMessages.value?.let {
                selectedMessages.value =
                    if (it.contains(message)) it.minus(message)
                    else it.plus(message)
            } ?: run {
                selectedMessages.value = mutableListOf(message)
            }

            _messageSelected.value = message.id
            return
        }

        when (message.status) {
            MessageStatus.FAILED.value, MessageStatus.TIMEOUT.value -> retryMessage(message)
        }
    }

    private fun retryMessage(message: T) {
        _vibrateEvent.value = HapticFeedbackConstants.CONTEXT_CLICK
        message.status = MessageStatus.PENDING.value
        message.timestamp = Utils.getCurrentTimeStamp()
        updateMessage(message, true)
    }

    /**
     * Triggered when a chat message has been long-clicked.
     */
    override fun onMessageLongClicked(message: T) {
        selectedMessages.value?.let {
            if (it.isEmpty()) _vibrateEvent.value = HapticFeedbackConstants.LONG_PRESS

            selectedMessages.value =
                if (it.contains(message)) it.minus(message)
                else it.plus(message)
        } ?: run {
            _vibrateEvent.value = HapticFeedbackConstants.LONG_PRESS
            selectedMessages.value = mutableListOf(message)
        }
    }

    /**
     * Updates cache of current selected messages.
     */
    override fun selectAllMessages() {
        selectedMessages.value = _chatData.value?.toMutableList()
        _messageSelected.value = ALL_MESSAGES
    }

    override fun isSelected(message: T) =
        selectedMessages.value?.contains(message) ?: false

    /**
     * Clear the selection of messages by the user.
     */
    override fun onSelectionCleared() {
        selectedMessages.value = mutableListOf()
    }

    /**
     * Notifies the reply area preview has been tapped.
     */
    override fun onReplyPreviewClicked(uniqueId: ByteArray) {
        _scrollToReply.value = uniqueId
    }

    /**
     * Notifies the reply area preview has been tapped.
     */
    override fun onReplyPreviewClicked() {
        // DataBinding doesn't like nullable arguments that have a default value
        // so this function is necessary.
        val replyId = reply.value?.uniqueId
        replyId?.let { _scrollToReply.value = it }
    }

    /**
     * Begin a reply to [message]. Called by MessageViewHolder.
     */
    override fun onCreateReply(message: T) {
        if (message.canBeReplied()) reply.value = message
    }

    /**
     * Begin a reply to the current selected message. Called by reply button in
     * selected message context menu.
     */
    override fun onReplyToSelectedMessage() {
        reply.value = selectedMessages.value?.first()
        // Clear the selection after a message has been chosen for reply.
        onSelectionCleared()
    }

    /**
     * Remove the selected message and cancel the reply prompt.
     */
    override fun onRemoveReply() {
        reply.value = null
    }

    protected fun wrapReply(replyToMsg: T): ReplyWrapper {
        val replyJson = ReplyWrapper.buildJsonInstance(
            replyToMsg.sender,
            replyToMsg.uniqueId,
        )

        return ReplyWrapper.getInstance(replyJson)
    }

    protected fun createPayload(
        replyTo: ReplyWrapper? = null,
        msg: String
    ): String =
        if (isMockVersion()) PayloadWrapper(msg, replyTo).toString()
        else ChatMessage.buildCmixMsg(msg, replyTo)

    protected abstract fun sendMessage(msg: T)

    private fun checkMessageWasDelivered(
        delivered: Boolean,
        timedOut: Boolean,
        msg: T
    ) {
        val sendReport = repo.unmarshallSendReport(msg.sendReport!!)
        Timber.v(
            "[MSG DELIVERY CALLBACK] Unmarshalled SendReport: ${
                sendReport.marshal().decodeToString()
            }"
        )
        Timber.v("[MSG DELIVERY CALLBACK] Unmarshalled SendReport (Timestamp): ${sendReport.getTimestampMs()}")
        verifyingMsgs[msg.sendReport!!] = false
        when {
            delivered -> onDeliverySuccess(msg, sendReport)
            timedOut -> onDeliveryTimeout(msg)
            else -> onDeliveryFailed(msg)
        }
    }

    private fun onDeliveryTimeout(msg: T) {
        Timber.v("[MSG DELIVERY CALLBACK] Timed Out, updating message status")
        msg.status = MessageStatus.TIMEOUT.value
        updateMessage(msg)
    }

    private fun onDeliverySuccess(msg: T, sendReport: SendReportBase) {
        Timber.v("[MSG DELIVERY CALLBACK] Successfully delivered")
        msg.status = MessageStatus.SENT.value
        msg.uniqueId = sendReport.getMessageId()
        msg.timestamp = sendReport.getTimestampMs()

        updateMessage(msg)
    }

    private fun onDeliveryFailed(msg: T) {
        Timber.v("[DELIVERY CALLBACK] Not delivered")
        msg.status = MessageStatus.FAILED.value
        updateMessage(msg)
    }

    protected fun waitForMessageDelivery(msg: T) {
        val sendReport = msg.sendReport
        Timber.v("[INDIVIDUAL CHATS] Waiting for message delivery for msg id ${msg.id}")
        Timber.v("[INDIVIDUAL CHATS] SendReport ${msg.sendReport?.decodeToString()}")

        if (sendReport != null && !sendReport.contentEquals(byteArrayOf())) {
            try {
                ioThread {
                    if (verifyingMsgs.containsKey(sendReport) && verifyingMsgs[sendReport] == true) {
                        Timber.v("[INDIVIDUAL CHATS] Already verifying")
                    } else {
                        Timber.v("[MSG DELIVERY CALLBACK] Waiting for message delivery ${msg.uniqueId.toBase64String()}")
                        Timber.v(
                            "[MSG DELIVERY CALLBACK] Marshalled (decodeToString): ${
                                sendReport.decodeToString()
                            }"
                        )
                        verifyingMsgs[sendReport] = true
                        repo.waitForMessageDelivery(
                            sendReport,
                            onMessageDeliveryCallback = { msgId, delivered, timedOut, roundResults ->
                                Timber.v("[MSG DELIVERY CALLBACK] MsgId: ${msgId.toBase64String()} | delivered: $delivered | timedOut: $timedOut | results: $roundResults")
                                checkMessageWasDelivered(delivered, timedOut, msg)
                            },
                            timeoutMillis = DELIVERY_TIMEOUT_MS
                        )
                    }
                }
            } catch (err: Exception) {
                Timber.v("[DELIVERY CALLBACK] error: ${err.localizedMessage}")
                verifyingMsgs[sendReport] = false
                waitForMessageDelivery(msg)
            }
        } else {
            Timber.v("[MSG DELIVERY CALLBACK] Sent report is null ${msg.uniqueId.toBase64String()}")
            msg.status = MessageStatus.FAILED.value
            verifyingMsgs.remove(sendReport)
            updateMessage(msg)
        }
    }

    protected abstract fun updateMessage(
        msg: T,
        isResending: Boolean = false
    )

    protected abstract fun createMockedMsg()

    override fun onDeleteSelectedMessages() {
        val messageIds = selectedMessages.value?.map { it.id }
        messageIds?.let { deleteMessagesById(it) }
    }

    abstract fun deleteMessagesById(messageIds: List<Long>)

    override fun deleteAll() {
        onSelectionCleared()
        onDeleteAll()
    }

    abstract fun onDeleteAll()

    override fun readAll() {
        subscriptions.add(
            daoRepo.markChatRead(chatId)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .subscribeBy(
                    onError = { t: Throwable -> Timber.e(t) },
                    onSuccess = { res: Int ->
                        //Timber.v("Updated chat messages are now read")
                    }
                )
        )
    }

    override fun verifyUnsentMessages(list: List<T>) {
        viewModelScope.launch {
            val unsent = list.filter { it.failedDelivery }
            unsent.forEach { waitForMessageDelivery(it) }
        }
    }

    /**
     * Returns true if the ChatMessage [T] was not successfully delivered.
     */
    abstract val T.failedDelivery: Boolean

    /**
     * Clear the current displayed error.
     */
    override fun onClearError() {
        _errorMessage.value = null
    }

    protected fun String?.isValidMessage() = this?.isNotEmpty() ?: false

    override fun getUserId() = preferences.getUserId()

    protected fun onMessageSent(resetMessageInput: Boolean = true) {
        _beepEvent.value = true
        _vibrateEvent.value = HapticFeedbackConstants.LONG_PRESS
        if (resetMessageInput) resetMessageInput()
    }

    protected open fun resetMessageInput() {
        reply.value = null
        messageText.value = ""
    }

    /**
     * Lookup a message with the provided [messageID]. Used for presenting replies.
     */
    fun lookupMessage(messageId: ByteArray): T? {
        return try {
            _chatData.value?.first { it.uniqueId.contentEquals(messageId) }
        } catch (e: Exception) {
            null
        }
    }

    override val showMixClicked: LiveData<String?> get() = _showMixClicked
    private val _showMixClicked = MutableLiveData<String?>()
    private val showMixPendingDialogUI: InfoDialogUI by lazy {
        InfoDialogUI.create(
            title = "Show mix",
            body = appContext().getString(R.string.chats_show_mix_pending_message),
        )
    }

    override fun onShowMixClicked(message: T) {
        message.roundUrl?.let { _showMixClicked.value = it }
            ?: run { _showMixPendingMessage.value = showMixPendingDialogUI }
    }

    override val showMixPendingMessage: LiveData<InfoDialogUI?> get() = _showMixPendingMessage
    private val _showMixPendingMessage = MutableLiveData<InfoDialogUI?>(null)

    override fun onShowMixHandled() {
        _showMixClicked.value = null
    }

    override fun onShowMixPendingMessageShown() {
        _showMixPendingMessage.value = null
    }

    companion object {
        const val DELIVERY_TIMEOUT_MS = 30_000L
    }
}
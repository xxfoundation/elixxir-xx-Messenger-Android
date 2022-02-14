package io.xxlabs.messenger.ui.main.chat

import android.graphics.Bitmap
import android.net.Uri
import android.text.Spanned
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import io.xxlabs.messenger.data.room.model.ChatMessage

interface ChatMessagesUIController<T: ChatMessage>  {
    /**
     * Returns true if the content is loading.
     */
    val isLoading: LiveData<Boolean>

    /**
     * The messages for this chat.
     */
    val chatData: LiveData<PagedList<T>>

    /**
     * Returns true if there are no messages in the current chat.
     */
    val noContent: LiveData<Boolean>
    val messageToCopy: T?

    /**
     * Text entered by the user to send as a message.
     */
    val messageText: MutableLiveData<String?>
    val selectionMenuVisible: LiveData<Boolean>
    val copyMenuOptionEnabled: LiveData<Boolean>
    val deleteMenuOptionEnabled: LiveData<Boolean>
    val replyMenuOptionEnabled: LiveData<Boolean>

    /**
     * Determines if the RecyclerView swipe selection should be enabled.
     */
    val swipeSelectionEnabled: LiveData<Boolean>

    /**
     * Exposes the selection state to update the UI accordingly.
     */
    val selectionMode: LiveData<Boolean>

    /**
     * The content of the message being replied to.
     */
    val replyText: LiveData<String?>

    /**
     * The author of the message being replied to.
     */
    val replyUsername: LiveData<String>

    /**
     * Image in the message being replied to.
     */
    val replyImage: LiveData<String?>

    /**
     * Exposes the unique ID of the reply so the UI can scroll to it.
     */
    val scrollToReply: LiveData<ByteArray>

    /**
     * Exposes error messages to be presented by UI.
     */
    val errorMessage: LiveData<Exception?>

    /**
     * The title for the current chat, typically the contact name.
     */
    val chatTitle: LiveData<String>

    val chatTitleGravity: Int

    /**
     * The contact's profile photo, or a default one if none was chosen.
     */
    val chatIcon: LiveData<Bitmap?>

    /**
     * Whether the contact has accepted the add request or not.
     */
    val isContactPending: LiveData<Boolean>

    /**
     * Controls the visibility of the button that allows selection of attachments.
     */
    val attachButtonVisible: LiveData<Boolean>

    /**
     * Controls the visibility of the button that cancels the selection of attachments.
     */
    val cancelAttachmentVisible: LiveData<Boolean>

    /**
     * Controls the visibility of UI to play recorded audio before sending.
     */
    val audioPreviewVisible: LiveData<Boolean>

    /**
     * Controls the visibility for the UI responsible for pausing audio preview playback.
     */
    val audioPreviewPauseVisible: LiveData<Boolean>

    /**
     * Controls the visibility for the input area where the user writes a new message.
     */
    val messageInputVisible: LiveData<Boolean>

    /**
     * Controls whether the input area should be enabled.
     */
    val messageInputEnabled: LiveData<Boolean>

    /**
     * Controls the visibility of the send button.
     */
    val sendButtonVisible: LiveData<Boolean>

    /**
     * Controls whether the send buttons should be enabled.
     */
    val sendButtonEnabled: LiveData<Boolean>

    /**
     * The placeholder text to be displayed if the contact has not yet accepted.
     * Returns blank if the contact has accepted.
     */
    val contactPendingMessage: LiveData<Spanned?>

    /**
     * The placeholder image to be displayed depending on if the contact has accepted,
     * and if so, if no messages have yet been sent.
     */
    val emptyChatPlaceholder: LiveData<Int>

    /**
     * The placeholder text to be displayed if no messages have yet been sent.
     * Returns blank messages have been sent.
     */
    val chatEmptyMessage: LiveData<Spanned?>

    /**
     * Exposes the "enter to send" setting in the user's preferences.
     */
    val enterToSend: Boolean

    /**
     * Exposes the "incognito keyboard" setting in the user's preferences.
     */
    val incognitoKeyboard: Boolean

    /**
     * Passes [HapticFeedbackConstants] to be performed by the view.
     */
    val vibrateEvent: LiveData<Int?>
    val beepEvent: LiveData<Boolean>

    /**
     * Returns the id of a message that was selected/unselected,
     * or [ALL_MESSAGES] if all messages were selected, to
     * efficiently update the UI.
     */
    val messageSelected: LiveData<Long?>

    /**
     * Returns the id of a message that was deleted,
     * or [ALL_MESSAGES] if all messages were selected, to
     * efficiently update the UI.
     */
    val messageDeleted: LiveData<Long?>

    /**
     * Controls visibility of attachment options (camera, gallery, etc.)
     */
    val selectAttachmentsVisible: LiveData<Boolean>

    /**
     * Controls visibility of the mic button.
     */
    val micButtonVisible: LiveData<Boolean>
    val stopRecordingVisible: LiveData<Boolean>

    /**
     * The duration of the current audio recording.
     */
    val recordingDuration: LiveData<Int>

    /**
     * The latest message received from any chat.
     */
    val lastMessage: LiveData<T?>

    fun onStartRecording()

    fun onStopRecordingClicked()

    fun onAttachFileButtonClicked()

    fun onCancelAttachmentClicked()

    fun onCameraButtonClicked()

    fun onGalleryButtonClicked()

    fun onFilesButtonClicked()

    fun onBeepComplete()

    fun onVibrateComplete()

    /**
     * Called when the selected attachment should be removed.
     */
    fun onRemoveAttachmentClicked(uri: Uri)

    /**
     * Triggered when a chat message has been clicked.
     */
    fun onMessageClicked(message: T)

    /**
     * Triggered when a chat message has been long-clicked.
     */
    fun onMessageLongClicked(message: T)

    /**
     * Updates cache of current selected messages.
     */
    fun selectAllMessages()

    fun isSelected(message: T): Boolean

    /**
     * Clear the selection of messages by the user.
     */
    fun onSelectionCleared()

    /**
     * Notifies the reply area preview has been tapped.
     */
    fun onReplyPreviewClicked(uniqueId: ByteArray)

    /**
     * Notifies the reply area preview has been tapped.
     */
    fun onReplyPreviewClicked()

    /**
     * Begin a reply to [message]. Called by MessageViewHolder.
     */
    fun onCreateReply(message: T)

    /**
     * Begin a reply to the current selected message. Called by reply button in
     * selected message context menu.
     */
    fun onReplyToSelectedMessage()

    /**
     * Remove the selected message and cancel the reply prompt.
     */
    fun onRemoveReply()

    fun onDeleteSelectedMessages()

    fun deleteAll()

    fun readAll()

    fun verifyUnsentMessages(list: List<T>)

    fun getUserId(): ByteArray

    /**
     * Clear the current displayed error.
     */
    fun onClearError()

    /**
     * Attempt to send a message.
     */
    fun onSendMessage()

    /**
     * Called when the UI send button is clicked
     */
    fun onSendButtonClicked()

    fun onShowMixClicked(message: T)
    fun onShowMixHandled()

    val showMixClicked: LiveData<String?>

    companion object {
        /** The max text length to when displaying a message being replied to.*/
        const val MAX_REPLY_PREVIEW_LENGTH = 100
        const val ALL_MESSAGES = -1L
    }
}
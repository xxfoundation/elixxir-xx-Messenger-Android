package io.xxlabs.messenger.ui.main.chat

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_MUSIC
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.inputmethod.InputContentInfoCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.PrivateMessage
import io.xxlabs.messenger.media.*
import io.xxlabs.messenger.support.dialog.BottomSheetPopup
import io.xxlabs.messenger.support.dialog.MenuChatDialog
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.touch.MessageSwipeController
import io.xxlabs.messenger.support.touch.SwipeActions
import io.xxlabs.messenger.ui.dialog.warning.showConfirmDialog
import io.xxlabs.messenger.ui.main.MainActivity
import io.xxlabs.messenger.ui.main.chat.adapters.AttachmentListener
import io.xxlabs.messenger.ui.main.chat.adapters.AttachmentsAdapter
import io.xxlabs.messenger.ui.main.chat.adapters.ChatMessagesAdapter
import io.xxlabs.messenger.ui.main.chat.adapters.PrivateMessagesAdapter
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject

class PrivateMessagesFragment :
    ChatMessagesFragment<PrivateMessage>(),
    MediaCallback,
    MicrophoneCallback,
    IKSEditText.IKSEditTextListener
{

    private val contactId: ByteArray by lazy {
        PrivateMessagesFragmentArgs
            .fromBundle(requireArguments())
            .contactId
            ?.fromBase64toByteArray()
            ?: requireArguments().getByteArray("contact_id") !!
    }
    private val cachedContact: ContactData? by lazy {
        PrivateMessagesFragmentArgs
            .fromBundle(requireArguments())
            .contact
    }

    /* ViewModels */

    @Inject
    lateinit var chatViewModelFactory: PrivateMessagesViewModelFactory

    private val chatViewModel: PrivateMessagesViewModel by viewModels {
        PrivateMessagesViewModel.provideFactory(
            chatViewModelFactory,
            contactId,
            cachedContact
        )
    }
    override val uiController: ChatMessagesUIController<PrivateMessage>
        get() = chatViewModel

    /* UI */

    override val chatMessagesAdapter: ChatMessagesAdapter<PrivateMessage, *>
        get() = privateMessagesAdapter
    private lateinit var privateMessagesAdapter: PrivateMessagesAdapter
    private lateinit var chatsLayoutManager: LinearLayoutManager
    private lateinit var attachmentsAdapter: AttachmentsAdapter

    /* Sending files and media */

    private lateinit var cameraProvider: CameraProvider
    private lateinit var galleryProvider: DeviceStorageProvider
    private lateinit var microphoneProvider: MicrophoneProvider

    /* Record audio */

    private val tempAudioFile: File
        get() {
            return File.createTempFile(
                "xxm_${System.currentTimeMillis()}",
                ".m4a",
                requireContext().getExternalFilesDir(DIRECTORY_MUSIC)
            )
        }

    // Responsible for recording audio
    private var mediaRecorder: MediaRecorder? = null
    // Holds a File reference to the last audio recording created
    private var latestAudioFile: File? = null

    /* Media playback */

    private val mediaPlayer: MediaPlayerProvider by lazy {
        XxMediaPlayer(this)
    }
    private val audioPreviewControls: SimpleMediaPlayerControls by lazy {
        object : SimpleMediaPlayerControls {
            override val audioSeekBar: SeekBar
                get() = binding.audioPreviewSeekBar
            override val audioTimeLabel: TextView
                get() = binding.audioPreviewDuration
            override val audioPlayButton: View
                get() = binding.audioPreviewPlayButton
            override val audioPauseButton: View
                get() = binding.audioPreviewPauseButton
        }
    }
    private val audioPreviewPlayer: SimpleAudioPlayer by lazy {
        SimpleAudioPlayer(mediaPlayer, audioPreviewControls)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        cameraProvider = context as? CameraProvider
            ?: throw ClassCastException("Activity must implement CameraProvider!")
        galleryProvider = context as? DeviceStorageProvider
            ?: throw ClassCastException("Activity must implement DeviceStorageProvider!")
        microphoneProvider = context as? MicrophoneProvider
            ?: throw ClassCastException("Activity must implement MicrophoneProvider!")
    }

    override fun initListeners() {
        super.initListeners()
        binding.audioPreviewPlayButton.setOnClickListener {
            audioPreviewPlayer.togglePlayback(true)
        }
        binding.audioPreviewPauseButton.setOnClickListener {
            audioPreviewPlayer.togglePlayback(false)
        }
        binding.chatMsgInput.setListener(this)
    }

    override fun initMessagesRecyclerView() {
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        privateMessagesAdapter = PrivateMessagesAdapter(chatViewModel, mediaPlayer)
        chatsLayoutManager = layoutManager
        binding.chatRecyclerView.layoutManager = chatsLayoutManager
        binding.chatRecyclerView.adapter = privateMessagesAdapter
        binding.chatRecyclerView.itemAnimator = null

        privateMessagesAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                layoutManager.scrollToPositionWithOffset(0, 0)
            }
        })

        messageSwipeController = MessageSwipeController(
            requireContext(),
            object : SwipeActions {
                override fun showReply(position: Int) {
                    replyTo(position)
                }
            })

        itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.chatRecyclerView)
    }

    override fun initAttachmentRecyclerView() {
        attachmentsAdapter = AttachmentsAdapter(object : AttachmentListener {
            override fun onRemoveClicked(uri: Uri) {
                chatViewModel.onRemoveAttachmentClicked(uri)
            }

            override fun onAttachmentClicked(uri: Uri) {
                // TODO: Open attachment preview in full screen.
            }
        })
        binding.attachmentsRV.adapter = attachmentsAdapter
    }

    override fun observeUI() {
        chatViewModel.startCamera.observe(viewLifecycleOwner) { start ->
            if (start) {
                cameraProvider.startCamera(this, false)
                chatViewModel.onCameraHandled()
            }
        }

        chatViewModel.openGallery.observe(viewLifecycleOwner) { show ->
            if (show) {
                galleryProvider.selectFiles(
                    this,
                    listOf("image/*"),
                    false
                )
                chatViewModel.onGalleryHandled()
            }
        }

        chatViewModel.openFileBrowser.observe(viewLifecycleOwner) { show ->
            if (show) {
                galleryProvider.selectFiles(
                    this,
                    listOf("*/*"),
                    false
                )
                chatViewModel.onFileBrowserHandled()
            }
        }

        chatViewModel.startRecording.observe(viewLifecycleOwner) { start ->
            if (start) {
                requestMicPermission()
                chatViewModel.onStartRecordingHandled()
            }
        }

        chatViewModel.stopRecording.observe(viewLifecycleOwner) { stop ->
            if (stop) {
                stopRecording()
                chatViewModel.onStopRecordingHandled()
            }
        }

        chatViewModel.previewRecording.observe(viewLifecycleOwner) { preview ->
            if (preview) initPreviewPlayer()
        }

        chatViewModel.sendAudioMessage.observe(viewLifecycleOwner) { send ->
            if (send) {
                audioPreviewPlayer.togglePlayback(false)
                sendRecording()
                chatViewModel.onAudioSent()
            }
        }

        chatViewModel.attachments.observe(viewLifecycleOwner) { uriList ->
            attachmentsAdapter.submitList(uriList)
        }

        chatViewModel.contactDeleted.observe(viewLifecycleOwner) { deleted ->
            if (deleted) leaveChat()
        }

        chatViewModel.fullScreenImageUri.observe(viewLifecycleOwner) { imageUri ->
            imageUri?.let {
                val fullScreen = PrivateMessagesFragmentDirections
                    .actionGlobalFullScreenImageFragment(it)
                findNavController().navigate(fullScreen)
                chatViewModel.onFullScreenImageHandled()
            }
        }

        chatViewModel.navigateToProfile.observe(viewLifecycleOwner) { navigate ->
            if (navigate) {
                menuActionOpenContactProfile()
                chatViewModel.onNavigateToProfileHandled()
            }
        }

        super.observeUI()
    }

    override fun newMessageArrivedInOtherChat(lastMessage: ChatMessage): Boolean {
        return if (lastMessage.unread) {
            (lastMessage as? PrivateMessage)?.let {
                !it.sender.contentEquals(contactId)
                        && !it.receiver.contentEquals(contactId)
            } ?: true
        } else false
    }

    override fun leaveChat() {
        (requireActivity() as MainActivity).hideKeyboard()
        findNavController().navigateSafe(R.id.action_chat_pop_to_chats)
    }

    private fun displayPhotoOrVideoDialog() {
        BottomSheetPopup.getInstance(
            requireContext(),
            icon = R.drawable.ic_camera,
            description = "Camera",
            topButtonTitle = "Take photo",
            topButtonDismiss = true,
            topButtonClick = { cameraProvider.startCamera(this, false) },
            middleButtonTitle = "Record video",
            middleButtonDismiss = true,
            middleButtonClick = { cameraProvider.startCamera(this, true) },
            bottomButtonTitle = "Cancel",
            bottomButtonDismiss = true
        ).show()
    }

    override fun onFilesSelected(uriList: List<Uri>) {
        chatViewModel.onAttachmentSelected(uriList.first())
    }

    private fun requestMicPermission() {
        microphoneProvider.requestRecordAudioPermission(this)
    }

    override fun onMicrophonePermissionGranted() {
        recordAudio()
    }

    private fun recordAudio() {
        latestAudioFile = tempAudioFile

        try {
            mediaRecorder?.reset() ?: createMediaRecorder()
            initializeMediaRecorder()
            chatViewModel.onRecordingStarted()
        } catch (e: IOException) {
            requireContext().toast("Failed to start audio recording")
        }
    }

    private fun createMediaRecorder() {
        mediaRecorder = if (Build.VERSION.SDK_INT < 32) {
            MediaRecorder()
        } else {
            MediaRecorder(requireContext())
        }
    }

    private fun initializeMediaRecorder() {
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(latestAudioFile?.absolutePath)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun initPreviewPlayer() {
        latestAudioFile?.let { recording ->
            audioPreviewPlayer.audioUri = FileProvider.getUriForFile(
                requireActivity().applicationContext,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                recording
            )

            latestAudioFile = null
        }
    }

    private fun sendRecording() {
        chatViewModel.onAttachmentSelected(audioPreviewPlayer.audioUri)
    }

    override fun openBottomSheetMenu() {
        binding.chatTopBarMenu.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val bottomMenu = MenuChatDialog.getInstance(
            onClickViewContact = { menuActionOpenContactProfile() },
            onClickDeleteContact = { showDeleteContactPopup() },
            onClickClearChat = { showClearChatPopup() },
            onClickSearch = { }
        )

        bottomMenu.show(childFragmentManager, "individualChatBottomMenu")
    }

    private fun menuActionOpenContactProfile() {
        Timber.v("Sending contact id ${contactId} || ${contactId.toBase64String()}")
        val bundle = bundleOf("contact_id" to contactId)

        if (findNavController().isFragmentInBackStack(R.id.contactDetailsFragment)) {
            findNavController().navigateUp()
        } else {
            findNavController().navigateSafe(R.id.action_chat_to_profile_from_chat_flow, bundle)
        }
    }

    private fun showDeleteContactPopup() {
        showConfirmDialog(
            getString(R.string.confirm_delete_connection_dialog_title),
            getString(R.string.confirm_delete_connection_dialog_body, chatViewModel.chatTitle),
            getString(R.string.confirm_delete_connection_dialog_button),
            { chatViewModel.deleteContact() }
        )
    }

    override fun onPause() {
        super.onPause()
        releaseMediaRecorder()
    }

    private fun releaseMediaRecorder() {
        chatViewModel.onCancelRecording()
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun receivedContent(contentInfo: InputContentInfoCompat) {
        chatViewModel.onAttachmentSelected(contentInfo.contentUri)
    }
}
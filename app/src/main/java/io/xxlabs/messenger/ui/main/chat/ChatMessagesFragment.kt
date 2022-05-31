package io.xxlabs.messenger.ui.main.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.NetworkState
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.databinding.FragmentChatBinding
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialogFragment
import io.xxlabs.messenger.support.extensions.playBeepSound
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.extensions.toast
import io.xxlabs.messenger.support.touch.MessageSwipeController
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.MainActivity
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController.Companion.ALL_MESSAGES
import io.xxlabs.messenger.ui.main.chat.adapters.ChatMessagesAdapter
import io.xxlabs.messenger.ui.main.chat.viewholders.WebViewDialog
import io.xxlabs.messenger.ui.main.chat.viewholders.WebViewDialogUI
import kotlinx.android.synthetic.main.component_network_error_banner.*
import timber.log.Timber
import javax.inject.Inject

abstract class ChatMessagesFragment<T: ChatMessage>: BaseFragment() {

    /* ViewModels */

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected abstract val uiController: ChatMessagesUIController<T>
    private lateinit var networkViewModel: NetworkViewModel

    /* UI */

    protected lateinit var binding: FragmentChatBinding
    protected abstract val chatMessagesAdapter: ChatMessagesAdapter<T, *>
    lateinit var itemTouchHelper: ItemTouchHelper
    lateinit var messageSwipeController: MessageSwipeController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_chat,
            container,
            false
        )

        networkViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[NetworkViewModel::class.java]

        binding.networkViewModel = networkViewModel
        binding.chatViewModel = uiController
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponents()
    }

    private fun initComponents() {
        initMessagesRecyclerView()
        initAttachmentRecyclerView()
        initListeners()
    }

    override fun onStart() {
        super.onStart()
        observeUI()
    }

    protected abstract fun initMessagesRecyclerView()
    protected abstract fun initAttachmentRecyclerView()

    @CallSuper
    protected open fun initListeners() {
        binding.chatTopBarBackBtn.setOnSingleClickListener { leaveChat() }

        binding.chatTopBarMenu.setOnSingleClickListener { openBottomSheetMenu() }

        binding.chatBottomMenuDelete.setOnSingleClickListener { showDeleteMessagesPopup() }

        binding.chatBottomMenuCopy.setOnSingleClickListener { menuActionCopy() }

        binding.chatBottomMenuDeleteAll.setOnSingleClickListener { menuActionDeleteAllMessages() }

        binding.chatRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                scrollChatBottom(false)
            }
        }
    }

    @CallSuper
    protected open fun observeUI() {
        networkViewModel.networkState.observe(viewLifecycleOwner) { networkState ->
            Timber.v("Network State: $networkState")
            if (networkState == NetworkState.HAS_CONNECTION) {
                networkStatusLayout?.visibility = View.GONE
            } else {
                val bannerMsg = networkViewModel.getNetworkStateMessage(networkState)
                networkStatusLayout?.visibility = View.VISIBLE
                networkStatusText?.text = bannerMsg
            }
        }

        uiController.lastMessage.observe(viewLifecycleOwner) { lastMessage ->
            lastMessage?.let {
                if (newMessageArrivedInOtherChat(it)) {
                    binding.chatTopNotification.visibility = View.VISIBLE
                } else {
                    binding.chatTopNotification.visibility = View.GONE
                }
            }
        }

        uiController.selectionMenuVisible.observe(viewLifecycleOwner) { enabled ->
            if (enabled) onOpenBottomMenu()
            else onCloseBottomMenu()
        }

        uiController.swipeSelectionEnabled.observe(viewLifecycleOwner) { enabled ->
            if (!enabled) messageSwipeController.resetTranslation()
            messageSwipeController.allowSwipe = enabled
        }

        uiController.selectionMode.observe(viewLifecycleOwner) {
            chatMessagesAdapter.selectionMode = it
        }

        uiController.messageSelected.observe(viewLifecycleOwner) { messageId ->
            messageId?.let {
                if (it == ALL_MESSAGES) {
                    refreshChat()
                } else {
                    val position = chatMessagesAdapter.getItemPositionFromId(it)
                    chatMessagesAdapter.notifyItemChanged(position)
                }
            }
        }

        uiController.messageDeleted.observe(viewLifecycleOwner) { messageId ->
            messageId?.let {
                if (it == ALL_MESSAGES) {
                    refreshChat()
                } else {
                    val position = chatMessagesAdapter.getItemPositionFromId(it)
                    chatMessagesAdapter.notifyItemRemoved(position)
                }
            }
        }

        uiController.scrollToReply.observe(viewLifecycleOwner) {
            scrollToPositionWithUniqueId(it)
        }

        uiController.beepEvent.observe(viewLifecycleOwner) { beep ->
            if (beep) {
                requireContext().playBeepSound(false)
                uiController.onBeepComplete()
            }
        }

        uiController.vibrateEvent.observe(viewLifecycleOwner) {
            it?.let {
                binding.root.performHapticFeedback(it)
                uiController.onVibrateComplete()
            }
        }

        uiController.errorMessage.observe(viewLifecycleOwner) {
            it?.let { showError(it) }
        }

        uiController.showMixClicked.observe(viewLifecycleOwner) { url ->
            url?.let { showWebViewDialog(it) }
        }

        updateChat()
    }

    private fun showWebViewDialog(url: String) {
        val ui = WebViewDialogUI.create(url, null)
        WebViewDialog.newInstance(ui)
            .show(requireActivity().supportFragmentManager, null)
        uiController.onShowMixHandled()
    }

    /**
     * Called when the UI needs to be updated for all messages,
     * such as changes in selection mode.
     */
    private fun refreshChat() {
        Handler(Looper.getMainLooper()).postDelayed({
            chatMessagesAdapter.notifyDataSetChanged()
        }, 10)
    }

    protected abstract fun leaveChat()

    protected fun replyTo(position: Int) {
        try {
            chatMessagesAdapter.currentList?.get(position)?.let {
                uiController.onCreateReply(it)
            }
        } catch (e: IndexOutOfBoundsException) {
            return
        }
    }

    private fun scrollToPositionWithUniqueId(uniqueId: ByteArray) {
        val targetMessage = chatMessagesAdapter.getItemPositionFromUniqueId(uniqueId)
        if (targetMessage != RecyclerView.NO_POSITION) {
            binding.chatRecyclerView.smoothScrollToPosition(targetMessage)
        }
    }

    private fun updateChat() {
        uiController.chatData.observe(viewLifecycleOwner) { messages ->
            uiController.readAll()
            chatMessagesAdapter.submitList(messages)
        }
    }

    abstract fun newMessageArrivedInOtherChat(lastMessage: ChatMessage): Boolean

    private fun scrollChatBottom(smooth: Boolean = true) {
        if (isRemoving) return

        if (smooth) {
            binding.chatRecyclerView.smoothScrollToPosition(0)
        } else {
            binding.chatRecyclerView.scrollBy(0, Int.MAX_VALUE)
        }
    }

    //MENU
    private fun onOpenBottomMenu() {
        (requireActivity() as MainActivity).hideKeyboard()
        refreshChat()
    }

    private fun onCloseBottomMenu() = refreshChat()

    private fun menuActionReplyMessage() = uiController.onReplyToSelectedMessage()

    private fun menuActionCopy() {
        uiController.messageToCopy?.let { msg ->
            val clipboard: ClipboardManager? =
                requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            val clipData = msg.payloadWrapper.text
            val clip = ClipData.newPlainText("label", clipData)
            clipboard?.setPrimaryClip(clip)
            context?.toast("Text copied!")
            uiController.onSelectionCleared()
        }
    }

    private fun menuActionDeleteAllMessages() {
        uiController.selectAllMessages()
        showDeleteAllMessagesPopup()
    }

    abstract fun openBottomSheetMenu()

    private fun showDeleteMessagesPopup() {
        PopupActionBottomDialogFragment.getInstance(
            titleText = "Are you sure you want to delete the selected messages?",
            subtitleText = "This will clear the selected chat messages only for you. It can’t be undone.",
            positiveBtnText = "Yes, Delete",
            negativeBtnText = "No, Don't Delete",
            onClickPositive = { uiController.onDeleteSelectedMessages() },
            onClickNegative = { },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "individualChatMenuDeleteDialog")
    }

    private fun showDeleteAllMessagesPopup() {
        PopupActionBottomDialogFragment.getInstance(
            titleText = "Are you sure you want to delete?",
            subtitleText = "This will clear all chat messages only for you. It can’t be undone.",
            positiveBtnText = "Yes, Delete all",
            negativeBtnText = "No, Don't Delete",
            onClickPositive = { uiController.deleteAll() },
            onClickNegative = { },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "individualChatMenuDeleteAllDialog")
    }

    protected fun showClearChatPopup() {
        PopupActionBottomDialogFragment.getInstance(
            titleText = "Are you sure you want to clear the chat?",
            subtitleText = "You will be removing all associated messages for this chat only for you. It can't be undone.",
            positiveBtnText = "Clear Chat",
            negativeBtnText = "Cancel",
            onClickPositive = { uiController.deleteAll() },
            onClickNegative = { },
            isIncognito = preferences.isIncognitoKeyboardEnabled
        ).show(childFragmentManager, "individualChatMenuClearChat")
    }
}
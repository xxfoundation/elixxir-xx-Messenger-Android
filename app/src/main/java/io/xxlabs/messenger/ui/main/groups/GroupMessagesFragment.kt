package io.xxlabs.messenger.ui.main.groups

import android.view.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.data.room.model.GroupMessage
import io.xxlabs.messenger.support.dialog.MenuChatDialog
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.touch.MessageSwipeController
import io.xxlabs.messenger.support.touch.SwipeActions
import io.xxlabs.messenger.ui.main.MainActivity
import io.xxlabs.messenger.ui.main.chat.ChatMessagesFragment
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.messenger.ui.main.chat.adapters.ChatMessagesAdapter
import io.xxlabs.messenger.ui.dialog.confirm.showConfirmDialog
import javax.inject.Inject

class GroupMessagesFragment : ChatMessagesFragment<GroupMessage>() {

    /* ViewModels */

    @Inject
    lateinit var chatViewModelFactory: GroupMessagesViewModelFactory

    private val chatViewModel: GroupMessagesViewModel by viewModels {
        GroupMessagesViewModel.provideFactory(
            chatViewModelFactory,
            requireArguments().getByteArray("group_id")!!
        )
    }
    override val uiController: ChatMessagesUIController<GroupMessage>
        get() = chatViewModel

    /* UI */

    override val chatMessagesAdapter: ChatMessagesAdapter<GroupMessage, *>
        get() = groupMessagesAdapter
    private lateinit var groupMessagesAdapter: GroupMessagesAdapter
    private lateinit var groupMemberAvatarAdapter: GroupAvatarsAdapter
    private lateinit var groupMessagesLayoutManager: LinearLayoutManager
    private lateinit var groupMembersDialog: GroupMembersDialog

    override fun leaveChat() {
        (requireActivity() as MainActivity).hideKeyboard()
        findNavController().navigateSafe(R.id.action_groups_chat_pop_to_chats_list)
    }

    override fun initMessagesRecyclerView() {
        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true

        groupMessagesAdapter = GroupMessagesAdapter(chatViewModel)
        groupMessagesLayoutManager = layoutManager
        binding.chatRecyclerView.layoutManager = groupMessagesLayoutManager
        binding.chatRecyclerView.adapter = groupMessagesAdapter
        binding.chatRecyclerView.itemAnimator = null

        groupMessagesAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
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
        initAvatarsRecyclerView()
    }

    private fun initAvatarsRecyclerView() {
        val layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        binding.chatTopBarProfile.visibility = View.GONE
        binding.chatTopBarContactsRecycler.visibility = View.VISIBLE

        groupMemberAvatarAdapter = GroupAvatarsAdapter()
        binding.chatTopBarContactsRecycler.layoutManager = layoutManager
        binding.chatTopBarContactsRecycler.adapter = groupMemberAvatarAdapter
        binding.chatTopBarContactsRecycler.itemAnimator = null

        groupMembersDialog = GroupMembersDialog.getInstance()
    }

    override fun observeUI() {
        chatViewModel.groupMembers.observe(viewLifecycleOwner) {
            groupMessagesAdapter.updateGroupMembers(it)
        }

        chatViewModel.memberAvatars.observe(viewLifecycleOwner) {
            groupMemberAvatarAdapter.updateAvatars(it)
        }

        chatViewModel.leftGroup.observe(viewLifecycleOwner) { successful ->
            if (successful) leaveChat()
        }

        super.observeUI()
    }

    override fun newMessageArrivedInOtherChat(lastMessage: ChatMessage): Boolean {
        return if (lastMessage.unread) {
            (lastMessage as? GroupMessage)?.let {
                !it.groupId.contentEquals(chatViewModel.groupId)
            } ?: true
        } else false
    }

    override fun openBottomSheetMenu() {
        binding.chatTopBarMenu.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val bottomMenu = MenuChatDialog.getInstance(
            viewTitle = "View Members List",
            onClickViewContact = { menuActionOpenGroupMembersList() },
            deleteTitle = "Leave group",
            onClickDeleteContact = { showLeaveGroup() },
            onClickClearChat = { showClearChatPopup() },
            onClickSearch = { }
        )
        bottomMenu.show(childFragmentManager, "groupMembersBottomMenu")
    }

    private fun menuActionOpenGroupMembersList() {
        groupMembersDialog.show(childFragmentManager, "groupMembersDialog")
        groupMembersDialog.addAvatars(groupMemberAvatarAdapter.avatarList)
    }

    private fun showLeaveGroup() {
        showConfirmDialog(
            R.string.confirm_leave_group_dialog_title,
            R.string.confirm_leave_group_dialog_body,
            R.string.confirm_leave_group_dialog_button,
            { chatViewModel.leaveGroup() }
        )
    }
}
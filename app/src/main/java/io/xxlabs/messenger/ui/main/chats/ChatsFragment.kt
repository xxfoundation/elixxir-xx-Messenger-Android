package io.xxlabs.messenger.ui.main.chats

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.data.data.ChatWrapper
import io.xxlabs.messenger.data.datatype.NetworkState
import io.xxlabs.messenger.data.datatype.SelectionMode
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.support.extensions.incognito
import io.xxlabs.messenger.support.extensions.navigateSafe
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.selection.CustomSelectionTracker
import io.xxlabs.messenger.support.selection.LongKeyProvider
import io.xxlabs.messenger.support.touch.ButtonSwipeHelper
import io.xxlabs.messenger.support.touch.UnderlayButton
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.MainViewModel
import kotlinx.android.synthetic.main.component_bottom_menu_chats.*
import kotlinx.android.synthetic.main.component_network_error_banner.*
import kotlinx.android.synthetic.main.fragment_chats_list.*
import timber.log.Timber
import javax.inject.Inject

class ChatsFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var networkViewModel: NetworkViewModel
    lateinit var contactsViewModel: ContactsViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var chatsViewModel: ChatsViewModel

    @Inject
    lateinit var daoRepo: DaoRepository

    @Inject
    lateinit var schedulers: SchedulerProvider
    private lateinit var chatsAdapter: ChatsListAdapter
    private lateinit var navController: NavController
    private lateinit var tracker: CustomSelectionTracker
    var isBottomMenuOpen = false
    private lateinit var chatsSwipeController: ButtonSwipeHelper
    private val mediatorObject = Observer<Any> { Timber.v("Mediator initiated") }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_chats_list, container, false)
    }

    override fun onDetach() {
        if (this::chatsViewModel.isInitialized) {
            chatsViewModel.mediatorLiveData.removeObserver(mediatorObject)
        }
        super.onDetach()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        navController = findNavController()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(MainViewModel::class.java)

        chatsViewModel =
            ViewModelProvider(requireActivity(), viewModelFactory).get(ChatsViewModel::class.java)

        networkViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(NetworkViewModel::class.java)

        contactsViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(ContactsViewModel::class.java)
        initComponents(view)
    }

    fun initComponents(root: View) {
        root.setInsets(
            bottomMask = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime(),
            topMask = WindowInsetsCompat.Type.systemBars()
        )

        setListeners()
        bindRecyclerView()
        resetSearchBar()
        showNotificationDialog()
    }

    override fun onStart() {
        super.onStart()
        watchForObservables()
    }

    private fun showNotificationDialog() {
        if (preferences.userData.isNotBlank() && preferences.isFirstTimeNotifications) {
            showTwoButtonInfoDialog(
                R.string.settings_push_notifications_dialog_title,
                R.string.settings_push_notifications_dialog_body,
                null,
                ::enablePushNotifications,
                null,
                ::showCoverMessageDialog
            )
            preferences.isFirstTimeNotifications = false
        }
    }

    private fun enablePushNotifications() {
        mainViewModel.enableNotifications { error ->
            error?.let { showError(error) }
        }
    }

    private fun showCoverMessageDialog() {
        if (preferences.userData.isNotBlank() && preferences.isFirstTimeCoverMessages) {
            showTwoButtonInfoDialog(
                R.string.settings_cover_traffic_title,
                R.string.settings_cover_traffic_dialog_body,
                mapOf(
                    getString(R.string.settings_cover_traffic_link_text)
                            to getString(R.string.settings_cover_traffic_link_url)
                ),
                ::enableCoverMessages,
                ::declineCoverMessages,
            )
            preferences.isFirstTimeCoverMessages = false
        }
    }

    private fun enableCoverMessages() {
        enableDummyTraffic(true)
    }

    private fun declineCoverMessages() {
        enableDummyTraffic(false)
    }

    private fun enableDummyTraffic(enabled: Boolean) {
        try {
            mainViewModel.enableDummyTraffic(enabled)
        } catch (e: Exception) {
            showError(e, true)
        }
    }

    private fun setListeners() {
        chatsSearchBar.incognito(preferences.isIncognitoKeyboardEnabled)
        chatsSearchBar.addTextChangedListener { text ->
            chatsAdapter.filter.filter(text?.trim())
            if (!text.isNullOrBlank()) {
                closeBottomMenu()
            }
        }

        chatsMenu.setOnSingleClickListener {
            mainViewModel.toggleMenu()
        }

        chatsSelectConversationBtn.setOnSingleClickListener {
            val bundle = bundleOf("isSelectionMode" to true)
            navController.navigateSafe(R.id.action_chats_to_select_contact, bundle)
        }

        chatsSearchUdBtn.setOnSingleClickListener {
            navController.navigateSafe(R.id.action_chats_to_ud_search)
        }

        chatsCancelMenu.setOnSingleClickListener {
            closeBottomMenu()
        }

        chatsBottomMenu.visibility = View.GONE

        chatsBottomMenuPin.setOnSingleClickListener {
            pinChats()
        }

        chatsBottomMenuMute.setOnSingleClickListener {
            muteChats()
        }

        chatsBottomMenuDelete.setOnSingleClickListener {
            val selectedChats = chatsAdapter.getChats(getIdsListFromTracker())
                .filter { it.item is ContactData }

            val selectedGroups = chatsAdapter.getChats(getIdsListFromTracker())
                .filter { it.item is GroupData }

            when {
                selectedChats.isNotEmpty() -> deleteChats(selectedChats, selectedGroups)
                selectedGroups.isNotEmpty() -> deleteGroups(selectedGroups)
            }
        }

        chatsAddContactBtn.setOnSingleClickListener {
            navController.navigateSafe(R.id.action_chats_to_contacts)
        }

        chatsClickArea.setOnClickListener {
            mainViewModel.hideMenu()
        }

        chatsBottomMenuDeleteAll.setOnClickListener {
            val idsList = mutableListOf<Long>()
            chatsAdapter.chatsFiltered.forEach { item ->
                if (item is ChatWrapper) {
                    idsList.add(item.getItemId())
                } else if (item is ContactData) {
                    idsList.add(item.id)
                }
            }
            tracker.setItemsSelected(idsList)
            deleteAllChats()
        }
    }

    private fun watchForObservables() {
        contactsViewModel.requestsCount.observe(viewLifecycleOwner, Observer { newCount ->
            if (newCount > 0) {
                chatsMenuNotification.visibility = View.VISIBLE
                chatsMenuNotificationNumber.visibility = View.VISIBLE
                chatsMenuNotificationNumber.text = newCount.toString()
            } else {
                chatsMenuNotification.visibility = View.INVISIBLE
                chatsMenuNotification.visibility = View.INVISIBLE
            }
        })

        mainViewModel.isMenuOpened.observe(viewLifecycleOwner, Observer { isOpened ->
            if (isOpened) {
                chatsClickArea.visibility = View.VISIBLE
            } else {
                chatsClickArea.visibility = View.GONE
            }
        })

        chatsViewModel.acceptedContacts.observe(viewLifecycleOwner, {
            Timber.v("Updated contacts")
        })
        chatsViewModel.acceptedGroups.observe(viewLifecycleOwner, {
            Timber.v("Updated groups")
        })

        chatsViewModel.mediatorLiveData.observe(requireActivity(), mediatorObject)
        chatsViewModel.chatsData.observe(viewLifecycleOwner) { chats ->
            //Timber.v("Chats coming UI: $chats")
            Timber.v("Is empty: ${chats.isEmpty()}")
            chatsLoading.hide()
            if (chats.isEmpty()) {
                closeBottomMenu()
                Timber.v("Not showing chats")
                showEmptyMessage()
                chatsRecyclerView?.post {
                    chatsAdapter.update(listOf())
                }
            } else {
                Timber.v("Showing chats")
                hideEmptyMessage()
                chatsRecyclerView?.post {
                    chatsAdapter.update(chats)
                }
            }
        }

        networkViewModel.networkState.observe(
            viewLifecycleOwner,
            Observer<NetworkState> { networkState ->
                Timber.v("Network State: $networkState")
                if (networkState == NetworkState.HAS_CONNECTION) {
                    networkStatusLayout?.visibility = View.GONE
                } else {
                    val bannerMsg = networkViewModel.getNetworkStateMessage(networkState)
                    networkStatusLayout?.visibility = View.VISIBLE
                    networkStatusText?.text = bannerMsg
                }
            })
    }

    private fun hideEmptyMessage() {
        chatsEmptyBottomTxt?.visibility = View.INVISIBLE
        chatsAddContactBtn?.visibility = View.INVISIBLE
    }

    private fun showEmptyMessage() {
        chatsLoading.hide()
        chatsEmptyBottomTxt?.visibility = View.VISIBLE
        chatsAddContactBtn?.visibility = View.VISIBLE
    }

    private fun resetSearchBar() {
        chatsRecyclerView?.post {
            val text = chatsSearchBar?.text?.trim()
            if (text != null && chatsAdapter.itemCount > 0) {
                Timber.v("Text search was restored!")
                chatsAdapter.filter.filter(text)
                if (chatsSearchBar?.text?.isBlank() != true) {
                    closeBottomMenu()
                }
            }
        }
    }

    private fun bindRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        chatsAdapter = ChatsListAdapter(daoRepo, schedulers)

        chatsRecyclerView.layoutManager = layoutManager
        chatsRecyclerView.adapter = chatsAdapter
        chatsRecyclerView.itemAnimator = null

        initTracker()
        attachItemHelper()
    }

    private fun initTracker() {
        tracker = CustomSelectionTracker(chatsRecyclerView, LongKeyProvider(chatsRecyclerView))
        tracker.addObserver(
            onSelectionChanged = { newSelection ->
                Timber.v("Selected items: $newSelection")
                if (::chatsSwipeController.isInitialized && chatsSwipeController.isDragging()) {
                    tracker.clearSelection()
                } else {
                    disableEnableMenuBtn(newSelection.size)

                    if (!chatsRecyclerView.isAnimating && !isBottomMenuOpen && newSelection.isNotEmpty()) {
                        openBottomMenu()
                    }
                    Timber.v("Selected: tracker: ${tracker.selection.toList()}")
                }
            },
            onSelectionCleared = {
                Timber.v("Selection cleared")
                disableEnableMenuBtn(0)
            }
        )

        chatsAdapter.tracker = tracker
    }

    private fun attachItemHelper() {
        chatsSwipeController = object : ButtonSwipeHelper(
            chatsRecyclerView,
            directions = ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
        ) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtonsLeft: MutableList<UnderlayButton>,
                underlayButtonsRight: MutableList<UnderlayButton>
            ) {
//                underlayButtonsLeft.add(createPinBtn())
                underlayButtonsRight.add(createDeleteButton())
//                underlayButtonsRight.add(createMuteBtn())
            }

            override fun onStartDragging() {
                tracker.disableTracker()
            }

            override fun onStopDragging() {
                Handler(Looper.getMainLooper()).postDelayed({
                    tracker.enableTracker()
                }, 50)
            }
        }
    }

    private fun getIdsListFromTracker(): List<Long> {
        return tracker.selection.toList()
    }

    private fun disableEnableMenuBtn(items: Int) {
        if (items == 0) {
            chatsBottomMenuPin?.alpha = 0.5f
            chatsBottomMenuMute?.alpha = 0.5f
            chatsBottomMenuDelete?.alpha = 0.5f
            chatsBottomMenuPin?.isClickable = false
            chatsBottomMenuMute?.isClickable = false
            chatsBottomMenuDelete?.isClickable = false
        } else {
            chatsBottomMenuPin?.alpha = 1.0f
            chatsBottomMenuMute?.alpha = 1.0f
            chatsBottomMenuDelete?.alpha = 1.0f
            chatsBottomMenuPin?.isClickable = true
            chatsBottomMenuMute?.isClickable = true
            chatsBottomMenuDelete?.isClickable = true
        }
    }

    private fun openBottomMenu() {
        chatsSwipeController.reset()
        chatsSwipeController.allowSwipe = false

        if (!isBottomMenuOpen) {
            isBottomMenuOpen = true
            chatsAdapter.selectionMode = SelectionMode.CHAT_SELECTION
            chatsSelectConversationBtn?.visibility = View.GONE
            chatsSearchUdBtn?.visibility = View.GONE
            chatsCancelMenu?.visibility = View.VISIBLE
            chatsBottomMenu?.visibility = View.VISIBLE
            chatsAdapter.notifyItemRangeChanged(0, chatsAdapter.itemCount)
        }
    }

    internal fun closeBottomMenu() {
        isBottomMenuOpen = false
        chatsSwipeController.reset()
        chatsSwipeController.allowSwipe = true

        chatsAdapter.selectionMode = SelectionMode.CHAT_ACCESS
        tracker.clearSelection()
        chatsSelectConversationBtn?.visibility = View.VISIBLE
        chatsSearchUdBtn?.visibility = View.VISIBLE
        chatsCancelMenu?.visibility = View.GONE
        chatsBottomMenu?.visibility = View.GONE

        refreshChat()
    }

    private fun refreshChat() {
        chatsRecyclerView.post {
            chatsAdapter.notifyItemRangeChanged(0, chatsAdapter.itemCount)
        }
    }

    private fun pinChats() {
        showError("Not implemented")
    }

    private fun muteChats() {
        showError("Not implemented")
    }

    private fun deleteChats(
        selectedChats: List<ChatWrapper>,
        selectedGroups: List<ChatWrapper>
    ) {
        showConfirmDialog(
            R.string.confirm_delete_chats_dialog_title,
            R.string.confirm_delete_chats_dialog_body,
            R.string.confirm_delete_chats_dialog_button,
            {
                chatsViewModel.deleteChats(selectedChats)
                selectedGroups.run {
                    if (isNotEmpty()) deleteGroups(this)
                }
            }
        )
    }

    private fun deleteGroups(selectedGroups: List<ChatWrapper>) {
        showConfirmDialog(
            R.string.confirm_delete_groups_dialog_title,
            R.string.confirm_delete_groups_dialog_body,
            R.string.confirm_delete_groups_dialog_button,
            { chatsViewModel.leaveGroups(selectedGroups) }
        )
    }

    private fun deleteAllChats() {
        showConfirmDialog(
            R.string.confirm_delete_all_chats_dialog_title,
            R.string.confirm_delete_all_chats_dialog_body,
            R.string.confirm_delete_all_chats_dialog_button,
            ::onDeleteAllChatsConfirmed,
            ::onDeleteAllChatsDismissed
        )
    }

    private fun onDeleteAllChatsConfirmed() {
        chatsViewModel.deleteAll()
        tracker.clearSelection(false)
    }

    private fun onDeleteAllChatsDismissed() {
        tracker.clearSelection(true)
    }

    private fun showDeleteDialog(pos: Int) {
        val chatsList = listOf((chatsAdapter.chatsFiltered[pos] as ChatWrapper))
        when (chatsList.first().item) {
            is ContactData -> deleteChats(chatsList, listOf())
            is GroupData -> deleteGroups(chatsList)
        }
    }

    private fun createDeleteButton(): UnderlayButton {
        return UnderlayButton(
            requireContext(),
            null,
            R.drawable.ic_trash_can,
            R.color.accent_danger,
            null,
            object : UnderlayButton.ButtonClickListener {
                override fun onClick(pos: Int) {
                    showDeleteDialog(pos)
                }
            })
    }

    private fun createMuteBtn(): UnderlayButton {
        return UnderlayButton(
            requireContext(),
            null,
            R.drawable.ic_mute,
            R.color.textLightTheme,
            null,
            object : UnderlayButton.ButtonClickListener {
                override fun onClick(pos: Int) {
                    showError("Not implemented")
                }
            })
    }

    private fun createPinBtn(): UnderlayButton {
        return UnderlayButton(
            requireContext(),
            null,
            R.drawable.ic_pin,
            R.color.greenLightTheme,
            null,
            object : UnderlayButton.ButtonClickListener {
                override fun onClick(pos: Int) {
                    showError("Not implemented")
                }
            })
    }
}
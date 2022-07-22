package io.xxlabs.messenger.ui.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ProgressDialog.show
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.setPadding
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.onNavDestinationSelected
import com.bumptech.glide.Glide
import com.google.android.material.shape.CornerFamily
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.NavMainDirections
import io.xxlabs.messenger.R
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.data.datatype.NetworkState
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.databinding.ComponentCustomToastBinding
import io.xxlabs.messenger.media.MediaProviderActivity
import io.xxlabs.messenger.notifications.MessagingService
import io.xxlabs.messenger.support.callback.NetworkWatcher
import io.xxlabs.messenger.support.dialog.PopupActionBottomDialog
import io.xxlabs.messenger.support.extensions.*
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.misc.DebugLogger
import io.xxlabs.messenger.support.toast.CustomToastActivity
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.support.util.DialogUtils
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.support.view.LooperCircularProgressBar
import io.xxlabs.messenger.support.view.SnackBarActivity
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.global.BaseInstance
import io.xxlabs.messenger.ui.global.ContactsViewModel
import io.xxlabs.messenger.ui.global.NetworkViewModel
import io.xxlabs.messenger.ui.main.chats.ChatsFragment
import io.xxlabs.messenger.ui.main.chats.ChatsViewModel
import io.xxlabs.messenger.ui.main.contacts.PhotoSelectorFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.component_menu.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.util.*
import javax.inject.Inject

private val Bundle.isPrivateMessage: Boolean
    get() = getByteArray(MainActivity.INTENT_PRIVATE_CHAT) != null

private val Bundle.isGroupMessage: Boolean
    get() = getByteArray(MainActivity.INTENT_GROUP_CHAT) != null

private val Bundle.isRequest: Boolean
    get() = getInt(MainActivity.INTENT_REQUEST, -1) != -1

class MainActivity : MediaProviderActivity(), SnackBarActivity, CustomToastActivity {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var networkViewModel: NetworkViewModel
    lateinit var contactsViewModel: ContactsViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var chatsViewModel: ChatsViewModel

    private lateinit var mainNavController: NavController
    private lateinit var progress: LooperCircularProgressBar
    private var webDialog: PopupActionBottomDialog? = null
    var isBackBtnAllowed = true
    var isMenuOpened = false


    override fun onStart() {
        super.onStart()
        showBiometrics()
        observeUI()
        watchObservables()
        mainViewModel.checkIsLoggedInReturn()
    }

    override fun onResume() {
        super.onResume()
        networkViewModel.syncRequests()
    }

    private fun doOnResume() {
        verifyFirebaseToken()
        initCallbacks()
        initGroupManager()
        networkViewModel.tryStartNetworkFollower()
    }

    private fun initGroupManager() {
        mainViewModel.initGroupManager()
    }

    override fun onPause() {
        hideBiometrics()
        super.onPause()
    }

    private fun showBiometrics() {
        (getCurrentFragment() as? BaseFragment)?.apply {
            if (this !is PhotoSelectorFragment) showBiometricsLogin()
        }
    }

    private fun hideBiometrics() {
        (getCurrentFragment() as? BaseFragment)?.hideBiometrics()
    }

    private fun login() {
        try {
            mainViewModel.login(this, rsaDecryptPwd())
        } catch (e: NullPointerException) {
            showError(e.message ?:
            "There was an error while initializing the app. Please reinstall."
            )
        }
    }

    override fun onStop() {
        if (mainViewModel.wasLoggedIn) {
            if (shouldStopNetworkFollower) networkViewModel.tryStopNetworkFollower()
            shouldStopNetworkFollower = true
        }

        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeInstances++
        setWindowSettings()
        bindControllers()
        setupMenu()
        initNetworkWatcher()

        mainReportBtn.setOnSingleClickListener {
            DebugLogger.exportLatestLog(this)
        }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        intent.getBundleExtra(INTENT_DEEP_LINK_BUNDLE)?.let {
            handleDeepLink(it)
        }
    }

    private fun handleDeepLink(bundle: Bundle) {
        with (bundle) {
            when {
                isPrivateMessage -> privateMessageIntent(this)
                isGroupMessage -> groupMessageIntent(this)
                isRequest -> requestIntent(this)
                else -> unknownIntent()
            }
        }
    }

    private fun privateMessageIntent(bundle: Bundle) {
        bundle.getByteArray(INTENT_PRIVATE_CHAT)?.let { chatId ->
            val privateChatDirections = NavMainDirections.actionGlobalChat().apply {
                contactId = chatId.toBase64String()
                contact = null
            }
            mainNavController.navigateSafe(
                privateChatDirections.actionId,
                privateChatDirections.arguments
            )
        }
    }

    private fun groupMessageIntent(bundle: Bundle) {
        bundle.getByteArray(INTENT_GROUP_CHAT)?.let { chatId ->
            val groupChatDirections = NavMainDirections.actionGlobalGroupsChat().apply {
                groupId = chatId.toBase64String()
                group = null
            }
            mainNavController.navigateSafe(
                groupChatDirections.actionId,
                groupChatDirections.arguments
            )
        }
    }

    private fun requestIntent(bundle: Bundle) {
        bundle.getInt(INTENT_REQUEST, 0).let { tab ->
            val requestDirections = NavMainDirections.actionGlobalRequests().apply {
                selectedTab = tab
            }
            mainNavController.navigateSafe(
                requestDirections.actionId,
                requestDirections.arguments
            )
        }
    }

    private fun unknownIntent() {
        Timber.d("Unknown intent received!")
    }

    override fun onDestroy() {
        activeInstances--
        super.onDestroy()
    }

    private fun verifyFirebaseToken() {
        mainViewModel.verifyFirebaseTokenChanged()
    }

    private fun bindControllers() {
        Timber.v("Main controllers initialized")
        mainViewModel =
            ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)

        chatsViewModel =
            ViewModelProvider(this, viewModelFactory).get(ChatsViewModel::class.java)

        networkViewModel =
            ViewModelProvider(this, viewModelFactory).get(NetworkViewModel::class.java)

        contactsViewModel =
            ViewModelProvider(this, viewModelFactory).get(ContactsViewModel::class.java)

        mainNavController = Navigation.findNavController(this, R.id.mainNavHost)
        mainNavController.addOnDestinationChangedListener { navController: NavController, _, _ ->
            hideKeyboard()
            val backStackCount = mainNavHost.childFragmentManager.backStackEntryCount
            println("backstack count: $backStackCount")
            setStatusBarColor()
        }
    }

    private fun setWindowSettings() {
        if (preferencesRepository.isHideAppEnabled) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }

        makeStatusBarTransparent()
        setContentView(R.layout.activity_main)
        progress = LooperCircularProgressBar(this, false)
    }

    private fun createDashboard() {
        webDialog = DialogUtils.getWebPopup(
            this,
            "Dashboard",
            "https://dashboard.xx.network/"
        ).apply { show() }
    }

    private fun createJoinXx() {
        webDialog = DialogUtils.getWebPopup(
            this,
            getString(R.string.menu_join_xx_network),
            "https://xx.network"
        ).apply { show() }
    }

    @SuppressLint("SetTextI18n")
    private fun setupMenu() {
        mainMenuView?.setInsets(
            bottomMask = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime(),
            topMask = WindowInsetsCompat.Type.systemBars()
        )

        menuBottomBuildTxt?.text =
            "Build (${BuildConfig.VERSION_CODE})"

        menuBottomVersionTxt?.text = "Version ${BuildConfig.VERSION_NAME.substringBefore("-")}"
        menuBottomSemVersionTxt?.text =
            if (!isMockVersion()) "xxdk version: ${BindingsWrapperBindings.getXxdkVersion()}"
            else "xxdk version: mock"

        menuChatsText?.setOnClickListener {
            hideMenu()
        }

        menuContactsTxt?.setOnSingleClickListener {
            hideMenu()
            mainNavController.navigateSafe(R.id.action_menu_pop_to_contacts)
        }

        menuContactRequestsTxt?.setOnSingleClickListener {
            hideMenu()
            mainNavController.navigateSafe(R.id.action_global_requests)
        }

        menuScanText?.setOnSingleClickListener {
            hideMenu()
            mainNavController.navigateSafe(R.id.action_global_qr_code)
        }

        menuSettingsTxt?.setOnSingleClickListener {
            hideMenu()
            mainNavController.navigateSafe(R.id.action_menu_pop_to_settings)
        }

        menuDashboardTxt?.setOnSingleClickListener {
            hideMenu()
            webDialog?.apply {
                if (isShowing) dismiss()
            }
            createDashboard()
        }

        menuJoinXxTxt?.setOnSingleClickListener {
            hideMenu()
            webDialog?.apply {
                if (isShowing) dismiss()
            }
            createJoinXx()
        }

        menuQrCode?.setOnSingleClickListener {
            hideMenu()
            mainNavController.navigateSafe(R.id.action_global_qr_code)
        }

        menuProfilePhotoHolder?.setOnSingleClickListener {
            hideMenu()
            mainNavController.navigateSafe(R.id.action_global_ud_profile)
        }

        menuViewProfile?.setOnSingleClickListener {
            hideMenu()
            mainNavController.navigateSafe(R.id.action_global_ud_profile)
        }

        menuUsername?.setOnSingleClickListener {
            hideMenu()
            mainNavController.navigateSafe(R.id.action_global_ud_profile)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            dispatchTouchEvent(MotionEvent.obtain(-1, -1, MotionEvent.ACTION_DOWN, 0f, 0f, 0))
            true
        } else {
            return item.onNavDestinationSelected(mainNavController) || super.onOptionsItemSelected(
                item
            )
        }
    }

    private fun observeUI() {
        contactsViewModel.showToast.onEach { toast ->
            toast?.let {
                showCustomToast(toast)
                contactsViewModel.onToastShown()
            }
        }.launchIn(lifecycleScope)

        contactsViewModel.navigateToChat.onEach { contact ->
            contact?.let {
                openChat(contact)
                contactsViewModel.onNavigateHandled()
            }
        }.launchIn(lifecycleScope)

//        networkViewModel.networkState.observe(this) { state ->
//            when (state) {
//                NetworkState.NO_CONNECTION -> showConnectingMessage()
//                NetworkState.NETWORK_STOPPED -> showDisconnectedMessage()
//            }
//        }
        networkViewModel.networkStatus.observe(this) { statusMessage ->
            dismissNetworkStatusMessage()
            statusMessage?.let {
                cachedNetworkStatus.add(it)
                showCustomToast(it)
            }
        }
    }

    private fun dismissNetworkStatusMessage() {
        for (status in cachedNetworkStatus) {
            dismissIndefiniteToast(status)
        }
    }

    private fun openChat(contact: Contact) {
        val bundle = bundleOf("contact_id" to contact.userId)
        mainNavController.navigateSafe(R.id.action_global_chat, bundle)
    }

    private fun requestFailedToast(message: String?) {
        showCustomToast(
            ToastUI.create(
                body = message ?: "One of your requests failed.",
                leftIcon = R.drawable.ic_danger,
                backgroundColor = R.color.accent_danger
            )
        )
        contactsViewModel.onToastShown()
    }

    private fun watchObservables() {
        mainViewModel.loginStatus.observe(this, Observer { result ->
            Timber.v("[LOGIN] Status - Main")
            if (result is DataRequestState.Success) {
                val isLoggedIn = result.data
                Timber.v("[LOGIN] Main - Is user logged in: $isLoggedIn")
                if (isLoggedIn) {
                    doOnResume()
                } else {
                    login()
                }

                mainViewModel.loginStatus.postValue(DataRequestState.Completed())
            }
        })

        mainViewModel.loginProcess.observe(this, { result ->
            Timber.v("[LOGIN] Login - Main")
            when (result) {
                is DataRequestState.Success -> {
                    Timber.v("[LOGIN] Success!")
                    finishLoginProcess()
                    doOnResume()
                }
                is DataRequestState.Error -> {
                    Timber.v("[LOGIN] Error!")
                    finishLoginProcess()
                    showError(result.error, true)
                }
                is DataRequestState.Completed -> {
                    Timber.v("[LOGIN] Completed!")
                    progress.setMsg(null)
                    progress.dismiss()
                }
                else -> {
                }
            }
        })

        mainViewModel.toggleMenu.observe(this, Observer { result ->
            if (result is DataRequestState.Success) {
                if (result.data == false && isMenuOpened) {
                    hideMenu()
                } else {
                    if (isMenuOpened) {
                        hideMenu()
                    } else {
                        showMenu()
                        showMenuPicture()
                    }
                }

                mainViewModel.isMenuOpened.value = isMenuOpened
                mainViewModel.toggleMenu.value = DataRequestState.Completed()
            }
        })

        mainViewModel.showSpinner.observe(this, { show ->
            if (show is DataRequestState.Start) {
                progress.show()
            } else {
                progress.dismiss()
            }
        })

        mainViewModel.spinnerMsg.observe(this, Observer {
            if (progress.isShowing) {
                progress.setMsg(it)
            }
        })

        contactsViewModel.newAuthRequestSent.observe(this, Observer { result ->
            Timber.v("New request sent - UI - $result")
            when (result) {
                is SimpleRequestState.Success -> {
                    createSnackMessage("One of your contact requests was successfully sent!")
                    contactsViewModel.newAuthRequestSent.postValue(SimpleRequestState.Completed())
                }
                is SimpleRequestState.Error -> {
                    requestFailedToast(result.error?.message)
                }
                else -> {
                    Timber.v("Completed new auth request")
                }
            }
        })

        contactsViewModel.newConfirmRequestSent.observe(this, Observer { result ->
            Timber.v("New confirm request - UI - $result")
            when (result) {
                is DataRequestState.Error -> {
                    requestFailedToast(result.error.message)
                }

                is DataRequestState.Success -> {

                }
                else -> {
                }
            }
        })

        contactsViewModel.newIncomingRequestReceived.observe(this, Observer { result ->
            Timber.v("New incoming request - UI - $result")
            if (result is SimpleRequestState.Success) {
                createSnackMessage("Private channel invitation received!")
                contactsViewModel.newIncomingRequestReceived.postValue(SimpleRequestState.Completed())
            } else {
                Timber.v("Completed new incoming contact")
            }
        })

        contactsViewModel.newConfirmationRequestReceived.observe(this, Observer { result ->
            Timber.v("New confirmation request - UI - $result")
            if (result is SimpleRequestState.Success) {
                Timber.v("Request is success")
                createSnackMessage("A contact has accepted your private channel request!")
                contactsViewModel.newConfirmationRequestReceived.postValue(SimpleRequestState.Completed())
            } else {
                Timber.v("Completed confirm contact post")
            }
        })

        contactsViewModel.requestsCount.observe(this, { newCount ->
            if (newCount > 0) {
                menuContactsNotification.visibility = View.VISIBLE
                menuContactsNotificationNumber.visibility = View.VISIBLE
                menuContactsNotificationNumber.text = newCount.toString()
            } else {
                menuContactsNotification.visibility = View.INVISIBLE
                menuContactsNotificationNumber.visibility = View.INVISIBLE
                menuContactsNotificationNumber.text = ""
            }
        })

        mainViewModel.newGroup.observe(this, { result ->
            Timber.v("New Group Request - UI - $result")
            if (result is SimpleRequestState.Success) {
                Timber.v("Request is success")
                createSnackMessage("Private Group invitation received!")
                mainViewModel.newGroup.postValue(SimpleRequestState.Completed())
            } else {
                Timber.v("Completed confirm contact post")
            }
        })
    }

    private fun showMenuPicture() {
        changeStatusBarColor(R.color.transparent)
        val name = mainViewModel.getUserName()
        menuUsername?.text = name
        val photo = preferences.userPicture
        if (photo.isNotBlank()) {
            val photoBitmap = photo.fromBase64toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(
                photoBitmap, 0, photoBitmap.size
            )
            menuProfilePhoto?.setImageBitmap(bitmap)
            menuProfilePhoto?.visibility = View.VISIBLE
            menuProfilePhotoDefault?.visibility = View.GONE
        } else {
            menuProfilePhoto?.visibility = View.GONE
            if (name.length >= 2) {
                menuProfilePhotoDefault?.text = name.substring(0, 2).uppercase()
                menuProfilePhotoDefault?.visibility = View.VISIBLE
            }
        }
    }

    private fun finishLoginProcess() {
        mainViewModel.loginProcess.value = DataRequestState.Completed()
    }

    private fun initCallbacks() {
        Timber.v("[MAIN] Initializing callbacks")
        networkViewModel.checkRegisterNetworkCallback()
        contactsViewModel.registerAuthCallback()
        networkViewModel.registerMessageListeners()
    }

    private fun initNetworkWatcher() {
        NetworkWatcher.startWatchingNetwork(
            this,
            onAvailable = {
                networkViewModel.setInternetState(true)
            },
            onLost = {
                networkViewModel.setInternetState(false)
            },
            onChanged = {
                if (mainViewModel.wasLoggedIn) {
                    networkViewModel.tryRestartNetworkFollower()
                } else {
                    Timber.v("[MAIN] Not logged in before")
                }
            },
        )
    }

    fun hideMenu(): ObjectAnimator? {
        if (isMenuOpened) {
            isMenuOpened = false
            mainViewModel.isMenuOpened.value = false
            mainContent.animate()
                .scaleX(1f).scaleY(1f)
                .translationX(0f)
                .translationY(0f)
                .setDuration(300)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        mainMenuView.visibility = View.GONE
                        resetCorners()
                    }
                })
                .start()
        }
        return null
    }

    private fun resetCorners() {
        mainContent.shapeAppearanceModel = mainContent.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, 0f)
            .setBottomLeftCorner(CornerFamily.ROUNDED, 0f)
            .build()
        mainContent.invalidate()
    }

    fun showMenu(): ObjectAnimator? {
        isMenuOpened = true
        mainViewModel.isMenuOpened.value = true
        mainContent.animate()
            .scaleX(0.85f).scaleY(0.80f)
            .translationX(mainContent.width / 1.75f)
            .translationY(mainContent.height / 30f)
            .setDuration(300)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    mainMenuView.visibility = View.VISIBLE
                    super.onAnimationStart(animation)
                    roundCorners()
                }
            })
            .start()
        return null
    }

    private fun roundCorners() {
        mainContent.shapeAppearanceModel = mainContent.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, Utils.dpToPx(24).toFloat())
            .setBottomLeftCorner(CornerFamily.ROUNDED, Utils.dpToPx(24).toFloat())
            .build()
        mainContent.invalidate()
    }

    private fun setStatusBarColor() {
        val currFragment = getCurrentFragmentId()
        Timber.v("Current fragment id: $currFragment")

        changeStatusBarIconTheme(lightMode = true)
    }

    private fun getCurrentFragmentId() = mainNavController.currentDestination?.id
    private fun getCurrentFragment() = mainNavHost.childFragmentManager.fragments[0]

    override fun onBackPressed() {
        hideMenu()

        if (isBackBtnAllowed) {
            when (getCurrentFragmentId()) {
                R.id.chatFragment -> {
                    mainNavController.navigateSafe(R.id.action_chat_pop_to_chats)
                    return
                }

                R.id.chatsFragment -> {
                    val fragment = getCurrentFragment()
                    if (fragment is ChatsFragment) {
                        when {
                            fragment.isBottomMenuOpen -> fragment.closeBottomMenu()
                            !mainNavController.popBackStack() -> finish()
                        }
                    }
                    return
                }

                R.id.contactsFragment -> {
                    mainNavController.navigateSafe(R.id.action_contacts_pop)
                    return
                }
                R.id.settingsFragment -> {
                    mainNavController.navigateSafe(R.id.action_settings_pop)
                    return
                }

                else -> {
                    dispatchTouchEvent(
                        MotionEvent.obtain(
                            -1,
                            -1,
                            MotionEvent.ACTION_DOWN,
                            0f,
                            0f,
                            0
                        )
                    )
                    mainNavController.navigateUp()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard()
        onBackPressed()
        return true
    }

    override fun createSnackMessage(msg: String, forceMessage: Boolean): Snackbar? {
        return if (preferences.areInAppNotificationsOn || forceMessage) {
//            val snack = Snackbar.make(mainLayout, msg, Snackbar.LENGTH_LONG).setAction("OK") {}
//            snack.view.translationZ = 10f
//            snack.show()
//            return snack
            val toastUI = ToastUI.create(body = msg, leftIcon = null)
            val snackBar = createCustomToast(toastUI)
            showCustomToast(toastUI)
            snackBar
        } else null
    }

    fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { focus ->
            imm.hideSoftInputFromWindow(focus.windowToken, 0)
        }
        findViewById<View>(R.id.mainLayout).requestFocus()
    }

    fun showBlurry() {
        if (mainBlurryImg.visibility != View.VISIBLE) {
            mainBlurryImg.visibility = View.VISIBLE
        }
    }

    fun hideBlurry() {
        if (mainBlurryImg.visibility != View.GONE) {
            Glide.with(this).clear(mainBlurryImg)
            mainBlurryImg.visibility = View.GONE
        }
    }

    companion object : BaseInstance {
        const val INTENT_DEEP_LINK_BUNDLE = "nav_bundle"
        const val INTENT_PRIVATE_CHAT = "private_message"
        const val INTENT_GROUP_CHAT = "group_message"
        const val INTENT_REQUEST = "request"

        private var activeInstances = 0
        override fun activeInstancesCount(): Int {
            return activeInstances
        }

        override fun isActive(): Boolean {
            return activeInstances > 0
        }
    }

    /**
     * Cache indefinite toasts, so they may be looked up for dismissal
     */
    private val indefiniteToasts: MutableMap<ToastUI, Snackbar> = mutableMapOf()
    private val cachedNetworkStatus: MutableList<ToastUI> = mutableListOf()

    override fun showCustomToast(ui: ToastUI) {
        createCustomToast(ui).apply {
            if (ui.duration == LENGTH_INDEFINITE) indefiniteToasts[ui] = this
            show()
        }
    }

    override fun dismissIndefiniteToast(ui: ToastUI) {
        indefiniteToasts[ui]?.run {
            indefiniteToasts.remove(ui)
            dismiss()
        }
    }

    private fun createCustomToast(ui: ToastUI): Snackbar {
        val snackBar = Snackbar.make(findViewById(R.id.customToastView), "", ui.duration)
        val binding = ComponentCustomToastBinding.inflate(layoutInflater)
        binding.ui = ui
        (snackBar.view as Snackbar.SnackbarLayout).apply {
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(48)
            addView(binding.root, 0)

            val params = layoutParams as CoordinatorLayout.LayoutParams
            params.gravity = Gravity.TOP
            layoutParams = params
        }

        snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        return snackBar
    }
}
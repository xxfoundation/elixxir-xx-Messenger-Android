package io.xxlabs.messenger.ui.global

import android.app.Application
import android.os.CountDownTimer
import android.text.SpannableStringBuilder
import androidx.core.text.bold
import androidx.lifecycle.*
import bindings.NetworkHealthCallback
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.data.datatype.Environment
import io.xxlabs.messenger.data.datatype.NetworkFollowerStatus
import io.xxlabs.messenger.data.datatype.NetworkState
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.requests.data.contact.ContactRequestsRepository
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.toast.ToastUI
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.support.util.value
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NetworkViewModel @Inject constructor(
    val app: Application,
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    val schedulers: SchedulerProvider,
    private val requestsDataSource: ContactRequestsRepository
) : ViewModel() {

    /**
     * Exposes the network status as user-friendly toasts.
     */
    private val _networkStatus = MutableLiveData<ToastUI?>(null)
    val networkStatus: LiveData<ToastUI?> = Transformations.distinctUntilChanged(_networkStatus)

    private val noConnectionStatus: ToastUI by lazy {
        val body = NetworkState.NO_CONNECTION.statusMessage ?: ""
        val duration = LENGTH_INDEFINITE

        ToastUI.create(
            body = body,
            duration = duration,
            leftIcon = null
        )
    }

    var subscriptions = CompositeDisposable()
    private var networkState = MutableLiveData<NetworkState>()
    var userDiscoveryStatus = MutableLiveData<Boolean>()
    var networkFollowerTimer: CountDownTimer? = null
    var networkFollowerSeconds = -1
    private var checkForRequests = true
    private var lastTimeHealthy: Long = Utils.getCurrentTimeStamp()

    private var isFirstTimeNetwork = true
    private var isNetworkHealthy: Boolean = false
        set(value) {
            if (value && checkForRequests) {
                field = value
                syncRequests()
                checkForRequests = false
            }
        }
    private var wasNetworkHealthy: Boolean = false
    private var isInternetConnected = true
    private var isUdTryingToRun = false
    private var init = false
    private lateinit var networkHealthCallback: NetworkHealthCallback

    init {
        if (init) {
            Timber.v("[NETWORK VIEWMODEL] Network view model is init already")
        } else {
            init = true
            Timber.v("[NETWORK VIEWMODEL] Network view model was initiated")
        }
        Timber.v("[NETWORK VIEWMODEL] isMessageListenerRegistered: ${isMessageListenerRegistered()}")
        Timber.v("[NETWORK VIEWMODEL] isNetworkCallbackRegistered: ${isNetworkListenerRegistered()}")
    }

    fun syncRequests() {
        viewModelScope.launch {
            // Replay requests if the network is already healthy...
            if (isNetworkHealthy) {
                repo.replayRequests()
                syncContacts(repo.getPartners())
            } else {
                // ...or set a flag to replay requests when it becomes healthy.
                checkForRequests = true
            }
        }
    }

    private suspend fun syncContacts(partnerIds: List<String>) {
        val contacts = daoRepo.getAllContacts().value()
        partnerIds.forEach { partnerId ->
            contacts
                .filter { it.userId.toBase64String() == partnerId }
                .forEach {
                    if (it.status != RequestStatus.ACCEPTED.value) {
                        updateToAccepted(it)
                    }
                }
        }
    }

    private suspend fun updateToAccepted(contact: ContactData): Int =
        daoRepo.updateContactState(contact.userId, RequestStatus.ACCEPTED).value()

    private fun getNetworkCallback() = NetworkHealthCallback { isHealthy ->
        subscriptions.add(Observable.fromCallable { onHealthChangeCallback(isHealthy) }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .doOnComplete {
                checkNetworkConnection()
            }
            .subscribe()
        )
    }

    private fun onHealthChangeCallback(isHealthy: Boolean) {
        isNetworkHealthy = isHealthy
        checkLastHealthyTime(isHealthy)

        if (isNetworkHealthy != wasNetworkHealthy) {
            wasNetworkHealthy = isNetworkHealthy

            if (isFirstTimeNetwork) {
                isFirstTimeNetwork = false
            }
        }
    }

    private fun checkLastHealthyTime(isHealthy: Boolean) {
        if (Utils.getCurrentTimeStamp() - lastTimeHealthy > 5000) {
            val calendar = Calendar.getInstance()
            val formatter = SimpleDateFormat.getDateTimeInstance()
            calendar.time = Date(lastTimeHealthy)

            Timber.v("[NETWORK VIEWMODEL] is healthy: $isHealthy")
            Timber.v("[NETWORK VIEWMODEL] last health status change: ${formatter.format(calendar.time)}")

            lastTimeHealthy = Utils.getCurrentTimeStamp()
        }
    }

    private fun checkNetworkConnection(
        isFirstTimeNetwork: Boolean = this.isFirstTimeNetwork,
        isInternetConnected: Boolean = this.isInternetConnected,
        isNetworkHealthy: Boolean = this.isNetworkHealthy
    ) {
        networkState.value = when {
            isMockVersion() || isInternetConnected -> NetworkState.HAS_CONNECTION
            isFirstTimeNetwork -> NetworkState.FIRST_TIME
            !isNetworkHealthy -> NetworkState.NETWORK_STOPPED
            else -> NetworkState.NO_CONNECTION
        }
        _networkStatus.value = createStatusMessage(networkState.value)
    }

    private val NetworkState.statusMessage: String?
        get() {
            return when (ordinal) {
                NetworkState.HAS_CONNECTION.ordinal -> null
                else -> appContext().getString(R.string.network_state_connecting)
            }
        }

    private fun createStatusMessage(status: NetworkState?): ToastUI? =
        status?.let {
            when (it.ordinal) {
                NetworkState.HAS_CONNECTION.ordinal -> null
                else -> noConnectionStatus
            }
        }

    fun getNetworkStateMessage(currentNetworkState: NetworkState): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder()
        when (currentNetworkState) {
            NetworkState.FIRST_TIME -> {
                spannableStringBuilder
                    .bold { append("Connecting to xx network...") }
            }

            NetworkState.NO_CONNECTION -> {
                spannableStringBuilder
                    .bold { append("No internet connection.") }
            }

            NetworkState.HAS_CONNECTION -> {

            }

            NetworkState.NETWORK_STOPPED -> {
                spannableStringBuilder
                    .bold { append("Can't connect to xx network.") }
            }
        }

        return spannableStringBuilder
    }

    private fun checkStopNetworkTimer() {
        if (isNetworkFollowerTimerOn()) {
            Timber.v("[NETWORK VIEWMODEL] Network follower countdown has stopped!")
            networkFollowerTimer!!.cancel()
            networkFollowerTimer = null
            networkFollowerSeconds = -1
        }
    }

    fun tryStartNetworkFollower(onStartCallback: ((Boolean) -> (Unit))? = null) {
        val networkStatus = repo.getNetworkFollowerStatus()
        Timber.v("[NETWORK VIEWMODEL] has network follower already started: $networkStatus")
        if (networkStatus == NetworkFollowerStatus.RUNNING) {
            checkStopNetworkTimer()
        } else if (networkStatus == NetworkFollowerStatus.STOPPED) {
            startNetworkFollower(onStartCallback)
        }
    }

    fun tryStopNetworkFollower() {
       if (networkFollowerTimer == null && repo.getNetworkFollowerStatus() == NetworkFollowerStatus.RUNNING) {
            networkFollowerTimer = object : CountDownTimer(30_000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    networkFollowerSeconds = (millisUntilFinished / 1000).toInt()
                    Timber.v("[NETWORK VIEWMODEL] Stopping network follower in ${millisUntilFinished / 1000} seconds.")
                }

                override fun onFinish() {
                    networkFollowerTimer = null
                    networkFollowerSeconds = -1
                    stopNetworkFollower()
                }
            }

            networkFollowerTimer?.start()
        }
    }

    private fun isNetworkFollowerTimerOn() =
        networkFollowerTimer != null && networkFollowerSeconds >= 0

    private fun startNetworkFollower(onStartCallback: ((Boolean) -> (Unit))? = null) {
        val elapsedTime = System.currentTimeMillis()
        subscriptions.add(
            repo.startNetworkFollower()
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .doOnSuccess {
                    Timber.v("[NETWORK VIEWMODEL] Network follower is RUNNING")
                    Timber.v("[NETWORK VIEWMODEL] Started network follower in: ${elapsedTime - System.currentTimeMillis()}ms")
                    onStartCallback?.invoke(it)
                    newUserDiscovery()
                }
                .doOnError { err ->
                    Timber.v("[NETWORK VIEWMODEL] Network follower ERROR - could not start properly: ${err.localizedMessage}")
                    onStartCallback?.invoke(false)
                }.subscribe()
        )
    }

    private fun stopNetworkFollower() {
        subscriptions.add(
            repo.stopNetworkFollower()
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .doOnSuccess {
                    wasNetworkHealthy = false
                    isNetworkHealthy = false
                    isFirstTimeNetwork = true
                    resetNetworkState()
                    requestsDataSource.failUnverifiedRequests()
                    Timber.v("[NETWORK VIEWMODEL] Network follower is NOT RUNNING")
                }
                .doOnError { err ->
                    Timber.v("[NETWORK VIEWMODEL] Network follower ERROR - could not stop properly: ${err.localizedMessage}")
                }
                .subscribe()
        )
    }

    fun tryRestartNetworkFollower() {
        val networkStatus = repo.getNetworkFollowerStatus()
        Timber.v("network follower status: $networkStatus")
        if (networkStatus == NetworkFollowerStatus.RUNNING) {
            repo.stopNetworkFollower()
                .flatMap {
                    repo.startNetworkFollower()
                }
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .doOnSuccess {
                    resetNetworkState()
                    Timber.v("[NETWORK VIEWMODEL] network follower reinitialized with success")
                }
                .doOnError { err ->
                    Timber.v("[NETWORK VIEWMODEL] ERROR - could not restart properly: ${err.localizedMessage}")
                }
                .subscribe()
        }
    }

    fun checkRegisterNetworkCallback() {
        if (!this::networkHealthCallback.isInitialized
            && BuildConfig.ENVIRONMENT != Environment.MOCK
        ) {
            Timber.v("[MAIN] nor initialized, initializing network callback...")
            networkHealthCallback = getNetworkCallback()
            registerNetworkCallback()
        } else {
            Timber.v("[MAIN] Network callback is already initialized")
            isNetworkHealthy = true
            checkNetworkConnection()
        }
    }

    private fun registerNetworkCallback() {
        repo.registerNetworkHealthCb(networkHealthCallback)
            .retryWhen { errors ->
                errors.delay(3, TimeUnit.SECONDS)
            }
            .doOnError { err ->
                err.printStackTrace()
                Timber.e("[NETWORK VIEWMODEL] Registered network callback FAILED: ${err.localizedMessage}")
            }
            .doOnSuccess {
                setNetworkCallbackRegistered()
                Timber.v("[NETWORK VIEWMODEL] Registered network callback SUCCESS")
            }
            .subscribeOn(schedulers.single)
            .subscribe()
    }

    fun registerMessageListeners() {
        Timber.v("[MAIN] Registering message listener...")
        if (!isMessageListenerRegistered()) {
            Timber.v("[MAIN] Not initialized, starting message listener...")
            repo.registerMessageListener()
                .retryWhen { errors ->
                    errors.delay(3, TimeUnit.SECONDS)
                }
                .doOnError { err ->
                    err.printStackTrace()
                    Timber.e("[NETWORK VIEWMODEL] Registered message listener FAILED")
                }
                .doOnSuccess {
                    setMessageListenerRegistered()
                    Timber.v("[NETWORK VIEWMODEL] Registered message listener SUCCESS")
                }
                .subscribeOn(schedulers.single)
                .subscribe()
        } else {
            Timber.v("[MAIN] Message listener is already registered...")
        }
    }

    fun newUserDiscovery() {
        if (!isUdTryingToRun && !isUserDiscoveryRunning()) {
            isUdTryingToRun = true
            Timber.v("Starting user discovery...")
            subscriptions.add(
                repo.newUserDiscovery()
                    .subscribeOn(schedulers.single)
                    .observeOn(schedulers.main)
                    .doOnError { err ->
                        Timber.e("[NETWORK VIEWMODEL] Failed to register user discovery: ${err.localizedMessage}")
                        isUdTryingToRun = false
                        userDiscoveryStatus.value = false
                    }.doOnSuccess {
                        Timber.v("[NETWORK VIEWMODEL] User discovery registered with success!")
                        setUserDiscoveryRunning()
                        isUdTryingToRun = false
                        userDiscoveryStatus.value = true
                    }.subscribe()
            )
        }
    }

    fun setInternetState(hasInternet: Boolean) {
        isInternetConnected = hasInternet
        checkNetworkConnection(isInternetConnected = hasInternet)
    }

    private fun resetNetworkState() {
        networkState.value = NetworkState.FIRST_TIME
    }

    private fun isMessageListenerRegistered(): Boolean {
        return XxMessengerApplication.isMessageListenerRegistered
    }

    private fun setMessageListenerRegistered() {
        XxMessengerApplication.isMessageListenerRegistered = true
    }

    private fun isNetworkListenerRegistered(): Boolean {
        return XxMessengerApplication.isNetworkCallbackRegistered
    }

    private fun setNetworkCallbackRegistered() {
        XxMessengerApplication.isNetworkCallbackRegistered = true
    }

    fun isUserDiscoveryRunning(): Boolean {
        return XxMessengerApplication.isUserDiscoveryRunning
    }

    private fun setUserDiscoveryRunning() {
        XxMessengerApplication.isUserDiscoveryRunning = true
    }

    override fun onCleared() {
        subscriptions.clear()
        super.onCleared()
    }

    fun hasConnection(): Boolean {
        return if (isMockVersion()) {
            true
        } else {
            networkState.value == NetworkState.HAS_CONNECTION
        }
    }
}
package io.xxlabs.messenger.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.bindings.BindingsWrapperBindings
import io.xxlabs.messenger.bindings.wrapper.groups.group.GroupBase
import io.xxlabs.messenger.bindings.wrapper.groups.message.GroupMessageReceiveBase
import io.xxlabs.messenger.data.data.DataRequestState
import io.xxlabs.messenger.data.data.PayloadWrapper
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.data.room.model.GroupMessageData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.requests.data.group.GroupInvitationData
import io.xxlabs.messenger.requests.data.group.GroupRequestsRepository
import io.xxlabs.messenger.requests.data.group.InvitationMigrator
import io.xxlabs.messenger.requests.model.GroupInvitation
import io.xxlabs.messenger.support.extensions.toBase64String
import io.xxlabs.messenger.support.isMockVersion
import io.xxlabs.messenger.support.util.Utils
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val repo: BaseRepository,
    private val daoRepo: DaoRepository,
    private val preferences: PreferencesRepository,
    private val schedulers: SchedulerProvider,
    private val invitationsDataSource: GroupRequestsRepository
) : ViewModel() {
    var isMenuOpened = MutableLiveData<Boolean>()
    var subscriptions = CompositeDisposable()

    var showSpinner = MutableLiveData<DataRequestState<Boolean>>()
    var toggleMenu = MutableLiveData<DataRequestState<Any>>()
    var spinnerMsg = MutableLiveData<String>()
    var loginStatus = MutableLiveData<DataRequestState<Boolean>>()
    var loginProcess = MutableLiveData<DataRequestState<Boolean>>()
    var newGroup = MutableLiveData<SimpleRequestState<Any>>()
    private var isLoggingIn: Boolean = false
    private var hasManagerStarted: Boolean = false

    @Volatile
    var wasLoggedIn = false

    init {
        migrateOldInvitations()
    }

    private fun migrateOldInvitations() {
        viewModelScope.launch {
            InvitationMigrator.performMigration(invitationsDataSource, daoRepo)
        }
    }

    fun checkIsLoggedInReturn() {
        subscriptions.add(
            repo.isLoggedIn()
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .doOnError { err ->
                    Timber.e("[LOGIN] Is logged in error: ${err.localizedMessage}")
                    loginStatus.value = (DataRequestState.Error(err))
                }
                .doOnSuccess { isLoggingIn ->
                    Timber.e("[LOGIN] is logged in: $isLoggingIn")
                    wasLoggedIn = isLoggingIn
                    loginStatus.value = (DataRequestState.Success(isLoggingIn))
                }.subscribe()
        )
    }

    fun getUserBioKey(): String {
        return preferences.userBiometricKey
    }

    fun hideMenu() {
        toggleMenu.postValue(DataRequestState.Success(false))
    }

    fun toggleMenu() {
        toggleMenu.postValue(DataRequestState.Success(Any()))
    }

    fun getUserName(): CharSequence {
        return preferences.name
    }

    fun verifyFirebaseTokenChanged() {
        if (preferences.areNotificationsOn) {
            Timber.v("Verifying firebase token")
            Timber.v("Old token: ${preferences.notificationsTokenId}")
            Timber.v("Curr token: ${preferences.currentNotificationsTokenId}")
            if (isCurrentTokenNotEmpty() && isCurrentTokenDifferent()) {
                renewNotifications()
            } else {
                Timber.v("Token remains the same, not renewed")
            }
        } else {
            Timber.v("Notifications are not enabled")
        }
    }

    fun enableNotifications(callback: (Throwable?) -> (Unit)) {
        subscriptions.add(
            repo.registerNotificationsToken()
                .subscribeOn(schedulers.single)
                .observeOn(schedulers.main)
                .subscribeBy(
                    onSuccess = { token ->
                        Timber.d("new token successfully sent! $this")
                        preferences.areNotificationsOn = true
                        preferences.currentNotificationsTokenId = token!!
                        preferences.notificationsTokenId = token
                    },
                    onError = { err ->
                        Timber.e("error on sending token ${err.localizedMessage}")
                        callback.invoke(err)
                    })
        )
    }

    private fun renewNotifications() {
        Timber.v("Renewing notifications token...")
        subscriptions.add(
            repo.registerNotificationsToken()
                .subscribeOn(schedulers.single)
                .subscribeBy(
                    onSuccess = {
                        preferences.notificationsTokenId = preferences.currentNotificationsTokenId
                        Timber.d("new token successfully sent! $this")
                    },
                    onError = { err ->
                        Timber.e("error on sending token ${err.localizedMessage}")
                    })
        )
    }

    private fun isCurrentTokenDifferent() =
        preferences.currentNotificationsTokenId != preferences.notificationsTokenId

    private fun isCurrentTokenNotEmpty() =
        preferences.currentNotificationsTokenId.isNotEmpty()

    override fun onCleared() {
        Timber.v("Is Finishing")
        val updated = daoRepo.pendingMessagesToFailed().subscribeOn(schedulers.io).blockingGet()
        Timber.d("updated messages: $updated")
        subscriptions.clear()
        super.onCleared()
    }

    @Throws(NullPointerException::class)
    fun login(context: Context, rsaDecryptPwd: ByteArray) {
        if (isLoggingIn) return
        isLoggingIn = true

        val appFolderPath = getAppFolder(context).absolutePath

        Timber.v("[LOGIN] Logging in...")
        loginProcess.postValue(DataRequestState.Start())

        if (!wasLoggedIn) {
            subscriptions.add(
                repo.loginSingle(appFolderPath, rsaDecryptPwd)
                    .subscribeOn(schedulers.single)
                    .observeOn(schedulers.main)
                    .doOnSuccess { userId ->
                        Timber.v("[LOGIN] My userId: $userId")
                        Timber.v("[LOGIN] My userId (String): ${userId.decodeToString()}")
                        Timber.v("[LOGIN] My userId (String b64): ${userId.toBase64String()}")
                        wasLoggedIn = true
                        completeLogin()
                    }
                    .doOnError { err ->
                        Timber.e("[LOGIN] Error: ${err.localizedMessage}")
                        if (err.localizedMessage?.contains("cannot login when another session already exists") == true) {
                            wasLoggedIn = true
                            completeLogin()
                        } else {
                            completeLogin(err)
                        }
                    }.subscribe()
            )
        } else {
            Timber.v("[LOGIN] Already logged in")
            completeLogin()
        }
    }

    private fun getAppFolder(context: Context): File {
        Timber.v("[LOGIN] Verifying app folder contents...")
        val appFolder = BindingsWrapperBindings.getSessionFolder(context)

        return with(appFolder.listFiles()) {
            when {
                isNullOrEmpty() -> {
                    Timber.v("[LOGIN] App folder is empty! Critical files are missing.")
                    throw NullPointerException("Critical files are missing from disk! Please reinstall the app.")
                }
                else -> {
                    Timber.v("[LOGIN] Found $size files in app folder.")
                    appFolder
                }
            }
        }
    }

    fun initGroupManager() {
        if (!hasManagerStarted) {
            repo.initGroupManager({ group ->
                handleGroupRequest(group)
            }, { messageReceived ->
                handleMessageReceivedRequest(messageReceived)
            })
            hasManagerStarted = true
        }
    }

    private fun handleGroupRequest(group: GroupBase) {
        Timber.v(
            "[GROUP REQUEST] Group request has arrived from group ${
                group.getID().toBase64String()
            }"
        )
        Timber.v(
            "[GROUP REQUEST] Group name is ${group.getName().decodeToString()}"
        )

        val status = RequestStatus.VERIFIED
        subscriptions.add(daoRepo.createUserGroup(group, status)
            .flatMap {
                daoRepo.insertGroupMemberShip(
                    group.getID(),
                    group.getMembership(),
                    preferences.getUserId(),
                    repo.getStoredUsername()
                )
            }
            .flatMap {
                val initMessage = group.initMessage()
                if (!initMessage.isNullOrBlank()) {
                    val payload: String = if (isMockVersion()) {
                        PayloadWrapper(initMessage).toString()
                    } else {
                        ChatMessage.buildCmixMsg(initMessage)
                    }
                    Timber.v("[GROUP REQUEST] Initial message found")
                    daoRepo.insertGroupMessage(
                        GroupMessageData(
                            groupId = group.getID(),
                            receiver = group.getID(),
                            sender = group.getMembership()[0].getID(),
                            status = MessageStatus.RECEIVED.value,
                            payload = payload,
                            timestamp = group.getCreatedMs(),
                        )
                    )
                } else {
                    Timber.v("[GROUP REQUEST] Initial message NOT found")
                    Single.just(it)
                }
            }
            .subscribeOn(schedulers.io)
            .observeOn(schedulers.main)
            .doOnError { err -> Timber.e(err) }
            .doOnSuccess {
                newGroup.value = SimpleRequestState.Success(it)
                invitationsDataSource.save(
                    GroupInvitationData(
                        model = GroupData.from(group, status),
                        unread = true
                    )
                )
            }
            .subscribe())
    }

    private fun handleMessageReceivedRequest(messageReceived: GroupMessageReceiveBase) {
        Timber.v(
            "[GROUP MESSAGE RECEIVED] Received message from ${
                messageReceived.getGroupId().toBase64String()
            }"
        )

        Timber.v("Group Bindings timestamp (nano): ${messageReceived.getTimestampNano()}")
        Timber.v("Java timestamp (nano): ${Utils.getCurrentTimeStampNano()}")

        Timber.v("Group Bindings timestamp (ms): ${messageReceived.getTimestampMs()}")
        Timber.v("Java timestamp (ms): ${Utils.getCurrentTimeStamp()}")
        Timber.v("Kronos timestamp (ms): ${XxMessengerApplication.kronosClock.getCurrentNtpTimeMs()}")

        subscriptions.add(
            daoRepo.insertReceivedGroupMessage(
                senderId = messageReceived.getSenderId(),
                groupId = messageReceived.getGroupId(),
                payloadString = messageReceived.getPayloadString(),
                timestamp = messageReceived.getTimestampMs(),
                messageId = messageReceived.getMessageId(),
                messageReceived.getRoundUrl()
            ).subscribeOn(schedulers.io)
                .observeOn(schedulers.main)
                .doOnError { err -> Timber.e(err) }
                .doOnSuccess { inserted ->
                    if (inserted == -1L) {
                        Timber.v("[GROUP MESSAGE RECEIVED] Discarding msg - payload of received msg was null")
                    } else {
                        Timber.v("[GROUP MESSAGE RECEIVED] Successfully inserted received group message")
                    }
                }.subscribe()
        )
    }

    private fun completeLogin(err: Throwable? = null) {
        isLoggingIn = false
        if (err == null) {
            wasLoggedIn = true
            enableDummyTraffic(preferences.isCoverTrafficOn)
            loginProcess.postValue(DataRequestState.Success(true))
        } else {
            loginProcess.postValue(DataRequestState.Error(err))
        }
    }

    fun enableDummyTraffic(enabled: Boolean) {
        preferences.isCoverTrafficOn = enabled
        repo.enableDummyTraffic(enabled)
    }

    fun allPendingMessagesToFailed(): Int? {
        return daoRepo.pendingMessagesToFailed().subscribeOn(schedulers.io).blockingGet()
    }
}
package io.xxlabs.messenger.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import bindings.Bindings
import bindings.NotificationForMeReport
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.xxlabs.messenger.BuildConfig
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import io.xxlabs.messenger.ui.intro.splash.SplashScreenPlaceholderActivity
import io.xxlabs.messenger.ui.main.MainActivity
import timber.log.Timber
import javax.inject.Inject

class MessagingService : FirebaseMessagingService(), HasAndroidInjector {

    @Inject
    lateinit var serviceInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = serviceInjector

    @Inject
    lateinit var preferencesRepo: PreferencesRepository

    @Inject
    lateinit var repo: BaseRepository

    @Inject
    lateinit var schedulers: SchedulerProvider

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (XxMessengerApplication.isActivityVisible()) return

        val notificationState = NotificationState()

        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Timber.v("[NOTIFICATION] Notification data payload: %s", remoteMessage.data)
            val msg = remoteMessage.data

            try {
                Timber.v("[NOTIFICATION] PreImages %s", preferencesRepo.preImages)
                val isNotificationForMeReport = Bindings.notificationsForMe(
                    msg[NOTIFICATION_DATA],
                    preferencesRepo.preImages
                )

                for (i in 0 until isNotificationForMeReport.len()) {
                    with (isNotificationForMeReport[i]) {
                        if (this.shouldNotify() && !notificationState.alreadySent(this.type())) {
                            pushNotification(this)
                            notificationState.sent(this.type())
                        }
                    }
                }
            } catch (err: Exception) {
                Timber.v("[NOTIFICATION] Error: ${err.localizedMessage}")
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Timber.d("[NOTIFICATION] Message Notification Body: ${it.body}")
        }
    }

    /**
     * Called if InstanceID token is updated. This is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Timber.d("[NOTIFICATION] Refreshed token: $token")
        saveNewToken(token)
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    private fun pushNotification(richNotification: NotificationForMeReport) {
        increaseNotificationCount()
        val _notificationId = notificationId

        val pendingIntent = generatePendingIntent(
            generateIntent(),
            _notificationId
        )
        val notification = RichNotifications.create(
            this,
            pendingIntent,
            richNotification.notificationText(),
            richNotification.channelId()
        )

        createChannelAndNotify(richNotification, notification, _notificationId)
        wakeScreenUp()
    }

    private fun increaseNotificationCount() {
        notificationCount++
        Timber.v("[NOTIFICATION] Notification count: $notificationCount")
    }

    private fun generateIntent(): Intent =
        if (MainActivity.isActive()) Intent(this, MainActivity::class.java)
        else Intent(this, SplashScreenPlaceholderActivity::class.java)

    private fun generatePendingIntent(
        intent: Intent,
        notificationId: Int
    ) = PendingIntent.getActivity(
        this,
        notificationId,
        intent,
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun createChannelAndNotify(
        richNotification: NotificationForMeReport,
        notification: Notification,
        notificationId: Int,
    ) {
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                richNotification.channelId(),
                richNotification.channelName(),
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(
            BuildConfig.APPLICATION_ID,
            notificationId,
            notification
        )
    }

    private fun wakeScreenUp() {
        val pm: PowerManager = getSystemService(POWER_SERVICE) as PowerManager
        val isScreenOn = pm.isInteractive

        if (!isScreenOn) {
            val wl = pm.newWakeLock(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "xxmessenger:notificationLock"
            )
            wl.acquire(10000)
        }
    }

    /**
     * Send token to the network.
     * @param token The new token.
     */
    private fun saveNewToken(token: String) {
        Timber.v("[NOTIFICATION] Saving new firebase token: $token")
        preferencesRepo.currentNotificationsTokenId = token
    }

    private fun NotificationForMeReport.shouldNotify(): Boolean =
        forMe() && (!isDefault() && !isSilent())

    private fun NotificationForMeReport.isGroup(): Boolean =
        type().equals("group", true)

    private fun NotificationForMeReport.isRequest(): Boolean =
        type().equals("request", true)

    private fun NotificationForMeReport.isGroupRequest(): Boolean =
        type().equals("groupRq", true)

    private fun NotificationForMeReport.isDefault(): Boolean =
        type().equals("default", true)

    private fun NotificationForMeReport.isConfirm(): Boolean =
        type().equals("confirm", true)

    private fun NotificationForMeReport.isSilent(): Boolean =
        type().equals("silent", true)

    private fun NotificationForMeReport.isE2E(): Boolean =
        type().equals("e2e", true)

    private fun NotificationForMeReport.isEndFT(): Boolean =
        type().equals("endFT", true)

    private fun NotificationForMeReport.notificationText(): String =
        when {
            isGroupRequest() -> getString(R.string.notification_group_request_text)
            isRequest() -> getString(R.string.notification_request_text)
            isConfirm() -> getString(R.string.notification_confirm_text)
            isE2E() -> getString(R.string.notification_e2e_text)
            isGroup() -> getString(R.string.notification_group_text)
            isEndFT() -> getString(R.string.notification_endft_text)
            else -> "New activity" // Other types should not be displayed in the first place.
        }

    private fun NotificationForMeReport.channelId(): String =
        when {
            isRequest() || isGroupRequest() -> getString(R.string.request_notification_channel_id)
            isConfirm() -> getString(R.string.confirm_notification_channel_id)
            isE2E() || isEndFT() -> getString(R.string.e2e_notification_channel_id)
            isGroup() -> getString(R.string.group_notification_channel_id)
            else -> getString(R.string.default_notification_channel_id)
        }

    private fun NotificationForMeReport.channelName(): String =
        when {
            isRequest() || isGroupRequest() -> getString(R.string.notification_request_channel_label)
            isConfirm() -> getString(R.string.notification_confirm_channel_label)
            isE2E() || isEndFT() -> getString(R.string.notification_e2e_channel_label)
            isGroup() -> getString(R.string.notification_group_channel_label)
            else -> getString(R.string.default_notification_channel_label)
        }

    companion object {
        private const val NOTIFICATION_DATA = "notificationsTag"
        var notificationCount = 0
        private val notificationId
            get() = System.currentTimeMillis().toInt()
    }
}

private class NotificationState {
    private val notificationSentMap = hashMapOf(
        "groupRq" to false,
        "group" to false,
        "request" to false,
        "confirm" to false,
        "e2e" to false,
        "endFT" to false,
    )

    fun sent(type: String) {
        notificationSentMap[type] = true
    }

    fun alreadySent(type: String) = notificationSentMap[type] ?: false
}
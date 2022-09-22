package io.xxlabs.messenger.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.xxlabs.messenger.R

class RichNotifications {
    companion object {
        fun create(
            context: Context,
            pendingIntent: PendingIntent,
            text: String,
            channelId: String
        ): Notification = defaultBuilder(context, channelId, text, pendingIntent)
            .build()

        private fun defaultBuilder(
            context: Context,
            defaultChannelId: String,
            text: String,
            pendingIntent: PendingIntent,
            defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
        ) = NotificationCompat.Builder(context, defaultChannelId)
            .setSmallIcon(R.drawable.ic_notification_icon_small)
            .setColor(ContextCompat.getColor(context, R.color.brand_dark))
            .setContentTitle(context.getString(R.string.xx_app_name))
            .setContentText(text)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000))
            .setLights(Color.WHITE, 1000, 2500)
            .setNumber(1)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
    }
}
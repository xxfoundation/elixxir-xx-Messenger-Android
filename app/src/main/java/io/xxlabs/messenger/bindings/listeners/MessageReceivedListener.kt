package io.xxlabs.messenger.bindings.listeners

import bindings.Listener
import bindings.Message
import com.google.protobuf.InvalidProtocolBufferException
import data.proto.CMIXText
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.PrivateMessageData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.extensions.toBase64String
import timber.log.Timber
import javax.inject.Inject

//Msg Type 2
class MessageReceivedListener @Inject constructor(
    private val daoRepo: DaoRepository,
    private val schedulers: SchedulerProvider,
    private val preferences: PreferencesRepository
)/* : Listener */{
    var subscriptions = CompositeDisposable()

    init {
        Timber.v("Message listener started")
    }

//    override fun hear(message: Message) {
//        try {
//            val cmixText = CMIXText.parseFrom(message.payload)
//            val timestamp = message.timestampMS
//            val javaTimestamp = System.currentTimeMillis()
//            // Get the text of the message
//
//            Timber.v("Bindings timestamp (ms): ${message.timestampMS}")
//            Timber.v("Bindings timestamp (nano): ${message.timestampNano}")
//            Timber.v("Java timestamp (ms): $javaTimestamp")
//            Timber.v("Kronos timestamp (ms): ${XxMessengerApplication.kronosClock.getCurrentNtpTimeMs()}")
//
//            val senderId = message.sender
//            Timber.v(
//                "Received Message and attempting to save it %s, %s, %s",
//                cmixText,
//                timestamp,
//                senderId
//            )
//
//            insertMsgByContactId(
//                message.id,
//                senderId,
//                preferences.getUserId(),
//                cmixText,
//                timestamp,
//                message.roundURL
//            )
//        } catch (e: InvalidProtocolBufferException) {
//            Timber.e(e.localizedMessage)
//        }
//    }

//    override fun name(): String {
//        return this::javaClass.get().simpleName
//    }

    private fun insertMsgByContactId(
        uniqueMsgId: ByteArray,
        contactUserId: ByteArray,
        recipientId: ByteArray,
        text: CMIXText,
        timestamp: Long,
        roundUrl: String?
    ) {
        val msg = PrivateMessageData(
            status = MessageStatus.RECEIVED.value,
            payload = text.toByteArray().toBase64String(),
            timestamp = timestamp,
            unread = true,
            sender = contactUserId,
            receiver = recipientId,
            uniqueId = uniqueMsgId,
            roundUrl = roundUrl
        )
        Timber.v("Msg was received, trying to save...")

        subscriptions.add(
            daoRepo.insertMessage(msg)
                .subscribeOn(schedulers.io)
                .observeOn(schedulers.io)
                .subscribeBy(
                    onError = { err ->
                        Timber.e("Error on inserting message ${err.localizedMessage}")
                    },
                    onSuccess = { msgId ->
                        Timber.v("Insert message id: $msgId")
                    }
                )
        )
    }

    companion object {
        @Volatile
        var instance: MessageReceivedListener? = null

        fun getInstance(
            daoRepo: DaoRepository,
            schedulers: SchedulerProvider,
            preferences: PreferencesRepository
        ): MessageReceivedListener {
            return instance ?: synchronized(this) {
                val messageReceivedListener = MessageReceivedListener(
                    daoRepo,
                    schedulers,
                    preferences
                )
                instance = messageReceivedListener
                messageReceivedListener
            }
        }
    }
}
package io.xxlabs.messenger.bindings.listeners

import com.google.protobuf.InvalidProtocolBufferException
import data.proto.CMIXText
import io.elixxir.xxclient.callbacks.MessageListener
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.data.datatype.MessageStatus
import io.xxlabs.messenger.data.room.model.PrivateMessageData
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.support.extensions.fromBase64toByteArray
import io.xxlabs.messenger.support.extensions.toBase64String
import timber.log.Timber
import javax.inject.Inject
import io.elixxir.xxclient.models.Message as BindingsMessage

//Msg Type 2
class MessageReceivedListener @Inject constructor(
    private val daoRepo: DaoRepository,
    private val schedulers: SchedulerProvider,
) : MessageListener {
    var subscriptions = CompositeDisposable()

    override val name: String = this::javaClass.get().simpleName

    override fun onMessageReceived(message: BindingsMessage) {
        try {
            val cmixText = CMIXText.parseFrom(message.payload.fromBase64toByteArray())
            val timestamp = message.timestamp
            val javaTimestamp = System.currentTimeMillis()
            // Get the text of the message

            Timber.v("Bindings timestamp (ms): ${message.timestamp}")
            Timber.v("Bindings timestamp (nano): ${message.timestamp}")
            Timber.v("Java timestamp (ms): $javaTimestamp")
            Timber.v("Kronos timestamp (ms): ${XxMessengerApplication.kronosClock.getCurrentNtpTimeMs()}")

            Timber.v(
                "Received Message and attempting to save it %s, %s, %s",
                cmixText,
                timestamp,
                message.sender
            )

            insertMsgByContactId(
                message.id.fromBase64toByteArray(),
                message.sender.fromBase64toByteArray(),
                message.recipientId.fromBase64toByteArray(),
                cmixText,
                timestamp,
                message.roundUrl
            )
        } catch (e: InvalidProtocolBufferException) {
            Timber.e(e.localizedMessage)
        }
    }

    init {
        Timber.v("Message listener started")
    }

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
        ): MessageReceivedListener {
            return instance ?: synchronized(this) {
                val messageReceivedListener = MessageReceivedListener(
                    daoRepo,
                    schedulers,
                )
                instance = messageReceivedListener
                messageReceivedListener
            }
        }
    }
}
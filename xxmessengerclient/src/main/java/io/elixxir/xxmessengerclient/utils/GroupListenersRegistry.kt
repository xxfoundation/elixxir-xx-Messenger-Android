package io.elixxir.xxmessengerclient.utils

import io.elixxir.xxclient.callbacks.GroupMessageListener
import io.elixxir.xxclient.callbacks.GroupRequestListener
import io.elixxir.xxclient.group.Group
import io.elixxir.xxclient.models.GroupChatMessage
import io.elixxir.xxclient.utils.ReceptionId
import io.elixxir.xxclient.utils.RoundId

class GroupListenersRegistry {
    private var groupMessageListener: GroupMessageListener? = null
    private var groupRequestListener: GroupRequestListener? = null

    private var groupMessageCache: GroupMessageCache? = null
    private var groupRequestCache: GroupRequestCache? = null

    fun getMessageListener(): GroupMessageListener {
        return groupMessageListener ?: groupMessageCache ?: run {
            GroupMessageCache().apply {
                groupMessageCache = this
            }
        }
    }

    fun setMessageListener(listener: GroupMessageListener) {
        groupMessageCache?.setListener(listener) ?: run {
            groupMessageListener = listener
        }
    }

    fun getRequestListener(): GroupRequestListener {
        return groupRequestListener ?: groupRequestCache ?: run {
            GroupRequestCache().apply {
                groupRequestCache = this
            }
        }
    }

    fun setRequestListener(listener: GroupRequestListener) {
        groupRequestCache?.setListener(listener) ?: run {
            groupRequestListener = listener
        }
    }
}

private data class GroupMessageData(
    val decryptedMessage: GroupChatMessage?,
    val message: ByteArray?,
    val receptionId: ReceptionId?,
    val ephemeralId: Long,
    val roundId: RoundId,
    val error: Exception?
)

private class GroupMessageCache : GroupMessageListener {

    private var listener: GroupMessageListener? = null
    private val messageCache = mutableListOf<GroupMessageData>()
    override val name: String = "GroupMessageCache"

    fun setListener(listener: GroupMessageListener) {
        this.listener = listener
        sendMessages(listener)
        clearCache()
    }

    private fun sendMessages(listener: GroupMessageListener) {
        messageCache.forEach {
            listener.onMessageReceived(
                it.decryptedMessage,
                it.message,
                it.receptionId,
                it.ephemeralId,
                it.roundId,
                it.error
            )
        }
    }

    private fun clearCache() {
        messageCache.clear()
    }

    override fun onMessageReceived(
        decryptedMessage: GroupChatMessage?,
        message: ByteArray?,
        receptionId: ReceptionId?,
        ephemeralId: Long,
        roundId: RoundId,
        error: Exception?
    ) {
        listener?.onMessageReceived(
            decryptedMessage, message, receptionId, ephemeralId, roundId, error
        ) ?: run {
            messageCache.add(
                GroupMessageData(decryptedMessage, message, receptionId, ephemeralId, roundId, error)
            )
        }
    }

}

private class GroupRequestCache : GroupRequestListener {
    private var listener: GroupRequestListener? = null
    private val requestCache = mutableListOf<Group>()

    fun setListener(listener: GroupRequestListener) {
        this.listener = listener
        sendRequests(listener)
        clearCache()
    }

    private fun sendRequests(listener: GroupRequestListener) {
        requestCache.forEach {
            listener.onRequestReceived(it)
        }
    }

    private fun clearCache() {
        requestCache.clear()
    }

    override fun onRequestReceived(group: Group) {
        listener?.onRequestReceived(group) ?: run {
            requestCache.add(group)
        }
    }
}
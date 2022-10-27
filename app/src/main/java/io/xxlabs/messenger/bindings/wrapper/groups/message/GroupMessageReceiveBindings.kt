package io.xxlabs.messenger.bindings.wrapper.groups.message

import data.proto.CMIXText
import io.elixxir.xxclient.models.GroupChatMessage
import io.elixxir.xxclient.utils.ReceptionId
import io.elixxir.xxclient.utils.RoundId
import io.xxlabs.messenger.support.extensions.toBase64String

data class GroupMessageReceiveBindings(
    private val decryptedMessage: GroupChatMessage?,
    private val message: ByteArray?,
    private val receptionId: ReceptionId?,
    private val ephemeralId: Long,
    private val roundId: RoundId,
    private val error: Exception?
) : GroupMessageReceiveBase {
    override fun getEphemeralId(): Long = ephemeralId

    override fun getGroupId(): ByteArray = decryptedMessage?.groupId ?: byteArrayOf()

    override fun getMessageId(): ByteArray = decryptedMessage?.messageId ?: byteArrayOf()

    override fun getPayload(): ByteArray = decryptedMessage?.payload ?: byteArrayOf()

    override fun getPayloadString(): String? {
        return if (getPayload()contentEquals(byteArrayOf())) {
             null
        } else CMIXText.parseFrom(getPayload()).toByteArray().toBase64String()
    }

    override fun getRecipientId(): ByteArray = receptionId ?: byteArrayOf()

    override fun getRoundId(): Long = roundId

    override fun getSenderId(): ByteArray = decryptedMessage?.senderId ?: byteArrayOf()

    override fun getRoundTimestampNano(): Long = decryptedMessage?.timestamp ?: 0

    override fun getTimestampNano(): Long = decryptedMessage?.timestamp ?: 0

    override fun getTimestampMs(): Long = decryptedMessage?.timestamp ?: 0

    override fun getRoundUrl(): String? = null
}
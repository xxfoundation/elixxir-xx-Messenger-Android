package io.xxlabs.messenger.requests.ui.send

import io.xxlabs.messenger.data.room.model.Contact
import java.io.Serializable


interface SendRequestListener {
    fun sendRequest(request: OutgoingRequest)
}

interface OutgoingRequest : Serializable{
    val sender: RequestSender
    val receiver: Contact
}
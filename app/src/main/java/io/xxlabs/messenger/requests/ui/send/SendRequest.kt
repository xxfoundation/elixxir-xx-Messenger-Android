package io.xxlabs.messenger.requests.ui.send

import android.text.*
import android.text.Annotation
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.Country
import io.xxlabs.messenger.data.room.model.Contact
import io.xxlabs.messenger.support.appContext
import java.io.Serializable

interface RequestSender : Serializable {
    val email: String?
    val phone: String?
}

class SendRequest(
    private val sender: RequestSender,
    private val receiver: Contact,
    private val listener: SendRequestListener
) : SendRequestUI {
    override val body: Spanned = getSpannedBody(receiver.displayName, receiver.receiverFact())

    override val senderEmail: String = sender.email ?: getPlaceholder()
    override val senderPhone: String = formattedPhone() ?: getPlaceholder()
    override val emailToggleEnabled: Boolean = !sender.email.isNullOrBlank()
    override val phoneToggleEnabled: Boolean = !sender.phone.isNullOrBlank()

    private fun formattedPhone(): String? = sender.phone?.let { Country.toFormattedNumber(it) }

    override val positiveLabel: Int = R.string.send_request_positive_button
    override val negativeLabel: Int = R.string.send_request_negative_button
    override val positiveButtonEnabled: LiveData<Boolean> = MutableLiveData(true)

    private var includeEmail = false
    private var includePhone = false

    private fun getPlaceholder(): String =
        appContext().getString(R.string.send_request_fact_placeholder)

    override fun onEmailToggled(enabled: Boolean) { includeEmail = enabled }

    override fun onPhoneToggled(enabled: Boolean) { includePhone = enabled }

    override fun onCloseClicked() {}

    override fun onPositiveClick() {
        listener.sendRequest(createOutgoingRequest())
    }

    private fun createOutgoingRequest(): OutgoingRequest {
        val requestSender = object : RequestSender {
            override val email: String? = if (includeEmail) senderEmail else null
            override val phone: String? = if (includePhone) senderPhone else null
        }
        return object : OutgoingRequest {
            override val sender: RequestSender = requestSender
            override val receiver: Contact = this@SendRequest.receiver
        }
    }

    override fun onNegativeClick() {}

    private fun Contact.receiverFact(): String? {
        return email.ifBlank { phone.ifBlank { null } }
    }

    private fun getSpannedBody(receiverName: String, receiverFact: String? = null): Spanned {
        val body = appContext().getText(R.string.send_request_body) as SpannedString
        val spannedBody = SpannableStringBuilder(body)
        val highlight = appContext().getColor(R.color.brand_default)
        return spannedBody.applyFormatString(receiverName, receiverFact, highlight)
    }

    private fun SpannableStringBuilder.applyFormatString(
        username: String,
        userFact: String?,
        color: Int
    ): Spanned {
        val annotations = getSpans(0, this.length, Annotation::class.java)
        annotations.forEach { annotation ->
            when (annotation.value) {
                "receiverName" -> {
                    replace(
                        this.getSpanStart(annotation),
                        this.getSpanEnd(annotation),
                        "$username "
                    )
                    setSpan(
                        ForegroundColorSpan(color),
                        getSpanStart(annotation),
                        getSpanEnd(annotation),
                        SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                "receiverFact" -> {
                    replace(
                        this.getSpanStart(annotation),
                        this.getSpanEnd(annotation),
                        userFact?.let { "($it) " } ?: ""
                    )
                    userFact?.let {
                        setSpan(
                            ForegroundColorSpan(color),
                            getSpanStart(annotation),
                            getSpanEnd(annotation),
                            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }
        return this
    }
}
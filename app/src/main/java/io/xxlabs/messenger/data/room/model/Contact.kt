package io.xxlabs.messenger.data.room.model

import io.xxlabs.messenger.data.data.Country
import java.io.Serializable

interface Contact : Serializable {
    val id: Long
    val userId: ByteArray
    val username: String
    val status: Int
    val nickname: String
    val photo: ByteArray?
    val email: String
    val phone: String
    val marshaled: ByteArray?
    val createdAt: Long
    val displayName: String
    val initials: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    fun hasFacts(): Boolean
}

fun Contact.formattedEmail(): String? =
    if (email.isNotBlank()) email.substring(1)
    else null

fun Contact.formattedPhone(flagEmoji: Boolean = false): String? =
    if (phone.isNotBlank()) Country.toFormattedNumber(phone, flagEmoji)
    else null

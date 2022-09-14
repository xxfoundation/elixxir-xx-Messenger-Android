package io.xxlabs.messenger.ui.intro.registration.tfa

import io.xxlabs.messenger.data.datatype.FactType
import java.io.Serializable

interface TwoFactorAuthCredentials : Serializable {
    val confirmationId: String
    val factType: FactType
    val fact: String
    val countryCode: String get() = "US"

    companion object Factory {
        fun create(
            confirmationId: String,
            factType: FactType,
            fact: String,
            countryCode: String = "US"
        ): TwoFactorAuthCredentials {
            return TfaCredentials(
                confirmationId,
                factType,
                fact,
                countryCode
            )
        }
    }
}

data class TfaCredentials(
    override val confirmationId: String,
    override val factType: FactType,
    override val fact: String,
    override val countryCode: String = "US"
) : TwoFactorAuthCredentials

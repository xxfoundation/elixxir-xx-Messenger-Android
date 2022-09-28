package io.elixxir.core.preferences.model

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import io.elixxir.core.preferences.PreferencesRepository
import javax.inject.Inject

class EncrpytedPreferences @Inject internal constructor(
    @ApplicationContext context: Context,
) : PreferencesRepository {
    private val masterKeyAlias = "xx_preferences_key"
    private val preferencesAlias = "xx_preferences"

    private val masterKeySpec = KeyGenParameterSpec.Builder(
        masterKeyAlias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setDigests(KeyProperties.DIGEST_SHA256)
        .setRandomizedEncryptionRequired(true)
        .setKeySize(256)
        .build()

    private val masterKey =
        MasterKey.Builder(context, masterKeyAlias).setKeyGenParameterSpec(masterKeySpec).build()

    override val preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        preferencesAlias,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
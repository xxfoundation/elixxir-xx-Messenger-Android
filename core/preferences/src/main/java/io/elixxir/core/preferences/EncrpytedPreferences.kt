package io.elixxir.core.preferences

import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

class EncrpytedPreferences {

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

    var preferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        preferencesAlias,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
package io.xxlabs.messenger.registration.keygen

import androidx.lifecycle.ViewModel
import io.xxlabs.messenger.keystore.KeyStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class KeyGenViewModel(
    private val keyStoreManager: KeyStoreManager
) : ViewModel() {

    val keyGenerationComplete: Flow<Boolean> by :: _keyGenerationComplete
    private val _keyGenerationComplete = MutableStateFlow(false)

    val keyGenError: Flow<Exception?> by ::_keyGenError
    private val _keyGenError = MutableStateFlow(null)

    init {

    }
}
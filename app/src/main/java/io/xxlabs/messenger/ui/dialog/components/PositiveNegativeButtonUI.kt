package io.xxlabs.messenger.ui.dialog.components

import androidx.lifecycle.LiveData

interface PositiveNegativeButtonUI {
    val positiveLabel: Int
    val negativeLabel: Int
    val positiveButtonEnabled: LiveData<Boolean>

    fun onPositiveClick()
    fun onNegativeClick()
}
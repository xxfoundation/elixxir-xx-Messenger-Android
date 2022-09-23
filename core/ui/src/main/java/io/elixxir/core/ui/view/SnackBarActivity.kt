package io.elixxir.core.ui.view

import com.google.android.material.snackbar.Snackbar

interface SnackBarActivity {
    fun createSnackMessage(msg: String, forceMessage: Boolean = false): Snackbar?
}
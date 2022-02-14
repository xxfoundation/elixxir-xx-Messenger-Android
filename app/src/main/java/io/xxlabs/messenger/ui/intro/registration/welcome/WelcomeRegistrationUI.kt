package io.xxlabs.messenger.ui.intro.registration.welcome

import android.text.Spanned

interface WelcomeRegistrationUI {
    fun welcomeTitle(username: String): Spanned
    fun onWelcomeInfoClicked()
    fun onWelcomeNextClicked()
    fun onWelcomeSkipClicked()
}
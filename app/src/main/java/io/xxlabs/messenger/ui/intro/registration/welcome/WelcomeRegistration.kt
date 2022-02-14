package io.xxlabs.messenger.ui.intro.registration.welcome

import android.app.Application
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.dialog.info.InfoDialogUI
import io.xxlabs.messenger.support.dialog.info.SpanConfig
import javax.inject.Inject

class WelcomeRegistration @Inject constructor(
    private val application: Application,
) : WelcomeRegistrationController {

    override val welcomeNavigateNext: LiveData<Boolean> get() = navigateNext
    private val navigateNext = MutableLiveData(false)

    override val welcomeNavigateSkip: LiveData<Boolean> get() = navigateSkip
    private val navigateSkip = MutableLiveData(false)

    override val welcomeDialogUI: InfoDialogUI by lazy {
        InfoDialogUI.create(
            title = application.getString(R.string.registration_welcome_dialog_title),
            body = application.getString(R.string.registration_welcome_dialog_body),
            spans = mutableListOf(
                SpanConfig.create(
                    application.getString(R.string.registration_welcome_dialog_link_text),
                    application.getString(R.string.registration_welcome_dialog_link_url)
                )
            )
        )
    }

    override val welcomeInfoClicked: LiveData<Boolean> get() = infoClicked
    private val infoClicked = MutableLiveData(false)

    override fun onWelcomeInfoClicked() {
        if (infoClicked.value == true) return
        infoClicked.value = true
    }

    override fun onWelcomeInfoHandled() {
        infoClicked.value = false
    }

    override fun onWelcomeNavigateHandled() {
        navigateNext.value = false
        navigateSkip.value = false
    }

    override fun onWelcomeNextClicked() {
        if (navigateSkip.value == true) return
        navigateNext.value = true
    }

    override fun onWelcomeSkipClicked() {
        if (navigateNext.value == true) return
        navigateSkip.value = true
    }

    override fun welcomeTitle(username: String): Spanned {
        val highlight = application.getColor(R.color.brand_default)
        val title = application.getString(R.string.registration_welcome_title, username)
        val startIndex = title.indexOf("xx network", ignoreCase = true)

        return SpannableString(title).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex,
                title.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}
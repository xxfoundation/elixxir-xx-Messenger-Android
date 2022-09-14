package io.xxlabs.messenger.ui.intro.registration.success

import android.app.Application
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.ui.intro.registration.success.RegistrationStep.*
import javax.inject.Inject

class CompletedRegistrationStep @Inject constructor(
    private val application: Application
) : CompletedRegistrationStepController {

    private val emailNavigateNextStep = MutableLiveData(false)
    private val emailNextButtonEnabled = MutableLiveData(true)

    private val phoneNavigateNextStep = MutableLiveData(false)
    private val phoneNextButtonEnabled = MutableLiveData(true)

    private val restoreNavigateNextStep = MutableLiveData(false)
    private val restoreNextButtonEnabled = MutableLiveData(true)

    override val completedStepDescription: String =
        appContext().getString(R.string.backup_restore_successful_description)

    override fun getCompletedStepTitle(step: RegistrationStep): Spanned {
        return when (step) {
            EMAIL, PHONE -> getTfaCompletedStepTitle(step)
            RESTORE -> getAccountRestoredStepTitle()
        }
    }

    private fun getTfaCompletedStepTitle(step: RegistrationStep): Spanned {
        val factString = step.toString().lowercase()
        val highlight = application.getColor(R.color.black)
        val title = application.getString(
            R.string.registration_successfully_added_title,
            factString
        )

        val firstSpanStartIndex = title.indexOf(factString, ignoreCase = true)
        val firstSpanEndIndex = firstSpanStartIndex + factString.length

        val secondSpanText = "added"
        val secondSpanStartIndex = title.indexOf(secondSpanText, ignoreCase = true)

        return SpannableString(title).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                firstSpanStartIndex,
                firstSpanEndIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(highlight),
                secondSpanStartIndex,
                title.length-1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun getAccountRestoredStepTitle(): Spanned {
        val highlight = appContext().getColor(R.color.black)
        val title = appContext().getString(R.string.backup_restore_successful_title)

        val span1 = appContext().getString(R.string.backup_restore_successful_title_span_1)
        val startIndex1 = title.indexOf(span1, ignoreCase = true)

        val span2 = appContext().getString(R.string.backup_restore_successful_title_span_2)
        val startIndex2 = title.indexOf(span2, ignoreCase = true)

        return SpannableString(title).apply {
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex1,
                startIndex1 + span1.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(highlight),
                startIndex2,
                startIndex2 + span2.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    override fun getNextButtonText(step: RegistrationStep): String {
        return when (step) {
            PHONE, RESTORE -> application.getString(R.string.registration_flow_done)
            EMAIL -> application.getString(R.string.registration_flow_next)
        }
    }

    override fun onCompletedStepNavigate(step: RegistrationStep): LiveData<Boolean> {
        return when (step) {
            EMAIL -> emailNavigateNextStep
            PHONE -> phoneNavigateNextStep
            RESTORE -> restoreNavigateNextStep
        }
    }

    override fun isCompletedStepNextEnabled(step: RegistrationStep): LiveData<Boolean> {
        return when (step) {
            EMAIL -> emailNextButtonEnabled
            PHONE -> phoneNextButtonEnabled
            RESTORE -> restoreNextButtonEnabled
        }
    }

    override fun onCompletedStepNavigateHandled(step: RegistrationStep) {
        when (step) {
            EMAIL -> {
                emailNavigateNextStep.value = false
                enableEmailUI()
            }
            PHONE -> {
                phoneNavigateNextStep.value = false
                enablePhoneUI()
            }
            RESTORE -> restoreNavigateNextStep.value = false
        }
    }

    override fun onCompletedStepNextClicked(step: RegistrationStep) {
        when (step) {
            EMAIL -> {
                emailNavigateNextStep.value = true
                disableEmailUI()
            }
            PHONE -> {
                phoneNavigateNextStep.value = true
                disablePhoneUI()
            }
            RESTORE -> {
                restoreNavigateNextStep.value = true
            }
        }
    }

    private fun disablePhoneUI() {
        phoneNextButtonEnabled.value = false
    }

    private fun disableEmailUI() {
        emailNextButtonEnabled.value = false
    }

    private fun enablePhoneUI() {
        phoneNextButtonEnabled.value = true
    }

    private fun enableEmailUI() {
        emailNextButtonEnabled.value = true
    }
}
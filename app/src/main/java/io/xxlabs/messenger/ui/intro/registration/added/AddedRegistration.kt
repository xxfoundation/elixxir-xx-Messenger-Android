package io.xxlabs.messenger.ui.intro.registration.added

import android.app.Application
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.FactType
import javax.inject.Inject

class AddedRegistration @Inject constructor(
    private val application: Application
) : AddedRegistrationController {

    private val emailNavigateNextStep = MutableLiveData(false)
    private val emailNextButtonEnabled = MutableLiveData(true)

    private val phoneNavigateNextStep = MutableLiveData(false)
    private val phoneNextButtonEnabled = MutableLiveData(true)

    override fun getAddedTitle(factType: FactType): Spanned {
        val factString = factType.toString().lowercase()
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

    override fun getNextButtonText(factType: FactType): String {
        return when (factType) {
            FactType.PHONE -> application.getString(R.string.registration_flow_done)
            FactType.EMAIL -> application.getString(R.string.registration_flow_next)
            else -> "Ok"
        }
    }

    override fun onAddedNavigateNextStep(factType: FactType): LiveData<Boolean> {
        return when (factType) {
            FactType.EMAIL -> emailNavigateNextStep
            FactType.PHONE -> phoneNavigateNextStep
            else -> MutableLiveData(true)
        }
    }

    override fun isAddedNextButtonEnabled(factType: FactType): LiveData<Boolean> {
        return when (factType) {
            FactType.EMAIL -> emailNextButtonEnabled
            FactType.PHONE -> phoneNextButtonEnabled
            else -> MutableLiveData(true)
        }
    }

    override fun onAddedNavigateHandled(factType: FactType) {
        when (factType) {
            FactType.EMAIL -> {
                emailNavigateNextStep.value = false
                enableEmailUI()
            }
            FactType.PHONE -> {
                phoneNavigateNextStep.value = false
                enablePhoneUI()
            }
        }
    }

    override fun onAddedNextClicked(factType: FactType) {
        when (factType) {
            FactType.EMAIL -> {
                emailNavigateNextStep.value = true
                disableEmailUI()
            }
            FactType.PHONE -> {
                phoneNavigateNextStep.value = true
                disablePhoneUI()
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
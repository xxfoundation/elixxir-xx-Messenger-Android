package io.xxlabs.messenger.ui.intro.registration.success

import android.text.Spanned
import androidx.lifecycle.LiveData

enum class RegistrationStep {
    PHONE, EMAIL, RESTORE
}

interface CompletedRegistrationStepUI {
    val completedStepDescription: String
    fun getCompletedStepTitle(step: RegistrationStep): Spanned
    fun getNextButtonText(step: RegistrationStep): String
    fun isCompletedStepNextEnabled(step: RegistrationStep): LiveData<Boolean>
    fun onCompletedStepNextClicked(step: RegistrationStep)
}

interface CompletedRegistrationStepController : CompletedRegistrationStepUI {
    fun onCompletedStepNavigate(step: RegistrationStep): LiveData<Boolean>
    fun onCompletedStepNavigateHandled(step: RegistrationStep)
}
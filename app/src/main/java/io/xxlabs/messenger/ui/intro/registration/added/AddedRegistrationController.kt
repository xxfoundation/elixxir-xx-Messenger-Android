package io.xxlabs.messenger.ui.intro.registration.added

import androidx.lifecycle.LiveData
import io.xxlabs.messenger.data.datatype.FactType

interface AddedRegistrationController : AddedRegistrationUI {
    fun onAddedNavigateNextStep(factType: FactType): LiveData<Boolean>
    fun onAddedNavigateHandled(factType: FactType)
}


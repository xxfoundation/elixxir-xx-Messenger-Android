package io.xxlabs.messenger.ui.intro.registration.added

import android.text.Spanned
import androidx.lifecycle.LiveData
import io.xxlabs.messenger.data.datatype.FactType

interface AddedRegistrationUI {
    fun getAddedTitle(factType: FactType): Spanned
    fun getNextButtonText(factType: FactType): String
    fun isAddedNextButtonEnabled(factType: FactType): LiveData<Boolean>
    fun onAddedNextClicked(factType: FactType)
}
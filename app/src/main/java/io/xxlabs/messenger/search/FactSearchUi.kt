package io.xxlabs.messenger.search

import android.text.Editable
import androidx.lifecycle.LiveData

interface FactSearchUi {
    val countryCode: LiveData<String?>
    val searchHint: String
    val userInputEnabled: LiveData<Boolean>

    fun onCountryClicked()
    fun onSearchInput(editable: Editable?)
}
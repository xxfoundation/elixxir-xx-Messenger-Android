package io.xxlabs.messenger.ui.main.groups.create

import androidx.lifecycle.LiveData

interface CreateGroupDialogController : CreateGroupDialogUI {
    val maxGroupNameLength: Int
    val maxDescriptionLength: Int
    val nameError: LiveData<String?>
    val descriptionError: LiveData<String?>
    val createButtonEnabled: LiveData<Boolean>
}
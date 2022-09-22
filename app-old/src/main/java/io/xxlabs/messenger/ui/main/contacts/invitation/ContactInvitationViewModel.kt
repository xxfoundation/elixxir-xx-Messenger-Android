package io.xxlabs.messenger.ui.main.contacts.invitation

import androidx.lifecycle.ViewModel
import io.xxlabs.messenger.application.SchedulerProvider
import io.xxlabs.messenger.repository.DaoRepository
import io.xxlabs.messenger.repository.base.BaseRepository
import javax.inject.Inject

class ContactInvitationViewModel @Inject constructor(
    val repo: BaseRepository,
    val daoRepo: DaoRepository,
    private val schedulers: SchedulerProvider
) : ViewModel() {
}
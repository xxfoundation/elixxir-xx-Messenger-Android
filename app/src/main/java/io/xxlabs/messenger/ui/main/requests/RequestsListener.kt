package io.xxlabs.messenger.ui.main.requests

import android.view.View
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData

interface RequestsListener {
    fun onResend(v: View, contact: ContactData)
    fun onClickUsername(v: View, contact: ContactData)
    fun onClickAcceptContact(pos: Int, contact: ContactData)
    fun onClickRejectContact(pos: Int, contact: ContactData)
    fun onClickAcceptGroup(pos: Int, group: GroupData)
    fun onClickRejectGroup(pos: Int, group: GroupData)
    fun onRetry(contact: ContactData)
    fun onVerifying(contact: ContactData)
}
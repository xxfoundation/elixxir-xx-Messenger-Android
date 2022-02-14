package io.xxlabs.messenger.ui.main.contacts

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BitmapCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.datatype.SelectionMode
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.support.RandomColor
import io.xxlabs.messenger.support.extensions.capitalizeWords
import io.xxlabs.messenger.support.selection.ItemDetailsLookup
import io.xxlabs.messenger.support.view.BitmapResolver
import io.xxlabs.messenger.support.view.RoundedCornerLayout
import timber.log.Timber

class ContactsViewHolder(
    private val parent: View,
    private val contactUsername: TextView,
    private val contactPhoto: ImageView,
    private val contactPhotoDefault: TextView,
    private val contactPhotoDefaultBg: RoundedCornerLayout,
    private val divider: View,
    private val checkBox: CheckBox
) : RecyclerView.ViewHolder(parent) {
    private var requestStatus: RequestStatus = RequestStatus.SENT
    private var selectionMode: SelectionMode = SelectionMode.CONTACT_ACCESS
    private var id: Long = -1L
    private var bindingsId: ByteArray = byteArrayOf()
    private var isChooseModeEnabled = false

    fun showDivider(show: Boolean) {
        divider.visibility = if (show) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun setState(friendStatus: Int) {
        contactUsername.alpha = if (friendStatus == RequestStatus.ACCEPTED.value
            || friendStatus == RequestStatus.RECEIVED.value
        ) {
            1.0f
        } else {
            0.5f
        }

        contactPhoto.alpha = if (friendStatus == RequestStatus.ACCEPTED.value
            || friendStatus == RequestStatus.RECEIVED.value
        ) {
            1.0f
        } else {
            0.5f
        }
    }

    fun setContactId(id: Long, bindingsId: ByteArray) {
        this.id = id
        this.bindingsId = bindingsId
    }

    fun setContactStatus(status: Int) {
        requestStatus = RequestStatus.from(status)
    }

    fun setPhoto(photo: ByteArray?, initials: String) {
        if (photo == null) {
            setDefaultAvatar(initials)
        } else {
            val bitmap = BitmapResolver.getBitmap(photo)
            Timber.v("[CONTACT PROFILE] Decoded Photo: ${(bitmap?.rowBytes ?: 1) * (bitmap?.height ?: 1)}")
            bitmap?.let {
                Timber.v(
                    "[CONTACT PROFILE] Decoded Photo (allocation): ${
                        BitmapCompat.getAllocationByteCount(
                            bitmap
                        )
                    }"
                )
            }
            hideDefaultAvatar()
            clearCurrentPhoto()

            Glide.with(contactPhoto.context)
                .asBitmap()
                .load(bitmap)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .centerCrop()
                .into(contactPhoto)

            contactPhoto.visibility = View.VISIBLE
        }
    }

    private fun clearCurrentPhoto() {
        Glide.with(contactPhoto.context).clear(contactPhoto)
        contactPhoto.setImageDrawable(null)
    }

    private fun hideDefaultAvatar() {
        contactPhotoDefault.visibility = View.INVISIBLE
    }

    private fun setDefaultAvatar(initials: String) {
        contactPhotoDefault.text = initials
        contactPhotoDefault.visibility = View.VISIBLE
        contactPhoto.visibility = View.INVISIBLE

        val colorPair = RandomColor.getRandomColor(bindingsId)
        contactPhotoDefaultBg.background.setTint(colorPair.first)
        if (colorPair.second) {
            contactPhotoDefault.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.neutral_active
                )
            )
        } else {
            contactPhotoDefault.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.white
                )
            )
        }
    }

    fun setContactUsernameText(text: String) {
        contactUsername.text = text.capitalizeWords()
    }

    fun setOnClick(selectionMode: SelectionMode) {
        this.selectionMode = selectionMode
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override val position: Int = bindingAdapterPosition
            override val selectionKey: Long = id
        }

    fun isSelected(isChecked: Boolean = false) {
        checkBox.isChecked = isChecked
    }

    fun setSetChooseMode(chooseMode: Boolean) {
        isChooseModeEnabled = chooseMode
        if (chooseMode) {
            checkBox.visibility = View.VISIBLE
        } else {
            checkBox.visibility = View.GONE
        }
    }

    fun setChooseModeListener(contact: ContactData, listener: GroupsSelectionListener) {
        val bundle = bundleOf("contact_id" to bindingsId)

        if (isChooseModeEnabled) {
            itemView.setOnClickListener {
                val isChecked = !checkBox.isChecked
                checkBox.isChecked = isChecked
                setOnCheckedListener(isChecked, listener, contact)
            }
        } else {
            itemView.setOnClickListener {
                when (selectionMode) {
                    SelectionMode.CONTACT_ACCESS -> {
                        navigateFromContactsSelection(bundle)
                    }
                    SelectionMode.PROFILE_ACCESS -> {
                        navigateFromContacts(bundle)
                    }
                    SelectionMode.CHAT_ACCESS -> {
                        navigateFromChat(bundle)
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun setChooseModeGroups(group: GroupData) {
        val bundle = bundleOf("group_id" to group.groupId)

        if (isChooseModeEnabled) {
            itemView.setOnClickListener {}
        } else {
            itemView.setOnClickListener {
                navigateFromGroupChat(bundle)
            }
        }
    }

    private fun setOnCheckedListener(
        isChecked: Boolean,
        listener: GroupsSelectionListener,
        contact: ContactData
    ) {
        if (isChecked) {
            listener.onAddMember(contact)
        } else {
            listener.onRemoveMember(this, contact, bindingAdapterPosition)
        }
    }

    fun deselectContact() {
        checkBox.isChecked = false
    }

    private fun navigateFromChat(bundle: Bundle) {
        if (requestStatus == RequestStatus.RECEIVED) {
            Navigation.findNavController(parent)
                .navigate(R.id.action_contacts_to_invitation, bundle)
        } else {
            Navigation.findNavController(parent)
                .navigate(R.id.action_global_chat, bundle)
        }
    }

    private fun navigateFromGroupChat(bundle: Bundle) {
        Navigation.findNavController(parent)
            .navigate(R.id.action_global_groups_chat, bundle)
    }

    private fun navigateFromContacts(bundle: Bundle) {
        if (requestStatus == RequestStatus.RECEIVED) {
            Navigation.findNavController(parent)
                .navigate(R.id.action_contacts_to_invitation, bundle)
        } else {
            Navigation.findNavController(parent)
                .navigate(R.id.action_contacts_to_profile, bundle)
        }
    }

    private fun navigateFromContactsSelection(bundle: Bundle) {
        when (requestStatus) {
            RequestStatus.RECEIVED -> {
                Navigation.findNavController(parent)
                    .navigate(R.id.action_contacts_selection_to_invitation, bundle)
            }
            RequestStatus.ACCEPTED -> {
                Navigation.findNavController(parent)
                    .navigate(R.id.action_contacts_selection_to_chat, bundle)
            }
            else -> {
                Navigation.findNavController(parent)
                    .navigate(R.id.action_contacts_selection_to_profile, bundle)
            }
        }
    }

    companion object {
        /**
         * @param parent
         * @return ConversationsViewHolder
         */
        fun newInstance(parent: View): ContactsViewHolder {
            val name = parent.findViewById(R.id.contactName) as TextView
            val contactPhoto = parent.findViewById(R.id.contactPhoto) as ImageView
            val contactPhotoDefault = parent.findViewById(R.id.contactPhotoDefault) as TextView
            val contactPhotoDefaultBg =
                parent.findViewById(R.id.contactPhotoHolder) as RoundedCornerLayout
            val divider = parent.findViewById(R.id.contactDivider) as View
            val checkBox = parent.findViewById(R.id.contactCheckbox) as CheckBox

            return ContactsViewHolder(
                parent,
                name,
                contactPhoto,
                contactPhotoDefault,
                contactPhotoDefaultBg,
                divider,
                checkBox
            )
        }
    }
}
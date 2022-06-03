package io.xxlabs.messenger.ui.main.contacts.deprecated

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.RandomColor
import io.xxlabs.messenger.support.extensions.capitalizeWords
import io.xxlabs.messenger.support.selection.ItemDetailsLookup
import io.xxlabs.messenger.support.view.BitmapResolver
import io.xxlabs.messenger.support.view.RoundedCornerLayout
import io.xxlabs.messenger.support.view.SquaredCornerLayout

class GroupMemberViewHolder(
    parent: View,
    private val contactUsername: TextView,
    private val contactPhoto: ImageView,
    private val contactPhotoDefault: TextView,
    private val contactPhotoDefaultBg: SquaredCornerLayout,
    internal val contactCancelBtn: RoundedCornerLayout
) : RecyclerView.ViewHolder(parent) {
    private var id: Long = -1L
    private var contactId: ByteArray = byteArrayOf()

    fun setContactId(id: Long, contactId: ByteArray) {
        this.id = id
        this.contactId = contactId
    }

    fun setPhoto(photo: ByteArray?, initials: String) {
        if (photo == null) {
            setDefaultAvatar(initials)
        } else {
            val bitmap = BitmapResolver.getBitmap(photo)
            hideDefaultAvatar()
            clearCurrentPhoto()

            Glide.with(contactPhoto.context)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .load(bitmap)
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

        val colorPair = RandomColor.getRandomColor(contactId)
        contactPhotoDefaultBg.background.setTint(colorPair.first)
        if (colorPair.second) {
            contactPhotoDefault.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.neutral_active
                )
            )
        }
    }

    fun setContactUsernameText(text: String) {
        contactUsername.text = text.capitalizeWords()
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override val position: Int = bindingAdapterPosition
            override val selectionKey: Long = id
        }

    companion object {

        /**
         * @param parent
         * @return ConversationsViewHolder
         */
        fun newInstance(parent: View): GroupMemberViewHolder {
            val name = parent.findViewById(R.id.contactMemberName) as TextView
            val contactPhoto = parent.findViewById(R.id.contactMemberPhoto) as ImageView
            val contactPhotoDefault =
                parent.findViewById(R.id.contactMemberPhotoDefault) as TextView
            val contactPhotoDefaultBg =
                parent.findViewById(R.id.contactMemberPhotoHolder) as SquaredCornerLayout
            val contactCancelBtn = parent.findViewById(R.id.contactMemberBtnCancel) as RoundedCornerLayout

            return GroupMemberViewHolder(
                parent,
                name,
                contactPhoto,
                contactPhotoDefault,
                contactPhotoDefaultBg,
                contactCancelBtn
            )
        }
    }
}
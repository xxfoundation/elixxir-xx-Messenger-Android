package io.xxlabs.messenger.ui.main.chats

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.BackgroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.SelectionMode
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.support.RandomColor
import io.xxlabs.messenger.support.selection.ItemDetailsLookup
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.support.view.BitmapResolver
import io.xxlabs.messenger.support.view.SquaredCornerLayout
import timber.log.Timber
import java.util.regex.Pattern

class ChatsViewHolder(
    private val parent: View,
    private val contactUsername: TextView,
    private val conversationMsgPreview: TextView,
    private val conversationTimeStamp: TextView,
    private val chatUnreadCountLayout: RelativeLayout,
    private val contactPhoto: ImageView,
    private val contactPhotoDefault: TextView,
    private val defaultPhotoBg: SquaredCornerLayout,
    private val checkbox: CheckBox
) : RecyclerView.ViewHolder(parent), View.OnClickListener {
    var previousPhoto: Bitmap? = null
    var currentObj: Any? = null
    var bindingsId: ByteArray = byteArrayOf()
    var rowId: Long = -1
    private var selectionMode = SelectionMode.CHAT_ACCESS

    init {
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (selectionMode == SelectionMode.CHAT_ACCESS) {
            if (currentObj is GroupData) {
                val group = currentObj as GroupData?
                Timber.v("recipient ID BEING USED TO OPEN ${group?.groupId ?: bindingsId}")
                val bundle = bundleOf(
                    "group_id" to (group?.groupId ?: bindingsId)
                )

                Navigation.findNavController(parent.context as Activity, R.id.mainNavHost)
                    .navigate(R.id.action_global_groups_chat, bundle)
            } else if (currentObj is ContactData) {
                val contact = currentObj as ContactData?
                Timber.v("recipient ID BEING USED TO OPEN ${contact?.userId ?: bindingsId}")
                val bundle = bundleOf(
                    "contact_id" to (contact?.userId ?: bindingsId)
                )

                Navigation.findNavController(parent.context as Activity, R.id.mainNavHost)
                    .navigate(R.id.action_global_chat, bundle)
            }
        }
    }

    /**
     * @param text
     */
    fun setDateText(text: String) {
        try {
            conversationTimeStamp.text = text
        } catch (err: Exception) {
            err.localizedMessage
            conversationTimeStamp.visibility = View.GONE
        }
    }

    /**
     * @param text
     */
    fun setPreview(text: String) {
        conversationMsgPreview.text = text
    }

    fun setPreviewHighlight(text: String, lastSearch: String) {
        val stringBuilder = SpannableStringBuilder(text)
        val p = Pattern.compile(lastSearch, Pattern.CASE_INSENSITIVE or Pattern.LITERAL)
        val m = p.matcher(text)
        while (m.find()) {
            stringBuilder.setSpan(
                BackgroundColorSpan(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.yellowLightTheme
                    )
                ),
                m.start(),
                m.end(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
        }

        conversationMsgPreview.text = stringBuilder
    }

    fun setBold(isNotViewed: Boolean) {
        if (isNotViewed) {
            conversationTimeStamp.setTypeface(null, Typeface.BOLD)
            contactUsername.setTypeface(null, Typeface.BOLD)
            conversationMsgPreview.setTypeface(null, Typeface.BOLD)
        } else {
            contactUsername.setTypeface(null, Typeface.NORMAL)
            conversationTimeStamp.setTypeface(null, Typeface.NORMAL)
            conversationMsgPreview.setTypeface(null, Typeface.NORMAL)
        }
    }

    fun setContact(newContact: ContactData) {
        currentObj = newContact
        rowId = newContact.id
        bindingsId = newContact.userId
        contactUsername.text = newContact.displayName
        setPhoto(newContact)
    }

    fun setGroup(newGroup: GroupData) {
        currentObj = newGroup
        rowId = newGroup.id + 10000
        bindingsId = newGroup.groupId
        contactUsername.text = newGroup.name
        setDefaultAvatar(newGroup.name.substring(0, 2).uppercase())
    }

    private fun setPhoto(contact: ContactData) {
        if (contact.photo == null) {
            setDefaultAvatar(contact.initials)
        } else {
            val bitmap = BitmapResolver.getBitmap(contact.photo!!) ?: return

            if (bitmap.sameAs(previousPhoto)) {
                return
            }

            val resizedBitmap = BitmapResolver.getResizedBitmap(bitmap, Utils.dpToPx(42))
            clearCurrentPhoto()
            hideDefaultAvatar()

            Glide.with(itemView.context)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .load(resizedBitmap)
                .listener(object : RequestListener<Bitmap> {
                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        previousPhoto = bitmap
                        contactPhoto.setImageBitmap(resource)
                        return true
                    }

                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        setDefaultAvatar(contact.initials)
                        return false
                    }
                }).submit()
        }
    }

    private fun clearCurrentPhoto() {
        Glide.with(itemView.context).clear(contactPhoto)
        contactPhoto.setImageBitmap(null)
    }

    fun hideDefaultAvatar() {
        contactPhotoDefault.visibility = View.INVISIBLE
    }

    private fun setDefaultAvatar(initials: String) {
        contactPhoto.visibility = View.GONE
        contactPhotoDefault.visibility = View.VISIBLE
        contactPhotoDefault.text = initials

        val colorPair = RandomColor.getRandomColor(bindingsId)
        defaultPhotoBg.background.setTint(colorPair.first)
        if (colorPair.second) {
            contactPhotoDefault.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.neutral_active
                )
            )
        }
    }

    fun setUnreadCount(unreadCount: Int, isLastSenderContact: Boolean) {
        if (isLastSenderContact && unreadCount > 0) {
            chatUnreadCountLayout.visibility = View.VISIBLE
            chatUnreadCountLayout.findViewById<TextView>(R.id.chatUnreadCount).text =
                unreadCount.toString()
        } else {
            chatUnreadCountLayout.visibility = View.GONE
        }
    }

    fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        object : ItemDetailsLookup.ItemDetails<Long>() {
            override val position = bindingAdapterPosition
            override val selectionKey: Long = rowId
            override fun inSelectionHotspot(e: MotionEvent): Boolean {
                return selectionMode == SelectionMode.CHAT_SELECTION
            }
        }

    fun isSelected(isChecked: Boolean = false) {
        checkbox.isChecked = isChecked
    }

    fun setSelectionOptions(selectionMode: SelectionMode) {
        if (selectionMode == SelectionMode.CHAT_SELECTION) {
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.neutral_off_white
                )
            )
            checkbox.visibility = View.VISIBLE
        } else {
            itemView.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    R.color.transparent
                )
            )
            checkbox.visibility = View.GONE
        }
        this.selectionMode = selectionMode
    }

    companion object {
        /**
         * @param parent
         * @return ConversationsViewHolder
         */
        fun newInstance(parent: View): ChatsViewHolder {
            val nameTextView = parent.findViewById(R.id.chatUsername) as TextView
            val companyTextView = parent.findViewById(R.id.chatMsgPreview) as TextView
            val dateTextView = parent.findViewById(R.id.chatTimestamp) as TextView
            val chatUnreadCountLayout =
                parent.findViewById(R.id.chatUnreadCountLayout) as RelativeLayout
            val avatarPhoto = parent.findViewById(R.id.chatContactPhoto) as ImageView
            val defaultPhoto = parent.findViewById(R.id.chatContactPhotoDefault) as TextView
            val defaultPhotoBg = parent.findViewById(R.id.chatContactPhotoBg) as SquaredCornerLayout
            val checkbox = parent.findViewById(R.id.chatCheckbox) as CheckBox

            return ChatsViewHolder(
                parent,
                nameTextView,
                companyTextView,
                dateTextView,
                chatUnreadCountLayout,
                avatarPhoto,
                defaultPhoto,
                defaultPhotoBg,
                checkbox
            )
        }
    }
}
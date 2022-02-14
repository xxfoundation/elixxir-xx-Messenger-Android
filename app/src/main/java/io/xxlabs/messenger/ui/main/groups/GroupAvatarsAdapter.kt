package io.xxlabs.messenger.ui.main.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.AvatarWrapper
import io.xxlabs.messenger.support.RandomColor
import io.xxlabs.messenger.support.view.BitmapResolver
import io.xxlabs.messenger.support.view.RoundedCornerLayout


class GroupAvatarsAdapter : RecyclerView.Adapter<GroupAvatarsAdapter.AvatarViewHolder>() {
    var avatarList: List<AvatarWrapper> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvatarViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.list_item_group_chat_avatar, parent, false)
        return AvatarViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvatarViewHolder, position: Int) {
        val item = avatarList[position]
        if (item.photo == null) {
            setDefaultAvatar(holder, item)
        } else {
            holder.photoDefault.visibility = View.GONE
            holder.photo.visibility = View.VISIBLE
            holder.photoDefault.contentDescription = "chat.avatars.item.$position.photo"
            holder.photo.contentDescription = "chat.avatars.item.$position.photo"

            val bitmap = BitmapResolver.getBitmap(item.photo!!)
            Glide.with(holder.photo.context).clear(holder.photo)
            holder.photo.setImageDrawable(null)

            Glide.with(holder.photo.context)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .load(bitmap)
                .into(holder.photo)
        }

        holder.itemView.contentDescription = "chat.avatars.item.$position"
    }

    private fun setDefaultAvatar(
        holder: AvatarViewHolder,
        item: AvatarWrapper
    ) {
        val context = holder.itemView.context
        holder.photoDefault.text =
            if (item.username.isNotBlank()) item.username.substring(0, 2)
            else ""

        holder.photoDefault.visibility = View.VISIBLE
        holder.photo.visibility = View.GONE

        val byteIdentifier =
            if (!item.userId.contentEquals(byteArrayOf())) item.userId
            else item.username.encodeToByteArray()

        val colorPair = RandomColor.getRandomColor(byteIdentifier)
        holder.photoHolder.background.setTint(colorPair.first)
        if (colorPair.second) {
            holder.photoDefault.setTextColor(
                ContextCompat.getColor(context, R.color.neutral_active)
            )
        } else {
            holder.photoDefault.setTextColor(
                ContextCompat.getColor(context, R.color.white)
            )
        }
    }

    fun updateAvatars(avatars: List<AvatarWrapper>) {
        avatarList = avatars
        notifyDataSetChanged()
    }

    override fun getItemCount() = avatarList.size

    class AvatarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoDefault: TextView = itemView.findViewById(R.id.groupAvatarPhotoDefault)
        val photo: ImageView = itemView.findViewById(R.id.groupAvatarPhoto)
        val photoHolder: RoundedCornerLayout = itemView.findViewById(R.id.groupAvatarHolder)
    }
}
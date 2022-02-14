package io.xxlabs.messenger.ui.main.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
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

class GroupMembersAdapter : RecyclerView.Adapter<GroupMembersAdapter.MemberViewHolder>(),
    Filterable {
    var avatarList: List<AvatarWrapper> = listOf()
    var avatarListFiltered: List<AvatarWrapper> = listOf()

    private lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.list_item_group_chat_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val item = avatarList[position]
        holder.username.text = item.username
        if (item.photo == null) {
            setDefaultAvatar(holder, item)
        } else {
            holder.photoDefault.visibility = View.GONE
            holder.photo.visibility = View.VISIBLE

            val bitmap = BitmapResolver.getBitmap(item.photo!!)
            Glide.with(holder.photo.context).clear(holder.photo)
            holder.photo.setImageDrawable(null)

            Glide.with(holder.photo.context)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .load(bitmap)
                .into(holder.photo)
        }
    }

    private fun setDefaultAvatar(
        holder: MemberViewHolder,
        item: AvatarWrapper
    ) {
        val context = holder.itemView.context
        holder.photoDefault.text = if (item.username.isNotBlank()) {
            item.username.substring(0, 2)
        } else {
            ""
        }
        holder.photoDefault.visibility = View.VISIBLE
        holder.photo.visibility = View.GONE

        val colorPair = RandomColor.getRandomColor(item.userId)
        holder.photoHolder.background.setTint(colorPair.first)
        if (colorPair.second) {
            holder.photoDefault.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.neutral_active
                )
            )
        } else {
            holder.photoDefault.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
        }
    }

    fun updateAvatars(avatars: List<AvatarWrapper>) {
        avatarList = listOf()
        avatarListFiltered = listOf()
        avatarList = avatars
        avatarListFiltered = avatarList
        notifyItemRangeChanged(0, itemCount)
    }

    override fun getItemCount(): Int {
        return avatarListFiltered.size
    }

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoDefault: TextView = itemView.findViewById(R.id.groupMemberAvatarPhotoDefault)
        val photo: ImageView = itemView.findViewById(R.id.groupMemberAvatarPhoto)
        val photoHolder: RoundedCornerLayout = itemView.findViewById(R.id.groupMemberAvatarHolder)
        val username: TextView = itemView.findViewById(R.id.groupMemberUsername)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val searchString = charSequence.toString()
                val filteredResults: MutableList<AvatarWrapper>
                if (searchString.isEmpty()) {
                    filteredResults = avatarList.toMutableList()
                } else {
                    val filteredList: MutableList<AvatarWrapper> = mutableListOf()
                    for (row in avatarList) {
                        val username = row.username

                        if (username.contains(searchString, true)) {
                            filteredList.add(row)
                        }
                    }

                    filteredList.sortWith { first, second ->
                        first.username.compareTo(second.username, true)
                    }

                    filteredResults = filteredList
                }

                val filterResults = FilterResults()
                filterResults.values = filteredResults
                return filterResults
            }

            override fun publishResults(
                charSequence: CharSequence,
                filterResults: FilterResults
            ) {
                avatarListFiltered = filterResults.values as List<AvatarWrapper>
                notifyItemRangeChanged(0, itemCount)
            }
        }
    }

    fun filter(text: String) {
        return filter.filter(text)
    }
}
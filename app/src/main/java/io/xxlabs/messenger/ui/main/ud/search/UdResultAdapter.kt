package io.xxlabs.messenger.ui.main.ud.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.bindings.wrapper.contact.ContactWrapperBase
import io.xxlabs.messenger.support.RandomColor
import io.xxlabs.messenger.support.view.SquaredCornerLayout

class UdResultAdapter(private val udSelectionListener: UdSelectionListener) :
    RecyclerView.Adapter<UdResultAdapter.UserDiscoveryViewHolder>() {
    private var resultsList: MutableList<ContactWrapperBase> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserDiscoveryViewHolder {
        val context = parent.context
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.list_item_contact_search_result, parent, false)
        return UserDiscoveryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return resultsList.size
    }

    override fun onBindViewHolder(holder: UserDiscoveryViewHolder, position: Int) {
        val item = resultsList[position]

        val username = item.getUsernameFact()
        val name = item.getDisplayName()
        val email = item.getEmailFact()
        val phone = item.getFormattedPhone()

        holder.username.text = if (!phone.isNullOrBlank()) {
            phone
        } else if (!email.isNullOrBlank()) {
            email
        } else {
            username
        }

        holder.name.text = name
        holder.contactPhotoHolderDefault.text = if (!name.isNullOrBlank()) {
            val splitName = name.split(" ")
            if (splitName.size > 1) {
                splitName[0][0].uppercase() + splitName[1][0].uppercase()
            } else {
                name.substring(0, 2).uppercase()
            }
        } else {
            username.substring(0, 2).uppercase() ?: ""
        }

        val colorPair = RandomColor.getRandomColor(item.getId())
        holder.contactPhotoHolderBg.background.setTint(colorPair.first)
        if (colorPair.second) {
            holder.contactPhotoHolderDefault.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.neutral_active
                )
            )
        }

        holder.itemView.setOnClickListener {
            udSelectionListener.onItemSelected(it, item)
        }

        holder.username.contentDescription = "ud.search.result.item.$position.username"
        holder.name.contentDescription = "ud.search.result.item.$position.name"
        holder.contactPhotoHolderBg.contentDescription = "ud.search.result.item.$position.photo.bg"
        holder.contactPhotoHolderDefault.contentDescription = "ud.search.result.item.$position.photo.text"
        holder.contactPhotoHolder.contentDescription = "ud.search.result.item.$position.photo"
    }

    fun setResult(results: ContactWrapperBase) {
        clearResult(false)
        resultsList.addAll(listOf(results))
        notifyDataSetChanged()
    }

    fun clearResult(notify: Boolean = true) {
        resultsList.clear()

        if (notify) {
            notifyDataSetChanged()
        }
    }

    class UserDiscoveryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val username: TextView = view.findViewById(R.id.udSearchResultUsernameText)
        val name: TextView = view.findViewById(R.id.udSearchResultNameText)
        val contactPhotoHolderBg: SquaredCornerLayout =
            view.findViewById(R.id.udSearchResultPhotoBg)
        val contactPhotoHolder: AppCompatImageView = view.findViewById(R.id.udSearchResultPhoto)
        val contactPhotoHolderDefault: TextView = view.findViewById(R.id.udSearchResultPhotoText)
    }
}
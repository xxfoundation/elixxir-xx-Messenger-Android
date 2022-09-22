package io.xxlabs.messenger.ui.main.chat.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import java.util.*

class HeaderViewHolder(
    parent: View,
    private val title: TextView
) : RecyclerView.ViewHolder(parent) {
    fun bindTo(
        newTitle: String
    ) {
        title.text = if (newTitle.isNotEmpty()) {
            newTitle[0].toString().uppercase(Locale.getDefault())
        } else {
            "Other"
        }
    }

    companion object {
        /**
         * @param parent
         * @return MessageViewHolder
         */
        fun newInstance(parent: View): HeaderViewHolder {
            val title: TextView = parent.findViewById(R.id.contactHeaderText)

            return HeaderViewHolder(
                parent,
                title
            )
        }
    }
}
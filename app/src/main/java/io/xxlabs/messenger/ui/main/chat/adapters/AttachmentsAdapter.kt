package io.xxlabs.messenger.ui.main.chat.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.xxlabs.messenger.R

class AttachmentsAdapter(
    private val listener: AttachmentListener
) : ListAdapter<Uri, AttachmentsAdapter.AttachmentHolder>(AttachmentDiffCallBack()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentHolder {
        return AttachmentHolder.from(parent)
    }

    override fun onBindViewHolder(holder: AttachmentHolder, position: Int) {
        val uri = getItem(position)
        holder.bind(uri, listener)
    }

    class AttachmentHolder(val parent: View): RecyclerView.ViewHolder(parent) {
        private val attachmentPreview: ImageView = parent.findViewById(R.id.attachmentPreview)
        private val removeButton: ImageView = parent.findViewById(R.id.removeAttachmentButton)

        fun bind(uri: Uri, listener: AttachmentListener) {
            Glide.with(itemView.context)
                .load(uri)
                .into(attachmentPreview)

            attachmentPreview.setOnClickListener { listener.onAttachmentClicked(uri) }
            removeButton.setOnClickListener { listener.onRemoveClicked(uri) }
        }

        companion object {
            fun from(parent: ViewGroup): AttachmentHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(
                    R.layout.list_item_attachment, parent, false
                )
                return AttachmentHolder(view)
            }
        }
    }
}

class AttachmentDiffCallBack : DiffUtil.ItemCallback<Uri>() {
    override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
        return oldItem == newItem
    }
}

interface AttachmentListener {
    fun onRemoveClicked(uri: Uri)
    fun onAttachmentClicked(uri: Uri)
}
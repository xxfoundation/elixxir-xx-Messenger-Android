package io.xxlabs.messenger.ui.main.chat.adapters

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.support.util.Utils
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

abstract class ChatMessagesAdapter<T : ChatMessage, VH : RecyclerView.ViewHolder>
    (diffCallback: DiffUtil.ItemCallback<T>) : PagedListAdapter<T, VH>(diffCallback) {

    /**
     * Controls visibility of MessageViewHolder checkboxes
     */
    var selectionMode = false

    fun getItemPositionFromUniqueId(uniqueId: ByteArray): Int {
        return currentList?.indexOf(currentList?.find { msg ->
            msg?.uniqueId.contentEquals(uniqueId)
        }) ?: RecyclerView.NO_POSITION
    }

    fun getItemPositionFromId(tmpId: Long): Int {
        return currentList?.indexOf(currentList?.find { msg ->
            msg?.id == tmpId
        }) ?: RecyclerView.NO_POSITION
    }

    protected fun showDate(
        isSameDay: Boolean,
        date: Calendar
    ): String? {
        val currDate = Calendar.getInstance()
        return if (isSameDay) {
            null
        } else {
            val df: DateFormat = if (currDate.get(Calendar.YEAR) == date.get(Calendar.YEAR)) {
                SimpleDateFormat("EEE, dd/MM", Locale.ENGLISH)
            } else {
                SimpleDateFormat("EEE, dd/MM/yy", Locale.ENGLISH)
            }

            df.format(date.time)
        }
    }

    protected fun showHeader(position: Int): String? {
        val msg = currentList?.get(position)
        val currentTimeStamp = msg?.timestamp ?: return null
        val previousTimeStamp = if (position + 1 >= itemCount ) {
            0
        } else {
            currentList?.get(position + 1)?.timestamp ?: 0
        }

        val currentTime = Calendar.getInstance()
        val previousTime = Calendar.getInstance()
        currentTime.time = Date(currentTimeStamp)
        previousTime.time = Date(previousTimeStamp)

        val isSameDay = Utils.isSameDay(currentTime, previousTime)
        return showDate(isSameDay, currentTime)
    }

    protected fun getMessageId(position: Int): Long {
        return try {
            getItem(position)!!.id
        } catch (e: Exception) {
            RecyclerView.NO_ID
        }
    }

    override fun getItemCount(): Int = currentList?.size ?: 0
}

abstract class ChatMessageDiffCallback<T: ChatMessage>: DiffUtil.ItemCallback<T>()
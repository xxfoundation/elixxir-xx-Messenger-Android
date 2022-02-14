package io.xxlabs.messenger.support.selection

import androidx.recyclerview.widget.RecyclerView

internal class LongKeyProvider(val recyclerView: RecyclerView): BaseKeyProvider<Long> {
    override fun getKey(position: Int): Long {
        return recyclerView.adapter?.getItemId(position)
            ?: throw IllegalStateException("RecyclerView adapter is not set!")
    }

    override fun getPosition(key: Long): Int {
        val viewHolder = recyclerView.findViewHolderForItemId(key)
        return viewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }
}
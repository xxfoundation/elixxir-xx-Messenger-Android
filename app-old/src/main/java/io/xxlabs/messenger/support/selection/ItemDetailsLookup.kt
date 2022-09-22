package io.xxlabs.messenger.support.selection

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

abstract class ItemDetailsLookup<K> {
    /**
     * @return true if there is an item at the event coordinates.
     */
    fun overItem(e: MotionEvent): Boolean {
        return getItemPosition(e) != RecyclerView.NO_POSITION
    }

    /**
     * @return true if there is an item w/ a stable ID at the event coordinates.
     */
    fun overItemWithSelectionKey(e: MotionEvent): Boolean {
        return overItem(e) && hasSelectionKey(getItemDetails(e))
    }

    /**
     * @return true if the event coordinates are in an area of the item
     * that can result in dragging the item. List items frequently have a white
     * area that is not draggable allowing band selection to be initiated
     * in that area.
     */
    fun inItemDragRegion(e: MotionEvent): Boolean {
        return overItem(e) && getItemDetails(e)!!.inDragRegion(e)
    }

    /**
     * @return true if the event coordinates are in a "selection hot spot"
     * region of an item. Contact in these regions result in immediate
     * selection, even when there is no existing selection.
     */
    fun inItemSelectRegion(e: MotionEvent): Boolean {
        return overItem(e) && getItemDetails(e)!!.inSelectionHotspot(e)
    }

    /**
     * @return the adapter position of the item at the event coordinates.
     */
    fun getItemPosition(e: MotionEvent): Int {
        val item: ItemDetails<*>? = getItemDetails(e)
        return item?.position ?: RecyclerView.NO_POSITION
    }

    /**
     * @return the ItemDetails for the item under the event, or null.
     */
    abstract fun getItemDetails(e: MotionEvent): ItemDetails<K>?
    abstract class ItemDetails<K> {
        /**
         * Returns the adapter position of the item. See
         * [ViewHolder.getAdapterPosition][RecyclerView.ViewHolder.getAdapterPosition]
         *
         * @return the position of an item.
         */
        abstract val position: Int

        /**
         * @return true if the item has a selection key.
         */
        fun hasSelectionKey(): Boolean {
            return selectionKey != null
        }

        /**
         * @return the selection key of an item.
         */
        abstract val selectionKey: K?

        /**
         * Areas are often included in a view that behave similar to checkboxes, such
         * as the icon to the left of an email message. "selection
         * hotspot" provides a mechanism to identify such regions, and for the
         * library to directly translate taps in these regions into a change
         * in selection state.
         *
         * @return true if the event is in an area of the item that should be
         * directly interpreted as a user wishing to select the item. This
         * is useful for checkboxes and other UI affordances focused on enabling
         * selection.
         */
        open fun inSelectionHotspot(e: MotionEvent): Boolean {
            return false
        }

        /**
         * "Item Drag Region" identifies areas of an item that are not considered when the library
         * evaluates whether or not to initiate band-selection for mouse input. The drag region
         * will usually correspond to an area of an item that represents user visible content.
         * Mouse driven band selection operations are only ever initiated in non-drag-regions.
         * This is a consideration as many layouts may not include empty space between
         * RecyclerView items where band selection can be initiated.
         *
         *
         *
         * For example. You may present a single column list of contact names in a
         * RecyclerView instance in which the individual view items expand to fill all
         * available space.
         * But within the expanded view item after the contact name there may be empty space that a
         * user would reasonably expect to initiate band selection. When a MotionEvent occurs
         * in such an area, you should return identify this as NOT in a drag region.
         *
         *
         *
         * Further more, within a drag region, a mouse click and drag will immediately
         * initiate drag and drop (if supported by your configuration).
         *
         * @return true if the item is in an area of the item that can result in dragging
         * the item. List items frequently have a white area that is not draggable allowing
         * mouse driven band selection to be initiated in that area.
         */
        open fun inDragRegion(e: MotionEvent): Boolean {
            return false
        }

        override fun equals(other: Any?): Boolean {
            return (other is ItemDetails<*>
                    && isEqualTo(other))
        }

        private fun isEqualTo(other: ItemDetails<*>): Boolean {
            val key = selectionKey
            val sameKeys: Boolean
            sameKeys = if (key == null) {
                other.selectionKey == null
            } else {
                key == other.selectionKey
            }
            return sameKeys && position == other.position
        }

        override fun hashCode(): Int {
            return position ushr 8
        }
    }

    companion object {
        private fun hasSelectionKey(item: ItemDetails<*>?): Boolean {
            return item?.selectionKey != null
        }

        private fun hasPosition(item: ItemDetails<*>?): Boolean {
            return item != null && item.position != RecyclerView.NO_POSITION
        }
    }
}
package io.xxlabs.messenger.support.selection

import android.view.View

interface TrackerClickListener {
    fun onClick(view: View, position: Int)
    fun onLongClick(view: View, position: Int)
}
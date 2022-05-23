package io.xxlabs.messenger.requests.ui.details.group.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Displays members of a group.
 */
class MembersAdapter(
//    private val members: List<MemberItem>
) : RecyclerView.Adapter<MemberViewHolder>() {

    private var members = listOf<MemberItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder =
        MemberViewHolder.create(parent)

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.onBind(members[position])
    }

    override fun getItemCount(): Int = members.count()

    fun showMembers(membersList: List<MemberItem>) {
        members = membersList
        notifyDataSetChanged()
    }
}
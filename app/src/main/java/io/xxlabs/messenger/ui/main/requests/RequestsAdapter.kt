package io.xxlabs.messenger.ui.main.requests

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.RequestStatus
import io.xxlabs.messenger.data.room.model.ContactData
import io.xxlabs.messenger.data.room.model.GroupData
import io.xxlabs.messenger.support.RandomColor
import io.xxlabs.messenger.support.appContext
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.support.view.SquaredCornerLayout
import java.util.*

class RequestsAdapter(private val requestsListener: RequestsListener) :
    ListAdapter<Any, RequestsAdapter.RequestViewHolder>(RequestDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val context = parent.context
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.list_item_requests, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val item = currentList[position]
        applyTextStyle(holder)

        if (item is ContactData) {
            holder.username.text = item.displayName
            bindPhoto(holder, item)
            bindStatus(holder, item)
            bindTime(holder, item.createdAt)
        } else if (item is GroupData) {
            holder.username.text = item.name
            bindStatusGroup(holder, item)
        }

        holder.username.contentDescription = "requests.item.$position.username"
        holder.contactPhotoBg.contentDescription = "requests.item.$position.photo.bg"
        holder.contactPhoto.contentDescription = "requests.item.$position.photo"
        holder.contactPhotoText.contentDescription = "requests.item.$position.photo.text"
        holder.resend.contentDescription = "requests.item.$position.btn.resend"
        holder.acceptBtn.contentDescription = "requests.item.$position.btn.accept"
        holder.rejectBtn.contentDescription = "requests.item.$position.btn.reject"
    }

    private fun applyTextStyle(holder: RequestViewHolder) {
        holder.reverify.paint.isUnderlineText = true
        holder.verifying.paint.isUnderlineText = true
    }

    private fun bindPhoto(holder: RequestViewHolder, item: ContactData) {
        if (item.photo != null) {
            holder.contactPhoto.visibility = View.VISIBLE
            holder.contactPhotoText.visibility = View.GONE
            val bitmap = BitmapFactory.decodeByteArray(
                item.photo, 0, item.photo!!.size
            )
            Glide.with(holder.itemView.context).asBitmap().load(bitmap).into(holder.contactPhoto)
        } else {
            holder.contactPhoto.visibility = View.GONE
            holder.contactPhotoText.visibility = View.VISIBLE
            holder.contactPhotoText.text = item.initials

            val colorPair = RandomColor.getRandomColor(item.userId)
            holder.contactPhotoBg.background.setTint(colorPair.first)
            if (colorPair.second) {
                holder.contactPhotoText.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.neutral_active
                    )
                )
            } else {
                holder.contactPhotoText.setTextColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.neutral_white
                    )
                )
            }
        }
    }

    private fun bindTime(holder: RequestViewHolder, requestTimestamp: Long) {
        val date = Date(requestTimestamp)
        val cal = Calendar.getInstance()
        cal.time = date

        val time = if (Utils.isToday(cal)) {
            Utils.getTimestampString(date.time)
        } else {
            Utils.calculateGetTimestampString(date.time, "MMM dd, yyyy")
        }

        holder.usernameTime.text = time
        holder.time.text = time
    }

    private fun bindStatus(
        holder: RequestViewHolder,
        item: ContactData
    ) {
        when(RequestStatus.from(item.status)) {
            RequestStatus.CONFIRM_FAIL, RequestStatus.SEND_FAIL -> updateUiForFailure(holder, item)
            RequestStatus.RECEIVED -> updateUiForReceived(holder, item)
            RequestStatus.UNVERIFIED -> updateUiForUnverified(holder, item)
            RequestStatus.VERIFYING -> updateUiForVerifying(holder, item)
            RequestStatus.SENT, RequestStatus.RESET_SENT, RequestStatus.RESET_FAIL -> {
                updateUiForSent(holder, item)
            }
            else -> {
                holder.usernameTime.visibility = View.GONE
                holder.confirmationLayout.visibility = View.GONE
                holder.resend.visibility = View.GONE
                holder.reverify.visibility = View.GONE
                holder.verifying.visibility = View.GONE
            }
        }

        holder.contactPhotoBg.setOnSingleClickListener {
            requestsListener.onClickUsername(it, item)
        }

        holder.contactPhoto.setOnSingleClickListener {
            requestsListener.onClickUsername(it, item)
        }

        holder.contactPhotoText.setOnSingleClickListener {
            requestsListener.onClickUsername(it, item)
        }

        holder.username.setOnSingleClickListener {
            requestsListener.onClickUsername(it, item)
        }
    }

    private fun updateUiForSent(holder: RequestViewHolder, item: ContactData) {
        holder.usernameTime.visibility = View.GONE
        holder.confirmationLayout.visibility = View.GONE
        holder.resend.visibility = View.VISIBLE
        holder.reverify.visibility = View.GONE
        holder.verifying.visibility = View.GONE

        holder.resend.setOnSingleClickListener {
            requestsListener.onResend(it, item)
        }
    }

    private fun updateUiForFailure(holder: RequestViewHolder, item: ContactData) {
        holder.username.setTextColor(ContextCompat.getColor(appContext(), R.color.redDarkTheme))
        holder.resend.visibility = View.VISIBLE
        holder.confirmationLayout.visibility = View.GONE
        holder.reverify.visibility = View.GONE
        holder.verifying.visibility = View.GONE

        holder.resend.setOnSingleClickListener {
            requestsListener.onResend(it, item)
        }
    }

    private fun updateUiForReceived(holder: RequestViewHolder, item: ContactData) {
        resetButtons(holder)
        setDefaultUsernameColor(holder)
        holder.time.visibility = View.GONE
        holder.confirmationLayout.visibility = View.VISIBLE
        holder.resend.visibility = View.GONE
        holder.reverify.visibility = View.GONE
        holder.verifying.visibility = View.GONE

        holder.acceptBtn.setOnSingleClickListener {
            onAcceptOrRejectClicked(holder)
            requestsListener.onClickAcceptContact(
                holder.bindingAdapterPosition,
                item
            )
        }
        holder.rejectBtn.setOnSingleClickListener {
            onAcceptOrRejectClicked(holder)
            holder.rejectBtn.isEnabled = false
            requestsListener.onClickRejectContact(
                holder.bindingAdapterPosition,
                item
            )
        }
    }

    private fun onAcceptOrRejectClicked(holder: RequestViewHolder) {
        disableView(holder.acceptBtn)
        disableView(holder.rejectBtn)
    }

    private fun resetButtons(holder: RequestViewHolder) {
        enableView(holder.acceptBtn)
        enableView(holder.rejectBtn)
    }

    private fun disableView(view: View) {
        view.isEnabled = false
        view.alpha = 0.5f
    }

    private fun enableView(view: View) {
        view.isEnabled = true
        view.alpha = 1.0f
    }

    private fun updateUiForUnverified(holder: RequestViewHolder, item: ContactData) {
        setDefaultUsernameColor(holder)
        holder.resend.visibility = View.GONE
        holder.reverify.visibility = View.VISIBLE
        holder.confirmationLayout.visibility = View.GONE
        holder.verifying.visibility = View.GONE

        holder.reverify.setOnSingleClickListener {
            requestsListener.onRetry(item)
        }
    }

    private fun updateUiForVerifying(holder: RequestViewHolder, item: ContactData) {
        setDefaultUsernameColor(holder)
        holder.resend.visibility = View.GONE
        holder.reverify.visibility = View.GONE
        holder.confirmationLayout.visibility = View.GONE
        holder.verifying.visibility = View.VISIBLE

        holder.verifying.setOnClickListener {
            requestsListener.onVerifying(item)
        }
    }

    private fun setDefaultUsernameColor(holder: RequestViewHolder) {
        holder.username.setTextColor(
            ContextCompat.getColor(
                appContext(),
                R.color.neutral_active
            )
        )
    }

    private fun bindStatusGroup(
        holder: RequestViewHolder,
        item: GroupData
    ) {
        hideContactsUi(holder)
        resetButtons(holder)
        holder.resend.visibility = View.GONE
        holder.usernameTime.visibility = View.GONE
        if (item.status == RequestStatus.ACCEPTED.value) {
            holder.usernameTime.visibility = View.GONE
            holder.confirmationLayout.visibility = View.GONE
            holder.resend.visibility = View.GONE
        } else {
            setDefaultUsernameColor(holder)
            holder.time.visibility = View.GONE
            holder.confirmationLayout.visibility = View.VISIBLE
            holder.resend.visibility = View.GONE

        }
        holder.contactPhotoText.visibility = View.VISIBLE
        holder.contactPhotoText.text = "G"

        holder.acceptBtn.setOnSingleClickListener() {
            requestsListener.onClickAcceptGroup(holder.bindingAdapterPosition, item)
            onAcceptOrRejectClicked(holder)
        }

        holder.rejectBtn.setOnSingleClickListener {
            requestsListener.onClickRejectGroup(holder.bindingAdapterPosition, item)
            onAcceptOrRejectClicked(holder)
        }
    }

    private fun hideContactsUi(holder: RequestViewHolder) {
        holder.reverify.visibility = View.GONE
        holder.verifying.visibility = View.GONE
    }

    class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactPhoto: ImageView = view.findViewById(R.id.itemRequestsContactPhoto)
        val contactPhotoText: TextView = view.findViewById(R.id.itemRequestsContactText)
        val contactPhotoBg: SquaredCornerLayout = view.findViewById(R.id.itemRequestsContactBg)
        val username: TextView = view.findViewById(R.id.itemRequestsUsername)
        val usernameTime: TextView = view.findViewById(R.id.itemRequestsUsernameTime)
        val resend: TextView = view.findViewById(R.id.itemRequestsResend)
        val reverify: TextView = view.findViewById(R.id.itemRequestRetryVerification)
        val verifying: TextView = view.findViewById(R.id.itemRequestVerifying)
        val time: TextView = view.findViewById(R.id.itemRequestsTime)
        val confirmationLayout: LinearLayout =
            view.findViewById(R.id.itemRequestsEndConfirmationLayout)
        val acceptBtn: ImageView = view.findViewById(R.id.itemRequestsAcceptBtn)
        val rejectBtn: ImageView = view.findViewById(R.id.itemRequestsRejectBtn)
    }
}

class RequestDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return (oldItem as? ContactData)?.let {
            if (newItem is ContactData) oldItem.id == newItem.id
            else false
        } ?: (oldItem as? GroupData)?.let {
            if (newItem is GroupData) oldItem.groupId.contentEquals(newItem.groupId)
            else false
        } ?: false
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return (oldItem as? ContactData)?.equals(newItem)
            ?: (oldItem as? GroupData)?.equals(newItem)
            ?: false
    }
}
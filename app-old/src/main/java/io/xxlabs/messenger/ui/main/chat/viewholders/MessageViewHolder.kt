package io.xxlabs.messenger.ui.main.chat.viewholders

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.text.PrecomputedTextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.NetworkPreviewWrapper
import io.xxlabs.messenger.data.data.ReplyWrapper
import io.xxlabs.messenger.data.room.model.ChatMessage
import io.xxlabs.messenger.data.room.model.PrivateMessageData
import io.xxlabs.messenger.support.singleExecutorInstance
import io.xxlabs.messenger.support.util.Utils
import io.xxlabs.messenger.ui.main.chat.ChatMessagesUIController
import io.xxlabs.url.preview.crawler.WebCrawler
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Base presentation class for [PrivateMessageData] in a [RecyclerView.Adapter].
 */
abstract class MessageViewHolder<T: ChatMessage>(
    private val parent: View
) : RecyclerView.ViewHolder(parent) {

    protected val dateHeader: TextView = parent.findViewById(R.id.itemMsgHeader)
    abstract val rootLayout: ViewGroup
    abstract val msgTextView: AppCompatTextView
    abstract val replyLayout: ViewGroup?
    abstract val replyIconMsg: ImageView?
    abstract val urlPreviewLayout: ViewGroup?
    abstract val checkbox: CheckBox
    abstract val timeStampText: TextView
    abstract val replyToUsername: TextView
    abstract val replyTextView: TextView?
    abstract val replyIcon: ImageView?

    protected val blinkAnimation: Animation =
        AnimationUtils.loadAnimation(parent.context, R.anim.blink_animation)
    protected val webCrawler = WebCrawler()
    private var imgViewId = -1

    protected lateinit var chatViewModel: ChatMessagesUIController<T>
    private lateinit var listener: MessageListener<T>

    @CallSuper
    open fun onBind(
        message: T,
        listener: MessageListener<T>,
        header: String?,
        selectionMode: Boolean,
        chatViewModel: ChatMessagesUIController<T>,
    ) {
        this.chatViewModel = chatViewModel
        this.listener = listener
        rootLayout.setOnClickListener { listener.onClick(message) }
        checkbox.setOnClickListener { listener.onClick(message) }
        rootLayout.setOnLongClickListener {
            listener.onLongClick(message)
            true
        }
        message.payloadWrapper.reply?.let { reply ->
            replyLayout?.setOnClickListener { listener.onReplyClicked(reply) }
        }

        setSelectionState(selectionMode, message)
        setShowHeader(header)
        with(message.payloadWrapper) {
            setMsgTextAndTimestamp(text, message.timestamp)
            showReplyDefault(reply)
        }

        updateAccessibility()
    }

    @CallSuper
    protected open fun updateAccessibility() {
        itemView.contentDescription = "chat.msg.list.item.$$absoluteAdapterPosition"
        msgTextView.contentDescription = "chat.item.$$absoluteAdapterPosition.text"
        timeStampText.contentDescription = "chat.item.$$absoluteAdapterPosition.timestamp"
        dateHeader.contentDescription = "chat.item.$$absoluteAdapterPosition.date"
        checkbox.contentDescription = "chat.item.$$absoluteAdapterPosition.checkbox"
        replyToUsername.contentDescription = "chat.item.$$absoluteAdapterPosition.reply.username"
        replyTextView?.contentDescription = "chat.item.$$absoluteAdapterPosition.reply.text"
        replyIcon?.contentDescription = "chat.item.$$absoluteAdapterPosition.reply.icon"
    }

    private fun setSelectionState(
        selectionMode: Boolean,
        message: T
    ) {
        if (selectionMode) {
            checkbox.visibility = View.VISIBLE
            checkbox.isChecked = chatViewModel.isSelected(message)
        } else checkbox.visibility = View.GONE
    }

    private fun setShowHeader(showHeader: String?) {
        if (showHeader.isNullOrBlank()) {
            (dateHeader.parent as ViewGroup).visibility = View.GONE
        } else {
            (dateHeader.parent as ViewGroup).visibility = View.VISIBLE
            dateHeader.text = showHeader
        }
    }

    private fun setMsgTextAndTimestamp(text: String, timeStamp: Long) {
        val msgTimestampText = Utils.calculateGetTimestampString(timeStamp)

        timeStampText.text = msgTimestampText
        val spannable = PrecomputedTextCompat.getTextFuture(
            text,
            TextViewCompat.getTextMetricsParams(msgTextView),
            singleExecutorInstance()
        )
        msgTextView.setTextFuture(spannable)
    }

    private fun showReplyDefault(replyTo: ReplyWrapper?) {
        if (replyTo == null) {
            replyLayout?.visibility = View.GONE
            replyIconMsg?.visibility = View.GONE
        } else {
            replyLayout?.visibility = View.VISIBLE
            bindReplyText(replyTo)
        }
    }

    protected open fun replyUsername(reply: ReplyWrapper): String =
        chatViewModel.chatTitle.value ?: ""

    private fun bindReplyText(reply: ReplyWrapper) {
        replyLayout?.setOnClickListener {
            chatViewModel.onReplyPreviewClicked(reply.uniqueId)
        }

        replyToUsername.text =
            if (reply.senderId.contentEquals(chatViewModel.getUserId())) {
                "You"
            } else {
                replyUsername(reply)
            }

        replyTextView?.text = replyText(reply)
        replyIcon?.visibility = View.GONE
    }

    private fun replyText(reply: ReplyWrapper): String {
        return listener.lookupMessage(reply.uniqueId)?.payloadWrapper?.text
            ?: itemView.context.getString(R.string.chat_reply_deleted_Message)
    }

    private fun bindReplyUrlPreview(imageUrl: String, replyIcon: ImageView?) {
        Glide.with(parent.context)
            .asBitmap()
            .override(100)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onLoadFailed(errorDrawable: Drawable?) {
                    replyIcon?.setImageDrawable(errorDrawable)
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    setImage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    clearImage()
                }

                private fun setImage(resource: Bitmap) {
                    replyIcon?.setImageBitmap(resource)
                }

                private fun clearImage() {
                    replyIcon?.let {
                        Glide.with(parent.context).clear(it)
                    }
                }
            })
    }

    private fun showUrlPreview(
        networkPreviewWrapper: NetworkPreviewWrapper?
    ) {
        if (networkPreviewWrapper == null) {
            urlPreviewLayout?.visibility = View.GONE
        } else {
            bindUrlPreview(networkPreviewWrapper)
        }
    }

    private fun bindUrlPreview(
        urlInfo: NetworkPreviewWrapper
    ) {
        urlPreviewLayout?.visibility = View.VISIBLE
        urlPreviewLayout?.setOnClickListener { Utils.openLink(parent.context, urlInfo.url) }

        val title = urlPreviewLayout?.findViewById<TextView>(R.id.itemMsgSentPreviewTitle)
        val description = urlPreviewLayout?.findViewById<TextView>(R.id.itemMsgSentDescription)
        val imgIconView = urlPreviewLayout?.findViewById<ImageView>(R.id.itemMsgSentPreviewIcon)
        val contentLoading =
            urlPreviewLayout?.findViewById<ContentLoadingProgressBar>(R.id.itemMsgSentPreviewLoading)

        title?.text = urlInfo.title
        description?.text = urlInfo.url

        msgTextView.text.toString()
            .replace("https://", "", true)
            .replace("http://", "", true)
            .replace("ftp://", "", true)


        imgIconView?.let {
            imgViewId = it.id
            contentLoading?.show()
            Glide.with(parent.context).clear(it)
            compositeDisposable.add(
                webCrawler.parseUrlSingle(urlInfo.url) {
                    Timber.d("Loading... URL from msg: $it")
                }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(20, TimeUnit.SECONDS)
                    .subscribe({ content ->
                        contentLoading?.hide()
                        Timber.d("Url parse success: $content")
                        if (!content.getFavicon().isNullOrEmpty()) {
                            handleUrlImage(arrayListOf(content.getFavicon()!!), it)
                        } else {
                            handleUrlImage(content.images, imgIconView)
                        }
                    }, {
                        contentLoading?.hide()
                        handleUrlImage(arrayListOf(), imgIconView)
                    })
            )
        }
    }

    private fun handleUrlImage(
        images: ArrayList<String>,
        imgView: ImageView
    ) {
        val linkIcon = ContextCompat.getDrawable(
            parent.context,
            R.drawable.ic_empty_link_img
        )
        if (imgViewId == imgView.id && images.isNotEmpty()) {
            Glide.with(parent.context)
                .asBitmap()
                .error(linkIcon)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .load(images[0])
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        imgView.setImageDrawable(errorDrawable)
                    }

                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        setImage(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        clearImage()
                    }

                    private fun setImage(resource: Bitmap) {
                        imgView.setImageBitmap(resource)
                    }

                    private fun clearImage() {
                        Glide.with(parent.context).clear(imgView)
                    }
                })
        } else {
            imgView.setImageDrawable(linkIcon)
        }
    }

    companion object {
        private var compositeDisposable = CompositeDisposable()
        fun disposeAll() = compositeDisposable.dispose()
    }
}

/**
 * Receives UI events from an item displayed in a list.
 */
interface MessageListener<T: ChatMessage> {
    fun onClick(message: T)
    fun onLongClick(message: T)
    fun onReplyClicked(reply: ReplyWrapper)
    fun lookupMessage(uniqueId: ByteArray): T?
    fun onShowMixClicked(message: T)
    fun onImageClicked(imageUri: String)
}
package io.xxlabs.messenger.ui.main.chat

import android.graphics.Bitmap
import android.text.InputType
import android.text.format.Formatter
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import io.xxlabs.messenger.R
import io.xxlabs.messenger.backup.model.BackupProgress
import io.xxlabs.messenger.support.extensions.disableWithAlpha
import io.xxlabs.messenger.support.extensions.enable
import io.xxlabs.messenger.support.extensions.incognito
import io.xxlabs.messenger.support.extensions.setTint
import java.text.DateFormat
import java.util.*


@BindingAdapter("android:visibility")
fun View.setVisibility(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("android:enabled")
fun EditText.enabled(enabled: Boolean) {
    isEnabled = enabled
}

@BindingAdapter("enterToSend", "viewModel", requireAll = true)
fun EditText.enterToSend(enabled: Boolean, viewModel: ChatMessagesUIController<*>) {
    if (enabled) {
        imeOptions = EditorInfo.IME_ACTION_SEND or 0x1000000
        maxLines = Int.MAX_VALUE
        setSelectAllOnFocus(true)
        setHorizontallyScrolling(false)
        setRawInputType(InputType.TYPE_CLASS_TEXT)
        setOnEditorActionListener { v, actionId, event ->
            if (event != null &&
                event.keyCode == KeyEvent.KEYCODE_ENTER ||
                actionId == EditorInfo.IME_ACTION_SEND
            ) {
                viewModel.onSendMessage()
                true
            } else {
                false
            }
        }
    } else {
        setHorizontallyScrolling(false)
        isVerticalScrollBarEnabled = false
        setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
    }
}

@BindingAdapter("incognito")
fun EditText.incognitoMode(enabled: Boolean) = incognito(enabled)

@BindingAdapter("android:enabled")
fun View.enabled(enabled: Boolean) {
    if (enabled) enable() else disableWithAlpha()
}

@BindingAdapter("android:visibility")
fun View.setVisibility(text: String?) {
    visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
}

@BindingAdapter("imageUrl")
fun ImageView.loadImage(url: String?) {
    url?.let {
        Glide.with(context)
            .asBitmap()
            .override(100)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(this)
    } ?: Glide.with(context).clear(this)
}

@BindingAdapter("profilePhoto")
fun ImageView.loadImage(bitmap: Bitmap?) {
    bitmap?.let {
        background = null
        Glide.with(context)
            .asBitmap()
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .apply(RequestOptions().override(60, 60))
            .load(it)
            .into(this)
    } ?: run {
        setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_contact))
    }
}

@BindingAdapter("resourceId")
fun ImageView.loadImage(resourceId: Int?) {
    resourceId?.let {
        Glide.with(context)
            .load(it)
            .into(this)
    } ?: Glide.with(context).clear(this)
}

@BindingAdapter("elapsedTime")
fun TextView.elapsedTime(ms: Int?) {
    ms?.let {
        val minutes = ms / 1000 / 60
        val seconds = ms / 1000 % 60
        text = context.getString(R.string.chat_msg_elapsed_time, minutes, seconds)
    }
}

fun TextView.fileSize(kb: Long?) {
    kb?.let {
        text = context.getString(R.string.chat_msg_document_file_size, kb)
    }
}

@BindingAdapter("invisible")
fun View.setInvisible(invisible: Boolean) {
    visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}

@BindingAdapter("date")
fun TextView.formatDate(timestamp: Long?) {
    text = when (timestamp) {
        null -> "Never"
        else -> DateFormat.getDateTimeInstance().format(Date(timestamp))
    }
}

@BindingAdapter("fileSize")
fun TextView.formatFileSize(bytes: Long) {
    text = Formatter.formatShortFileSize(context, bytes)
}

@BindingAdapter("backupProgress")
fun ProgressBar.setProgress(task: BackupProgress?) {
    task?.run {
        progress = (bytesTransferred / bytesTotal).toInt()
    }
}

@BindingAdapter("backupProgress")
fun TextView.setProgress(task: BackupProgress?) {
    task?.run {
        text = when {
            bytesTransferred == bytesTotal -> "Restore complete!"
            null != error -> error?.message
            else -> "Restore in progress"
        }
    }
}

@BindingAdapter("backgroundTint")
fun View.backgroundTint(color: Int?) {
    color?.let { background.setTint(it) }
}

@BindingAdapter("actionIcon", "iconPosition", "iconColor", requireAll = true)
fun TextView.addDrawable(
    @DrawableRes iconRes: Int?,
    position: DrawablePosition,
    @ColorRes colorRes: Int?
) {
    iconRes?.let {
        when (position) {
            DrawablePosition.TOP -> setCompoundDrawablesWithIntrinsicBounds(0, iconRes, 0, 0)
            DrawablePosition.BOTTOM -> setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, iconRes)
            DrawablePosition.START -> setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0)
            DrawablePosition.END -> setCompoundDrawablesWithIntrinsicBounds(0, 0, iconRes, 0)
        }
        compoundDrawablePadding = 6
        for (drawable in compoundDrawables) {
            drawable?.setTint(resources.getColor(colorRes ?: currentTextColor, null))
        }
    }
}

enum class DrawablePosition {
    TOP, START, END, BOTTOM
}

@BindingAdapter("thumbnailBitmap", "thumbnailIcon", requireAll = false)
fun ImageView.thumbnail(bitmap: Bitmap?, @IdRes icon: Int?) {
    bitmap?.let {
        visibility = View.VISIBLE
        setPadding(0)
        Glide.with(context)
            .asBitmap()
            .centerCrop()
            .load(it)
            .into(this)
    } ?: icon?.let {
        setImageResource(it)
    } ?: run {
        Glide.with(context).clear(this)
    }
}

@BindingAdapter("actionIcon")
fun ImageView.icon(@IdRes icon: Int?) {
    icon?.let { setImageResource(it) }
}

@BindingAdapter("actionIconTint")
fun ImageView.actionIconTint(@ColorRes color: Int?) {
    color?.let { setTint(it) }
}

@BindingAdapter("customStyle")
fun TextView.setStyle(@IdRes id: Int?) {
    id?.let {
        setTextAppearance(it)
    }
}
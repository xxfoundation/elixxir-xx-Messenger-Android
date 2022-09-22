package io.xxlabs.messenger.support.touch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.vibrateDevice
import io.xxlabs.messenger.support.util.Utils
import kotlin.math.abs
import kotlin.math.min

class MessageSwipeController(
    private val context: Context,
    private val swipeControllerActions: SwipeActions
) : ItemTouchHelper.Callback() {
    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private lateinit var imageDrawable: Drawable
    private lateinit var itemView: View
    private var translationX = 0f
    private var replyButtonProgress: Float = 0.toFloat()
    private var lastReplyButtonAnimationTime: Long = 0
    private var swipeBack = false
    private var isVibrate = false
    private var startTracking = false
    private var isGoingBack = false
    var allowSwipe: Boolean = true

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        setTouchListener(recyclerView, viewHolder)
        itemView = viewHolder.itemView
        imageDrawable = ContextCompat.getDrawable(context, R.drawable.ic_reply_swipe)!!
        return makeMovementFlags(ACTION_STATE_IDLE, RIGHT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return allowSwipe
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = false
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        var newDx = dX / 2
        translationX = newDx
        if (actionState == ACTION_STATE_SWIPE) {
            if (translationX <= convertToPixel(animationThreshold) || newDx < translationX) {

                if (newDx >= animationThreshold) {
                    newDx = animationThreshold.toFloat()
                }
            }

            currentItemViewHolder = viewHolder
            //drawReplyButton(c)
        }
        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            newDx,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        recyclerView.setOnTouchListener { _, event ->
            swipeBack =
                event.action == MotionEvent.ACTION_CANCEL || event.action == MotionEvent.ACTION_UP
            if (swipeBack) {
                isGoingBack = true
                if (abs(itemView.translationX) >= dpThreshold) {
                    translationX = 0f
                    swipeControllerActions.showReply(viewHolder.bindingAdapterPosition)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        translationX = 0f
                    }, 150)
                }
            }
            false
        }
    }

    private fun drawReplyButton(canvas: Canvas) {
        if (currentItemViewHolder == null) {
            return
        }
        val btnTranslationX = itemView.translationX / 2
        val newTime = System.currentTimeMillis()
        val dt = min(18, newTime - lastReplyButtonAnimationTime)
        lastReplyButtonAnimationTime = newTime
        val showing = btnTranslationX >= convertToPixel(20)
        if (showing) {
            if (replyButtonProgress < 1.0f) {
                replyButtonProgress += dt / 180.0f
                if (replyButtonProgress > 1.0f) {
                    replyButtonProgress = 1.0f
                } else {
                    itemView.invalidate()
                }
            }
        } else if (btnTranslationX <= 0.0f) {
            replyButtonProgress = 0f
            startTracking = false
            isVibrate = false
            isGoingBack = false
        } else {
            if (replyButtonProgress > 0.0f) {
                replyButtonProgress -= dt / 180.0f
                if (replyButtonProgress < 0.1f) {
                    replyButtonProgress = 0f
                } else {
                    itemView.invalidate()
                }
            }
        }

        val alpha = if (isGoingBack) {
            0
        } else {
            min(255f, 255 * replyButtonProgress).toInt()
        }

        imageDrawable.alpha = alpha
        if (startTracking) {
            if (!isVibrate && itemView.translationX >= dpThreshold.toFloat() - Utils.pxToDp(5)) {
                itemView.context.vibrateDevice(60L)
                isVibrate = true
            } else {
                if (itemView.translationX < dpThreshold.toFloat()) {
                    isVibrate = false
                }
            }
        }

        val x: Int = (itemView.translationX / 2).toInt()
        val y = itemView.top / 2 + itemView.bottom / 2 + convertToPixel(5)

        imageDrawable.setBounds(
            (x - convertToPixel(15)),
            (y - convertToPixel(15)),
            (x + convertToPixel(15)),
            (y + convertToPixel(15))
        )

        imageDrawable.draw(canvas)
    }

    private fun convertToPixel(dp: Int): Int {
        return Utils.dpToPx(dp)
    }

    fun resetTranslation() {
        if (::itemView.isInitialized) {
            itemView.translationX = 0f
        }
        translationX = 0f
    }

    fun isDragging() = translationX != 0f

    companion object {
        private val dpThreshold = Utils.dpToPx(90)
        private val animationThreshold = Utils.dpToPx(110)
    }
}
package io.xxlabs.messenger.support.touch

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.support.util.Utils
import java.util.*

/**
 * ItemTouchHelper Callback, to be used as callback
 * of swipe action.
 */
@SuppressLint("ClickableViewAccessibility")
abstract class ButtonSwipeHelper(
    private val recyclerView: RecyclerView,
    private val blockedPositions: List<Int> = listOf(),
    directions: Int = ItemTouchHelper.LEFT,
    context: Context = recyclerView.context,
) : ItemTouchHelper.SimpleCallback(0, directions), View.OnTouchListener {
    var allowSwipe = true
    private var translationX = 0f
    private var buttons: MutableList<UnderlayButton>? = null
    private val gestureDetector: GestureDetector
    private var swipedPos = -1
    private var isSwiping = 0
    private var swipeThreshold = 0.5f
    private val buttonsLeftBuffer: MutableMap<Int, MutableList<UnderlayButton>>
    private val buttonsRightBuffer: MutableMap<Int, MutableList<UnderlayButton>>
    private val recoverQueue: Queue<Int>

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (button in buttons!!) {
                if (button.onClick(e.x, e.y)) {
                    break
                }
            }

            return true
        }
    }

    override fun onTouch(v: View, e: MotionEvent): Boolean {
        if (swipedPos < 0 || blockedPositions.contains(swipedPos)) return false

        val point = Point(e.rawX.toInt(), e.rawY.toInt())

        val swipedViewHolder = recyclerView.findViewHolderForAdapterPosition(swipedPos)
        val swipedItem = swipedViewHolder?.itemView
        val rect = Rect()

        swipedItem?.let {
            val isVisible = it.getGlobalVisibleRect(rect)

            if (e.action == MotionEvent.ACTION_DOWN || e.action == MotionEvent.ACTION_UP || e.action == MotionEvent.ACTION_MOVE) {
                if (rect.top < point.y && rect.bottom > point.y)
                    gestureDetector.onTouchEvent(e)
                else {
                    recoverQueue.add(swipedPos)
                    swipedPos = -1
                    recoverSwipedItem()
                }
            }
            isVisible
        }
        return false
    }

    init {
        buttons = ArrayList()
        gestureDetector = GestureDetector(context, gestureListener)
        recyclerView.setOnTouchListener(this)
        buttonsLeftBuffer = HashMap()
        buttonsRightBuffer = HashMap()
        recoverQueue = object : LinkedList<Int>() {
            override fun add(element: Int): Boolean {
                return if (contains(element)) {
                    false
                } else {
                    super.add(element)
                }
            }
        }

        attachSwipe()
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val pos = viewHolder.bindingAdapterPosition

        if (swipedPos != pos) {
            recoverQueue.add(swipedPos)
        }

        swipedPos = pos

        when {
            buttonsRightBuffer.containsKey(swipedPos) -> {
                buttons = buttonsRightBuffer[swipedPos]
            }
            buttonsLeftBuffer.containsKey(swipedPos) -> {
                buttons = buttonsLeftBuffer[swipedPos]
            }
            else -> {
                buttons!!.clear()
            }
        }

        buttonsRightBuffer.clear()
        buttonsLeftBuffer.clear()

        swipeThreshold = 0.5f * buttons!!.size.toFloat() * BUTTON_WIDTH.toFloat()
        recoverSwipedItem()
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return swipeThreshold
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return 0.1f * defaultValue
    }

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float {
        return 5.0f * defaultValue
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
        val pos = viewHolder.bindingAdapterPosition
        translationX = dX
        val itemView = viewHolder.itemView

        if (pos < 0 || blockedPositions.contains(pos)) {
            swipedPos = pos
            return
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            when {
                dX < 0 -> {
                    if (isSwiping == -1) {
                        return
                    }

                    isSwiping = 1

                    var buffer: MutableList<UnderlayButton> = mutableListOf()

                    if (!buttonsRightBuffer.containsKey(pos)) {
                        instantiateUnderlayButton(viewHolder, underlayButtonsRight = buffer)
                        buttonsRightBuffer[pos] = buffer
                    } else {
                        buffer = buttonsRightBuffer[pos]!!
                    }

                    translationX =
                        dX * buffer.size.toFloat() * BUTTON_WIDTH.toFloat() / itemView.width
                    drawButtons(c, itemView, buffer, pos, translationX, true)
                    onStartDragging()
                }
                dX > 0 -> {
                    if (isSwiping == 1) {
                        return
                    }

                    isSwiping = -1

                    var buffer: MutableList<UnderlayButton> = mutableListOf()

                    if (!buttonsLeftBuffer.containsKey(pos)) {
                        instantiateUnderlayButton(viewHolder, underlayButtonsLeft = buffer)
                        buttonsLeftBuffer[pos] = buffer
                    } else {
                        buffer = buttonsLeftBuffer[pos]!!
                    }

                    translationX =
                        dX * buffer.size.toFloat() * BUTTON_WIDTH.toFloat() / itemView.width
                    drawButtons(c, itemView, buffer, pos, translationX, false)
                    onStartDragging()
                }
                else -> {
                    isSwiping = 0
                    onStopDragging()
                }
            }
        }

        super.onChildDraw(
            c,
            recyclerView,
            viewHolder,
            translationX,
            dY,
            actionState,
            isCurrentlyActive
        )
    }

    @Synchronized
    private fun recoverSwipedItem() {
        while (!recoverQueue.isEmpty()) {
            val pos = recoverQueue.poll()
            if (pos != null && pos > -1) {
                recyclerView.adapter!!.notifyItemChanged(pos)
            }
        }
    }

    private fun drawButtons(
        c: Canvas,
        itemView: View,
        buffer: List<UnderlayButton>,
        pos: Int,
        dX: Float,
        swipeLeft: Boolean
    ) {
        if (swipeLeft) {
            drawRightButtons(c, itemView, buffer, pos, dX)
        } else {
            drawLeftButtons(c, itemView, buffer, pos, dX)
        }
    }

    private fun drawRightButtons(
        c: Canvas,
        itemView: View,
        buffer: List<UnderlayButton>,
        pos: Int,
        dX: Float
    ) {
        var right = itemView.right.toFloat()
        val dButtonWidth = -1 * dX / buffer.size

        for (button in buffer) {
            val left = right - dButtonWidth
            button.onDraw(
                c,
                RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ),
                pos
            )

            right = left
        }
    }

    private fun drawLeftButtons(
        c: Canvas,
        itemView: View,
        buffer: List<UnderlayButton>,
        pos: Int,
        dX: Float
    ) {
        var left = itemView.left.toFloat()
        val dButtonWidth = dX / buffer.size

        for (button in buffer) {
            val right = left + dButtonWidth
            button.onDraw(
                c,
                RectF(
                    left,
                    itemView.top.toFloat(),
                    right,
                    itemView.bottom.toFloat()
                ),
                pos
            )

            left = right
        }
    }

    private fun attachSwipe() {
        val itemTouchHelper = ItemTouchHelper(this)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    fun isDragging() = translationX != 0f

    fun reset() {
        recoverSwipedItem()
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return allowSwipe
    }

    abstract fun instantiateUnderlayButton(
        viewHolder: RecyclerView.ViewHolder,
        underlayButtonsLeft: MutableList<UnderlayButton> = mutableListOf(),
        underlayButtonsRight: MutableList<UnderlayButton> = mutableListOf()
    )

    abstract fun onStartDragging()

    abstract fun onStopDragging()

    companion object {
        val BUTTON_WIDTH = Utils.dpToPx(56)
    }
}
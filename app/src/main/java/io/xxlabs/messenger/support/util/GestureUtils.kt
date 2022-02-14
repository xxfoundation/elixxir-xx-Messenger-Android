package io.xxlabs.messenger.support.util

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import io.xxlabs.messenger.data.datatype.Gesture
import timber.log.Timber
import kotlin.math.abs

class GestureUtils {
    companion object {
        @SuppressLint("ClickableViewAccessibility")
        fun addGestureTop(view: View, gesture: Gesture, onSwipe: () -> Unit) {
            val gestureDetector = GestureDetector(
                view.context,
                object : GestureDetector.SimpleOnGestureListener() {

                    override fun onDown(e: MotionEvent): Boolean {
                        return true
                    }

                    override fun onFling(
                        e1: MotionEvent,
                        e2: MotionEvent,
                        velocityX: Float,
                        velocityY: Float
                    ): Boolean {
                        Timber.i("onFling has been called!")
                        val swipeMinDistance = 120
                        val swipeMaxOffPath = 250
                        val swipeThresholdVelocity = 200

                        try {
                            when (gesture) {
                                Gesture.BOTTOM_TOP -> {
                                    if (e1.y - e2.y > swipeMaxOffPath && abs(velocityY) > swipeThresholdVelocity) {
                                        Timber.i("Swipe top down")
                                        onSwipe()
                                    }
                                }

                                Gesture.TOP_BOTTOM -> {
                                    if (e2.y - e1.y > swipeMaxOffPath && abs(velocityY) > swipeThresholdVelocity) {
                                        Timber.i("Swipe bottom up")
                                        onSwipe()
                                    }
                                }

                                Gesture.RIGHT_LEFT -> {
                                    if (e1.x - e2.x > swipeMinDistance && abs(velocityX) > swipeThresholdVelocity) {
                                        Timber.i("Right to Left")
                                        onSwipe()
                                    }
                                }

                                Gesture.LEFT_RIGHT -> {
                                    if (e2.x - e1.x > swipeMinDistance && abs(velocityX) > swipeThresholdVelocity) {
                                        Timber.i("Left to Right")
                                        onSwipe()
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            // nothing
                        }

                        return super.onFling(e1, e2, velocityX, velocityY)
                    }
                })

            view.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        }
    }
}
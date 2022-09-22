package io.xxlabs.messenger.support.view

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition


/**
 * A target for display [GifDrawable] or [Drawable] objects in [ImageView]fs.
 */
class GifDrawableImageViewTarget(
    view: ImageView,
    loopCount: Int? = null,
    private val onCompleteCallback: Animatable2Compat.AnimationCallback? = null
) : ImageViewTarget<GifDrawable>(view) {

    private val mLoopCount: Int = loopCount ?: GifDrawable.LOOP_FOREVER

    override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
        super.onResourceReady(resource, transition)
        onCompleteCallback?.let { resource.registerAnimationCallback(it) }
    }

    override fun setResource(resource: GifDrawable?) {
        if (resource is GifDrawable) {
            resource.setLoopCount(mLoopCount)
        }
        view.setImageDrawable(resource)
    }
}
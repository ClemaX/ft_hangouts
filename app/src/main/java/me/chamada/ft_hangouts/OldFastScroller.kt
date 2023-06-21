package me.chamada.ft_hangouts

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class OldFastScroller : LinearLayout {
    companion object {
        const val HANDLE_ANIMATION_DURATION: Long = 100
        const val HANDLE_HIDE_DELAY: Long = 1000
    }

    private var thumb: View
    private var bubble: View
        set(value) {
            field = value

            // Pivot from the bottom right corner
            field.pivotX = field.width.toFloat()
            field.pivotY = field.height.toFloat()

            var scaleX = ObjectAnimator.ofFloat(field, "scaleX", 0f, 1f)
                .setDuration(HANDLE_ANIMATION_DURATION)
            var scaleY = ObjectAnimator.ofFloat(field, "scaleY", 0f, 1f)
                .setDuration(HANDLE_ANIMATION_DURATION)
            var transparency = ObjectAnimator.ofFloat(field, "alpha", 0f, 1f)
                .setDuration(HANDLE_ANIMATION_DURATION)
            showAnimator.playTogether(scaleX, scaleY, transparency)
            showAnimator.doOnStart { field.visibility = VISIBLE }

            scaleX = ObjectAnimator.ofFloat(field, "scaleX", 1f, 0f)
                .setDuration(HANDLE_ANIMATION_DURATION)
            scaleY = ObjectAnimator.ofFloat(field, "scaleY", 1f, 0f)
                .setDuration(HANDLE_ANIMATION_DURATION)
            transparency = ObjectAnimator.ofFloat(field, "alpha", 1f, 0f)
                .setDuration(HANDLE_ANIMATION_DURATION)
            hideAnimator.playTogether(scaleX, scaleY, transparency)
            hideAnimator.doOnEnd { field.visibility = INVISIBLE }

            hideAnimator.startDelay = HANDLE_HIDE_DELAY
        }

    private var bubbleVisibility: Int
        set(value) {
            if (value == VISIBLE) {
                hideAnimator.cancel()
                showAnimator.start()
            } else if (value == INVISIBLE) {
                showAnimator.cancel()
                hideAnimator.start()
            }
        }
        get() = bubble.visibility


    private var position: Float = 0f
        set(value) {
            val maxThumbY = (height - thumb.height).toFloat()

            field = value.coerceIn(0f, 1f)

            thumb.y = (field * maxThumbY).coerceIn(0f, maxThumbY)
            bubble.y = thumb.y + thumb.height / 2 - bubble.height / 2
        }

    private var showAnimator: AnimatorSet = AnimatorSet()
    private var hideAnimator: AnimatorSet = AnimatorSet()

    var recyclerView: RecyclerView? = null
        set(value) {
            field = value
            field?.addOnScrollListener(scrollListener)
        }

    private var scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            //super.onScrolled(recyclerView, dx, dy)
            val range = recyclerView.computeVerticalScrollRange()
            val extent = recyclerView.computeVerticalScrollExtent()
            val offset = recyclerView.computeVerticalScrollOffset()
            position = offset.toFloat() / (range - extent)
        }
    }

    constructor(context: Context, attributes: AttributeSet, defStyleAttribute: Int)
    : super(context, attributes, defStyleAttribute) {
    }

    constructor(context: Context, attributes: AttributeSet)
            : super(context, attributes) {
    }

    constructor(context: Context)
            : super(context) {
    }

    init {
        val inflater = LayoutInflater.from(context)

        orientation = HORIZONTAL
        clipChildren = false

        inflater.inflate(R.layout.fastscroller, this)

        thumb = findViewById(R.id.fastscroller_thumb)
        bubble = findViewById(R.id.fastscroller_bubble)
    }

    private fun performScroll() {
        val itemCount = recyclerView?.adapter?.itemCount


        if (itemCount != null && itemCount != 0) {
            Log.d("OldFastScroller", "proportion: $position}")

            val scrollPosition = position * itemCount

            //val scrollPosition = position * (recyclerView!!.adapter!!.itemCount - 1)
            (recyclerView!!.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(scrollPosition.toInt(), 0)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            ACTION_DOWN -> {
                position = event.y / height

                // Show handle
                bubbleVisibility = VISIBLE

                performScroll()

                true
            }
            ACTION_MOVE -> {
                position = event.y / height

                performScroll()

                true
            }
            ACTION_UP, ACTION_CANCEL -> {
                // Hide handle
                bubbleVisibility = INVISIBLE

                true
            }
            else -> super.onTouchEvent(event)
        }
    }
}
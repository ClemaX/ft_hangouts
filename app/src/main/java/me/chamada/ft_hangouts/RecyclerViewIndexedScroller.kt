package me.chamada.ft_hangouts

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.R.*
import com.google.android.material.color.MaterialColors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class RecyclerViewIndexedScroller(context: Context, attrs: AttributeSet) : View(context, attrs) {
    interface IndexLabelListener {
        fun getIndexLabel(position: Int): String
    }

    private class OnScrollListener(private val scroller: RecyclerViewIndexedScroller):
        RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val contentHeight = recyclerView.computeVerticalScrollRange()
            val scrollPosition = recyclerView.computeVerticalScrollOffset()

            scroller.notifyRecyclerViewContentHeightChanged(contentHeight)
            scroller.notifyRecyclerViewScrollPositionChanged(scrollPosition)

            val adapter = recyclerView.adapter

            if (contentHeight != 0 && adapter is IndexLabelListener) {
                val scrollProportion = scrollPosition / contentHeight.toFloat()
                val position = (scrollProportion * adapter.itemCount).roundToInt()

                val indexLabelText = adapter.getIndexLabel(position)

                scroller.setIndexLabelText(indexLabelText)
            }
        }
    }

    abstract class OnScrollChangeListener {
        abstract fun onScrollStart()
        abstract fun onScrollEnd()
    }

    companion object {
        const val DEFAULT_INDEX_LABEL_SIZE = 128f
        const val DEFAULT_INDEX_THUMB_WIDTH = 24f
        const val DEFAULT_THUMB_MIN_HEIGHT_RATIO = 4f
        const val DEFAULT_INDEX_LABEL_TEXT_SIZE_RATIO = 0.5f
    }

    var recyclerView: RecyclerView? = null
        set(value) {
            if (field != value) {
                field?.removeOnScrollListener(scrollerScrollListener)
                value?.addOnScrollListener(scrollerScrollListener)

                field = value
            }
        }

    private var recyclerViewContentHeight: Int? = null
    private var recyclerViewScrollPosition: Int = 0
    private var thumbSelected: Boolean = false
    private var indexLabelText: String? = null

    private val thumbWidth: Int
    private val thumbMinHeight: Int
    private val indexLabelSize: Int

    private val animationDuration: Long = context.resources.getInteger(
        androidx.appcompat.R.integer.config_tooltipAnimTime).toLong()
    private val fadeDelayNs: Long = 2 * 1000 * 1000 * 1000

    private val trackRect: Rect = Rect()
    private val thumbRect: Rect = Rect()

    private val textBounds: Rect = Rect()

    private val thumbColor: Int
    private val thumbPressedColor: Int

    private val trackPaint = Paint()
    private val thumbPaint = Paint()
    private val indexLabelBackgroundPaint = Paint()
    private val indexLabelPaint = Paint()

    private var lastActivityTimeNs: Long = System.nanoTime()

    private val scrollerScrollListener = OnScrollListener(this)

    private val scrollChangeListeners: MutableList<OnScrollChangeListener> = emptyList<OnScrollChangeListener>().toMutableList();

    init {
        val colorSecondary = MaterialColors.getColor(context, attr.colorSecondary, Color.BLACK)
        val colorSecondaryVariant = MaterialColors.getColor(context, attr.colorSecondaryVariant, Color.BLACK)

        val colorOnSurface = MaterialColors.getColor(context, attr.colorOnSurface, Color.GRAY)
        val colorOnSecondary = MaterialColors.getColor(context, attr.colorOnSecondary, Color.BLACK)

        val defaultTrackColor = MaterialColors.compositeARGBWithAlpha(colorOnSurface, 255 / 3)
        val defaultThumbColor = MaterialColors.compositeARGBWithAlpha(colorOnSurface, 255 * 2 / 3)

        context.theme.obtainStyledAttributes(attrs, R.styleable.RecyclerViewIndexedScroller,
            0, 0
        ).apply {
            try {
                thumbWidth = getDimension(
                    R.styleable.RecyclerViewIndexedScroller_thumbWidth,
                    DEFAULT_INDEX_THUMB_WIDTH
                ).roundToInt()

                thumbMinHeight = getDimension(
                    R.styleable.RecyclerViewIndexedScroller_thumbMinHeight,
                    thumbWidth * DEFAULT_THUMB_MIN_HEIGHT_RATIO
                ).roundToInt()

                indexLabelSize = getDimension(
                    R.styleable.RecyclerViewIndexedScroller_indexLabelSize,
                    DEFAULT_INDEX_LABEL_SIZE
                ).roundToInt()

                trackPaint.apply {
                    color = getInt(
                        R.styleable.RecyclerViewIndexedScroller_trackColor,
                        defaultTrackColor
                    )
                }

                thumbColor = getInt(
                    R.styleable.RecyclerViewIndexedScroller_thumbColor,
                    defaultThumbColor
                )

                thumbPressedColor = getInt(
                    R.styleable.RecyclerViewIndexedScroller_thumbPressedColor,
                    colorSecondary
                )

                thumbPaint.apply {
                    color = thumbColor
                }

                indexLabelBackgroundPaint.apply {
                    color = getInt(
                        R.styleable.RecyclerViewIndexedScroller_indexLabelBackground,
                        colorSecondaryVariant
                    )
                }

                indexLabelPaint.apply {
                    color = getInt(
                        R.styleable.RecyclerViewIndexedScroller_indexLabelColor,
                        colorOnSecondary
                    )
                    textSize = getDimension(
                        R.styleable.RecyclerViewIndexedScroller_indexLabelTextSize,
                        indexLabelSize * DEFAULT_INDEX_LABEL_TEXT_SIZE_RATIO
                    )
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    style = Paint.Style.FILL
                }
            }
            finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentHeight = recyclerViewContentHeight

        if (contentHeight == null || contentHeight < measuredHeight) {
            return
        }

        canvas.apply {
            val savedState = save()
            val thumbX = (width - thumbWidth).toFloat()

            // Move to the thumb x position
            translate(thumbX, 0f)

            // Draw track and thumb rectangles
            drawRect(trackRect, trackPaint)
            drawRect(thumbRect, thumbPaint)

            restoreToCount(savedState)

            if (thumbSelected && indexLabelText != null) {
                val thumbCenterY = thumbRect.centerY()
                val y = max(0, thumbCenterY)

                translate(thumbX - indexLabelSize / 2f, y.toFloat())
                drawCircle(0f, 0f, indexLabelSize.toFloat() / 2, indexLabelBackgroundPaint)

                indexLabelText?.let { text ->
                    drawText(text, 0f, indexLabelPaint.textSize / 3, indexLabelPaint)
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w != 0 && h != 0) {
            trackRect.set(thumbWidth, 0, 0, h)
            thumbRect.set(thumbWidth, 0, 0, getThumbHeight())
        }
    }

    private fun getThumbHeight(): Int {
        val contentHeight = recyclerViewContentHeight
        val thumbHeight = if (contentHeight != null) {
            measuredHeight * measuredHeight / contentHeight.toFloat()
        }
        else {
            0f
        }

        return max(thumbMinHeight, min(measuredHeight, thumbHeight.roundToInt()))
    }

    override fun getLayoutParams(): ViewGroup.LayoutParams {
        return super.getLayoutParams().apply {
            width = thumbWidth + indexLabelSize
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        visibility = INVISIBLE
        fadeOutRunnable.run()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handler.removeCallbacks(fadeOutRunnable)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)

        if (visibility == VISIBLE) alpha = 1f
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return when (event?.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                visibility = VISIBLE
                thumbSelected = true
                thumbPaint.color = thumbPressedColor

                val scrollProportion = event.y / measuredHeight
                val position = (scrollProportion * measuredHeight).toInt()

                scrollToPosition(position)

                if (event.action == MotionEvent.ACTION_DOWN) {
                    invalidate()
                    dispatchScrollStart()
                }

                true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                thumbSelected = false
                thumbPaint.color = thumbColor

                invalidate()
                dispatchScrollEnd()

                true
            }
            else -> super.onTouchEvent(event)
        }
    }

    private val fadeOutRunnable = object : Runnable {
        override fun run() {
            val elapsedNs = System.nanoTime() - lastActivityTimeNs

            try {
              if (!thumbSelected && elapsedNs >= fadeDelayNs) {
                  animate()
                      .alpha(0f)
                      .setDuration(animationDuration)
                      .setListener(object: AnimatorListenerAdapter() {
                          override fun onAnimationEnd(animation: Animator) {
                              visibility = INVISIBLE
                          }
                      })
              }
            } finally {
                handler.postDelayed(this, fadeDelayNs / (1000 * 1000))
            }
        }
    }

    private fun scrollToPosition(position: Int) {
        val adapter = recyclerView?.adapter ?: return

        val maxScroll = computeVerticalScrollRange()
        val scrollProportion = min(1f, position / maxScroll.toFloat())
        val scrollPosition = scrollProportion * adapter.itemCount

        recyclerView?.scrollToPosition(scrollPosition.roundToInt())
    }

    private fun updateThumbHeight() {
        recyclerViewContentHeight?.let {contentHeight ->
            visibility = VISIBLE

            val thumbHeight = getThumbHeight()

            val scrollProportion = recyclerViewScrollPosition / contentHeight.toFloat()
            val scrollPosition = min(measuredHeight - thumbHeight, (measuredHeight * scrollProportion).toInt())

            thumbRect.set(thumbWidth, scrollPosition, 0, scrollPosition + thumbHeight)

            invalidate()

            lastActivityTimeNs = System.nanoTime()
        }
    }

    private fun dispatchScrollStart() {
        for (listener in scrollChangeListeners) {
            listener.onScrollStart()
        }
    }

    private fun dispatchScrollEnd() {
        for (listener in scrollChangeListeners) {
            listener.onScrollEnd()
        }
    }

    fun setOnScrollChangeListener(l: OnScrollChangeListener) {
        if (!scrollChangeListeners.contains(l)) {
            scrollChangeListeners.add(l)
        }
    }

    fun removeOnScrollChangeListener(l: OnScrollChangeListener) {
        scrollChangeListeners.remove(l)
    }
    fun setIndexLabelText(text: String?) {
        indexLabelText = text
        indexLabelPaint.getTextBounds(text, 0, text?.length ?: 0, textBounds)
    }

    fun notifyRecyclerViewContentHeightChanged(contentHeight: Int) {
        if (contentHeight >= 0) {
            recyclerViewContentHeight = contentHeight
        }
    }

    fun notifyRecyclerViewScrollPositionChanged(scrollPosition: Int) {
        recyclerViewScrollPosition = scrollPosition
        updateThumbHeight()
    }
}
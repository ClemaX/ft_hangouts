package me.chamada.ft_hangouts

import android.animation.ValueAnimator
import android.graphics.Canvas
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import kotlinx.coroutines.Runnable

class ScrollerBubble(private val recyclerView: RecyclerView) : ItemDecoration() {
    companion object {
        private const val SHOW_DURATION_MS: Long = 500
        private const val HIDE_DURATION_MS: Long = 500
        private const val HIDE_DELAY_AFTER_VISIBLE_MS: Long = 1500
    }

    enum class State {
        HIDDEN,
        VISIBLE,
    }

    enum class AnimationState {
        OUT,
        FADING_IN,
        IN,
        FADING_OUT,
    }

    private val showHideAnimator: ValueAnimator = ValueAnimator.ofFloat(0, 1);

    private val hideRunnable: Runnable = Runnable {
        hide(HIDE_DURATION_MS)
    }

    private var recyclerViewWidth: Int = 0
    private var recyclerViewHeight: Int = 0

    private var needVerticalBubble: Boolean = false
    private var needHorizontalBubble: Boolean = false

    private var state: State = State.HIDDEN
        set(value) {
            if (state == State.HIDDEN) {
                requestRedraw()
            } else {
                show()
                resetHideDelay(HIDE_DELAY_AFTER_VISIBLE_MS)
            }
            field = value
        }

    private var animationState: AnimationState = AnimationState.OUT;

    private fun requestRedraw() {
        recyclerView.invalidate();
    }

    fun show() {
        if (animationState == AnimationState.FADING_OUT) showHideAnimator.cancel()
        else if (animationState != AnimationState.OUT) return

        animationState = AnimationState.FADING_IN

        showHideAnimator.setFloatValues(showHideAnimator.animatedValue as Float, 1f)
        showHideAnimator.duration = SHOW_DURATION_MS
        showHideAnimator.start()
    }

    fun hide(duration: Long) {
        if (animationState == AnimationState.FADING_IN) showHideAnimator.cancel()
        else if (animationState != AnimationState.IN) return

        animationState = AnimationState.FADING_OUT

        showHideAnimator.setFloatValues(showHideAnimator.animatedValue as Float, 0f)
        showHideAnimator.duration = duration
        showHideAnimator.start()
    }

    private fun cancelHide() {
        recyclerView.removeCallbacks(hideRunnable)
    }

    private fun resetHideDelay(delay: Long) {
        cancelHide()
        recyclerView.postDelayed(hideRunnable, delay)
    }

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, recyclerViewState: RecyclerView.State) {
        if (recyclerView.width != recyclerViewWidth
            || recyclerView.height != recyclerViewHeight) {
            recyclerViewWidth = recyclerView.width
            recyclerViewHeight = recyclerView.height

            state = State.HIDDEN
        }
        super.onDrawOver(canvas, parent, recyclerViewState)

        if (animationState != AnimationState.OUT) {
            if (needVerticalBubble) {
                drawVerticalBubble(canvas)
            }
        }
    }

    private fun drawVerticalBubble(canvas: Canvas) {
        val viewWidth = recyclerViewWidth

        val left = viewWidth - verticalBubbleWidth - verticalOffset
        val top = verticalBubbleCenter - verticalBubbleHeight / 2

        bubble
    }
}
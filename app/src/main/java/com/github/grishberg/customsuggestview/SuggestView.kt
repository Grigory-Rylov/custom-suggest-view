package com.github.grishberg.customsuggestview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.Scroller
import android.widget.Toast
import kotlin.math.min

private const val MAX_SUGGEST_COUNT = 10

/**
 * Main suggests view
 */
class SuggestView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : View(ctx, attrs, style) {
    private val bubbles = mutableListOf<SuggestBubbleDrawable>()
    private val listener = SizeChangedAction()
    private val bubbleHorisontalMargin =
        ctx.resources.getDimensionPixelOffset(R.dimen.bubbleHorizontalMargin)
    private val bubbleVerticalMargin =
        ctx.resources.getDimensionPixelOffset(R.dimen.bubbleVerticalMargin)
    private var bubblesCount = 0
    private val gestureDetector: GestureDetector = GestureDetector(context, MyGestureListener())
    private val scroller = Scroller(context)

    init {
        for (i in 0 until MAX_SUGGEST_COUNT) {
            bubbles.add(SuggestBubbleDrawable(context))
        }
        isHorizontalScrollBarEnabled = true
        isVerticalScrollBarEnabled = false
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, bubbles[0].height + bubbleVerticalMargin * 2)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.translate(0f, bubbleVerticalMargin.toFloat())
        for (i in 0 until bubblesCount) {
            bubbles[i].draw(canvas)
        }
        canvas.restore()
    }

    fun setInitialSuggests(suggests: List<String>) {
        bubblesCount = min(suggests.size, MAX_SUGGEST_COUNT)

        for (i in 0 until bubblesCount) {
            val currentBubble = bubbles[i]
            currentBubble.updateSizeAction = listener
            currentBubble.setText(suggests[i])
            currentBubble.updateImmideately()
        }
        invalidate()
    }

    fun setSuggests(suggests: List<String>) {
        bubblesCount = min(suggests.size, MAX_SUGGEST_COUNT)

        for (i in 0 until bubblesCount) {
            val currentBubble = bubbles[i]
            currentBubble.updateSizeAction = listener
            currentBubble.setText(suggests[i])
        }

        for (i in 0 until bubblesCount) {
            bubbles[i].startAnimation()
        }
    }

    /**
     * Main idea https://habr.com/ru/post/120931
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val contentWidth = computeHorizontalScrollRange()
        // check for tap and cancel fling
        if (event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_DOWN) {
            if (!scroller.isFinished) scroller.abortAnimation()
        }

        if (gestureDetector.onTouchEvent(event)) return true

        // check for pointer release
        if (event.pointerCount == 1 && event.action and MotionEvent.ACTION_MASK == MotionEvent.ACTION_UP) {
            var newScrollX = scrollX
            if (scrollX < 0) newScrollX = 0 else if (scrollX > contentWidth - width) newScrollX =
                contentWidth - width
            var newScrollY = scrollY
            if (scrollY < 0) newScrollY = 0 else if (scrollY > contentWidth - height) newScrollY =
                contentWidth - height
            if (newScrollX != scrollX || newScrollY != scrollY) {
                scroller.startScroll(
                    scrollX,
                    scrollY,
                    newScrollX - scrollX,
                    0
                )
                awakenScrollBars(scroller.duration)
            }
        }

        return true
    }

    override fun computeHorizontalScrollRange(): Int {
        var totalContentWidth = bubbleHorisontalMargin
        for (i in 0 until bubblesCount) {
            totalContentWidth += bubbles[i].bounds.width() + bubbleHorisontalMargin
        }
        return totalContentWidth
    }

    override fun computeVerticalScrollRange(): Int {
        return 0
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            val oldX = scrollX
            val oldY = scrollY
            val x: Int = scroller.currX

            val contentWidth = computeHorizontalScrollRange()
            val scrollBeyondBounds = scrollX < 0 || scrollX > contentWidth
            if (scrollBeyondBounds) {
                return
            }

            scrollTo(x, 0)
            if (oldX != scrollX) {
                onScrollChanged(scrollX, scrollY, oldX, oldY)
            }
            postInvalidate()
        }
    }

    private inner class SizeChangedAction : OnSizeChangedAction {
        override fun onSizeChanged() {
            var left = bubbleHorisontalMargin

            for (i in 0 until bubblesCount) {
                bubbles[i].leftOffset = left
                left += bubbles[i].bounds.width() + bubbleHorisontalMargin
            }
            invalidate()
        }
    }

    private inner class MyGestureListener : SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            for (i in 0 until bubblesCount) {
                val x = e.x.toInt() + scroller.startX
                if (bubbles[i].isContains(x, e.y.toInt())) {
                    Toast.makeText(context, bubbles[i].currentText, Toast.LENGTH_SHORT).show()
                    return true
                }
            }
            return false
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            val contentWidth = computeHorizontalScrollRange()
            val totalOffsetOfterScrollX = scrollX + distanceX
            if (distanceX < 0) {
                scrollLeft(totalOffsetOfterScrollX, distanceX)
            } else {
                scrollRight(totalOffsetOfterScrollX, contentWidth, distanceX)
            }
            return true
        }

        private fun scrollRight(
            totalOffsetOfterScrollX: Float,
            contentWidth: Int,
            distanceX: Float
        ) {
            if (totalOffsetOfterScrollX + width > contentWidth) {
                scrollBy(min(distanceX.toInt(), (contentWidth - scrollX - width)), 0)
            } else {
                scrollBy(distanceX.toInt(), 0)
            }
        }

        private fun scrollLeft(totalOffsetOfterScrollX: Float, distanceX: Float) {
            if (totalOffsetOfterScrollX < 0) {
                scrollBy((distanceX - totalOffsetOfterScrollX).toInt(), 0)
            } else {
                scrollBy(distanceX.toInt(), 0)
            }
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val contentWidth = computeHorizontalScrollRange()
            val scrollBeyondBounds = scrollX < 0 || scrollX + width > contentWidth
            if (scrollBeyondBounds) {
                return false
            }

            scroller.fling(
                scrollX, scrollY,
                (-velocityX).toInt(), (-velocityY).toInt(),
                0, contentWidth - width,
                0, 0
            )
            awakenScrollBars(scroller.duration)
            return true
        }
    }
}
package com.github.grishberg.customsuggestview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
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
    private var bubblesCount = 0

    init {
        for (i in 0 until MAX_SUGGEST_COUNT) {
            bubbles.add(SuggestBubbleDrawable(context))
        }
    }

    override fun onDraw(canvas: Canvas) {
        for (i in 0 until bubblesCount) {
            bubbles[i].draw(canvas)
        }
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
}
package com.github.grishberg.customsuggestview

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.updateBounds

interface OnSizeChangedAction {
    /**
     * Is called when suggest changes size
     */
    fun onSizeChanged()
}

/**
 * Single suggest drawable.
 */
class SuggestBubbleDrawable(
    context: Context
) : Drawable() {
    private val bgColor = ContextCompat.getColor(context, R.color.suggestBubble)
    private val textColor = ContextCompat.getColor(context, R.color.suggestBubbleText)
    private var currentText = ""
    private var targetSize = RectF()
    private var currentSize = RectF()
    private val textBounds = Rect()
    private var targetTextWidth = 0
    private val leftPadding = context.resources.getDimension(R.dimen.bubblePadding)
    private val rightPadding = leftPadding
    private val topPadding = leftPadding
    private val bottomPadding = leftPadding
    private val radius: Float = context.resources.getDimension(R.dimen.bubbleRadius)
    private var textTop = 0f
    private val animatorUpdateListener = UpdateListener()
    private var lastVisibleCharCound = 0
    private val drawCharBuffer = CharArray(1)

    private var animator: ValueAnimator = ValueAnimator.ofInt(0, 1)
    var updateSizeAction: OnSizeChangedAction? = null
    var leftOffset: Int = 0

    private val borderPaint = Paint().apply {
        color = bgColor
    }
    private var textPaint = Paint().apply {
        color = textColor
        textSize = context.resources.getDimension(R.dimen.bubbleTextSize)
        isAntiAlias = true
    }

    /**
     * Set target text.
     */
    fun setText(text: String) {
        currentText = text
        textPaint.getTextBounds(currentText, 0, currentText.length, textBounds)
        targetTextWidth = textPaint.measureText(currentText).toInt()
        targetSize.set(
            0f, 0f, (leftPadding + rightPadding + targetTextWidth),
            (topPadding + bottomPadding + textBounds.height())
        )
        textTop = targetSize.height() / 2 - (textPaint.descent() + textPaint.ascent()) / 2
    }

    /**
     * Start expanding or collapsing animation.
     */
    fun startAnimation() {
        currentSize.top = targetSize.top
        currentSize.bottom = targetSize.bottom
        animator = ValueAnimator.ofInt(bounds.width(), targetSize.width().toInt())
        animator.addUpdateListener(animatorUpdateListener)
        animator.duration = 1000
        animator.start()
    }

    /**
     * Updates bubble bounds without animation
     */
    fun updateImmideately() {
        currentSize.set(targetSize)
        updateBounds(
            currentSize.left.toInt(),
            currentSize.top.toInt(),
            currentSize.right.toInt(),
            currentSize.bottom.toInt()
        )
    }

    override fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(leftOffset.toFloat(), 0f)
        canvas.drawRoundRect(currentSize, radius, radius, borderPaint)
        drawTextInsideBounds(canvas)
        canvas.restore()
    }

    private fun drawTextInsideBounds(canvas: Canvas) {
        var left = leftPadding
        for (i in lastVisibleCharCound until currentText.length) {
            drawCharBuffer[0] = currentText[i]

            val width = textPaint.measureText(currentText, i, i + 1)

            if (left + width + rightPadding > currentSize.width()) {
                break
            }
            canvas.drawText(drawCharBuffer, 0, 1, left, textTop, textPaint)
            left += width
        }
    }

    override fun setAlpha(alpha: Int) = Unit

    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun setColorFilter(colorFilter: ColorFilter?) = Unit

    private inner class UpdateListener : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animator: ValueAnimator) {
            val currentWidth = animator.animatedValue as Int

            currentSize.right = currentWidth.toFloat()

            updateBounds(
                currentSize.left.toInt(),
                currentSize.top.toInt(),
                currentSize.right.toInt(),
                currentSize.bottom.toInt()
            )
            updateSizeAction?.onSizeChanged()
        }
    }
}
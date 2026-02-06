package com.example.blockdoku.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.blockdoku.R

class BoardGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val thinPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.gray_600)
        strokeWidth = 1f
    }

    private val thickPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.gray_700)
        strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cellSize = width / 9f

        for (i in 1 until 9) {
            val paint = if (i % 3 == 0) thickPaint else thinPaint

            // 세로선
            canvas.drawLine(
                i * cellSize, 0f,
                i * cellSize, height.toFloat(),
                paint
            )

            // 가로선
            canvas.drawLine(
                0f, i * cellSize,
                width.toFloat(), i * cellSize,
                paint
            )
        }
    }
}

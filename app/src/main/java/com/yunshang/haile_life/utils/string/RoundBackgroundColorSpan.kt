package com.yunshang.haile_life.utils.string

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.text.style.ReplacementSpan


/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/7 17:51
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class RoundBackgroundColorSpan(
    private val bgColor: Int,
    private val textColor: Int,
    private val radius: Float
) :
    ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int = paint.measureText(text, start, end).toInt() + 60

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val color1 = paint.color
        paint.color = bgColor
        canvas.drawRoundRect(
            RectF(
                x,
                (top + 1).toFloat(),
                x + (paint.measureText(text, start, end).toInt() + radius * 2),
                (bottom - 1).toFloat()
            ), radius, radius, paint
        )
        paint.color = textColor
        text?.let {
            canvas.drawText(text, start, end, x + radius, y.toFloat(), paint)
        }
        paint.color = color1
    }
}
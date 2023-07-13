package com.yunshang.haile_life.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.yunshang.haile_life.R


/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/13 11:53
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class DividerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var ORIENTATION_HORIZONTAL = 0
    var ORIENTATION_VERTICAL = 1
    private var mPaint: Paint? = null
    private var orientation = ORIENTATION_VERTICAL

    init {

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.DividerView, 0, 0)

        try {
            val dashGap = a.getDimensionPixelSize(R.styleable.DividerView_dashGap, 5)
            val dashLength = a.getDimensionPixelSize(R.styleable.DividerView_dashLength, 5)
            val dashThickness = a.getDimensionPixelSize(R.styleable.DividerView_dashThickness, 3)
            val lineColor = a.getColor(R.styleable.DividerView_divider_line_color, -0x1000000)
            orientation = a.getInt(R.styleable.DividerView_divider_orientation, ORIENTATION_HORIZONTAL)

            mPaint = Paint().apply {
                isAntiAlias = true
                color = lineColor
                style = Paint.Style.STROKE
                strokeWidth = dashThickness.toFloat()
                pathEffect =
                    DashPathEffect(floatArrayOf(dashGap.toFloat(), dashLength.toFloat()), 0f)
            }
        } finally {
            a.recycle()
        }
    }

    fun setBgColor(color: Int) {
        mPaint!!.color = color
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        mPaint?.let {
            if (orientation == ORIENTATION_HORIZONTAL) {
                val center = height * 0.5f
                canvas.drawLine(0f, center, width.toFloat(), center, it)
            } else {
                val center = width * 0.5f
                canvas.drawLine(center, 0f, center, height.toFloat(), it)
            }
        }
    }
}
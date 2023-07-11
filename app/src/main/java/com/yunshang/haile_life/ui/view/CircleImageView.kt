package com.yunshang.haile_life.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import com.yunshang.haile_life.R
import kotlin.math.min

/**
 * Title :
 * Author: Lsy
 * Date: 2023/3/30 11:16
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
</desc></version></time></author> */
class CircleImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private val defaultStrokeWidth = 3
    private val defaultStrokeColor =
        ResourcesCompat.getColor(resources, R.color.dividing_line_color, null)
    private var strokeWidth = defaultStrokeWidth
    private var strokeColor = defaultStrokeColor
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView)
        strokeWidth = array.getDimensionPixelOffset(
            R.styleable.CircleImageView_strokeWidth,
            defaultStrokeWidth
        )
        strokeColor = array.getColor(R.styleable.CircleImageView_strokeColor, defaultStrokeColor)
        array.recycle()
        initPaint()
    }

    private fun initPaint() {
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = strokeWidth.toFloat()
        strokePaint.color = strokeColor
    }

    /**
     * 设置边框大小
     */
    fun setStrokeWidth(width: Float) {
        strokePaint.strokeWidth = width
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val width = measuredWidth
        val height = measuredHeight
        val radius = min(width, height)
        path.reset()
        path.addCircle(
            (width / 2).toFloat(),
            (height / 2).toFloat(),
            (radius / 2).toFloat(),
            Path.Direction.CW
        )
        canvas.clipPath(path)
        super.onDraw(canvas)
        if (0 < strokePaint.strokeWidth) {
            canvas.drawCircle(
                (width / 2).toFloat(),
                (height / 2).toFloat(),
                (radius / 2).toFloat(),
                strokePaint
            )
        }
    }
}
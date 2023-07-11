package com.yunshang.haile_life.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.yunshang.haile_life.R

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
class RoundImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    private val path = Path()
    var radius: Float

    init {
        context.obtainStyledAttributes(attrs, R.styleable.RoundImageView).let { array ->
            radius = array.getDimensionPixelOffset(R.styleable.RoundImageView_radius, 0).toFloat()
            array.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val width = measuredWidth
        val height = measuredHeight
        path.reset()
        path.addRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            radius,
            radius,
            Path.Direction.CW
        )
        canvas.clipPath(path)
        super.onDraw(canvas)
    }
}
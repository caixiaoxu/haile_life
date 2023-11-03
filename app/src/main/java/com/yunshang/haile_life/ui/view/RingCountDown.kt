package com.yunshang.haile_life.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.R

/**
 * Title :
 * Author: Lsy
 * Date: 2023/10/23 10:10
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class RingCountDown @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ringWidth = DimensionUtils.dip2px(context, 8f).toFloat()

    private val mPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = ringWidth
            strokeCap = Paint.Cap.ROUND
        }
    }


    private var cur:Int = 0
    private var total:Int = 0

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.RingCountDown)
        cur = array.getInteger(R.styleable.RingCountDown_progressVal, 0)
        total = array.getInteger(R.styleable.RingCountDown_totalVal, 0)
        array.recycle()
    }

    fun setData(total:Int,cur:Int){
        this.total = total
        this.cur = cur
        invalidate()
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        val rectF =
            RectF(ringWidth / 2, ringWidth / 2, width - ringWidth / 2, height - ringWidth / 2)
        mPaint.color = ContextCompat.getColor(context, R.color.secondColorPrimaryBg)
        canvas?.drawArc(rectF, 0f, 360f, false, mPaint)

        if (total > 0 && cur > 0 && cur < total){
            val angle = cur * 1.0f / total * 360
            val start = 360 - angle + -90
            mPaint.color = ContextCompat.getColor(context, R.color.secondColorPrimary)
            canvas?.drawArc(rectF, start, angle, false, mPaint)
        }
    }
}
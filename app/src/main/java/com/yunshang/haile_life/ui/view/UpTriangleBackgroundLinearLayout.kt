package com.yunshang.haile_life.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.children
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.R

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/24 15:51
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class UpTriangleBackgroundLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val dimen8 = DimensionUtils.dip2px(context, 8f)

    var triangleLR: Int
    var triangleMargin: Int

    private val mPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    init {
        val array =
            context.obtainStyledAttributes(attrs, R.styleable.UpTriangleBackgroundLinearLayout)
        triangleLR = array.getInt(R.styleable.UpTriangleBackgroundLinearLayout_triangle_lr, 1)
        triangleMargin = array.getDimensionPixelOffset(
            R.styleable.UpTriangleBackgroundLinearLayout_triangle_margin,
            dimen8 * 3
        )
        val triangleShadowW = array.getDimensionPixelOffset(
            R.styleable.UpTriangleBackgroundLinearLayout_triangle_shadow_w, 0
        )
        mPaint.color = array.getInt(
            R.styleable.UpTriangleBackgroundLinearLayout_bgColor,
            Color.WHITE
        )
//        if (triangleShadowW > 0){
//            setLayerType(View.LAYER_TYPE_SOFTWARE,mPaint)
//            mPaint.maskFilter = BlurMaskFilter(triangleShadowW.toFloat(), BlurMaskFilter.Blur.SOLID)
//        }
        array.recycle()
    }

    fun setContextBg(color: Int?) {
        color?.let {
            mPaint.color = color
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var childHeight = 0
        children.forEach {
            childHeight += it.measuredHeight
        }

        // 加上三角高度
        setMeasuredDimension(
            widthMeasureSpec, MeasureSpec.makeMeasureSpec(
                (childHeight + dimen8),
                MeasureSpec.EXACTLY
            )
        )
    }

    override fun dispatchDraw(canvas: Canvas?) {
        val path = Path()
        // 三角
        path.reset()
        path.moveTo(
            (if (0 == triangleLR) triangleMargin else measuredWidth - triangleMargin).toFloat(),
            0f
        )
        path.lineTo((if (0 == triangleLR) triangleMargin + dimen8 else measuredWidth - (triangleMargin + dimen8)).toFloat(), dimen8.toFloat())
        path.lineTo((if (0 == triangleLR) triangleMargin - dimen8 else  measuredWidth - (triangleMargin - dimen8)).toFloat(), dimen8.toFloat())
        canvas?.drawPath(path, mPaint)
        // 圆角矩形
        canvas?.drawRoundRect(
            0f,
            dimen8.toFloat(),
            measuredWidth.toFloat(),
            measuredHeight.toFloat(),
            dimen8.toFloat(),
            dimen8.toFloat(),
            mPaint
        )
        canvas?.save()
        canvas?.translate(0f, dimen8.toFloat())
        super.dispatchDraw(canvas)
        canvas?.restore()
    }
}
package com.yunshang.haile_life.ui.view.scan

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import com.journeyapps.barcodescanner.ViewfinderView
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.R
import timber.log.Timber

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/28 13:53
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CustomViewFinderView(context: Context, attrs: AttributeSet) :
    ViewfinderView(context, attrs) {
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mtb = DimensionUtils.dip2px(context, 120f)
    private val bitmapMatrix = Matrix()
    private var scanImg: Bitmap? = null
    private var scrollH = -1
    private val mScanAnim: ValueAnimator? by lazy {
        ValueAnimator.ofFloat(0f, 1f)
            .apply {
                duration = 2400
                repeatCount = -1
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
                addUpdateListener {
                    if (it.animatedValue is Float && 0 < scrollH) {
//                        Timber.i("移动y距离: ${it.animatedFraction * scrollH}")
                        bitmapMatrix.setTranslate(0f, it.animatedFraction * scrollH)
                        invalidate()
                    }
                }
            }
    }

    init {
        scanImg = BitmapFactory.decodeResource(context.resources, R.mipmap.icon_scan_img)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        scanImg?.let { img ->
            scrollH = measuredHeight - 2 * mtb
        }
        startScanAnim()
    }

    override fun onDraw(canvas: Canvas?) {
        scanImg?.let { img ->
            canvas?.let {
                canvas.save()
                canvas.clipRect(0, mtb, measuredWidth, measuredHeight - mtb)
                canvas.drawBitmap(img, bitmapMatrix, mPaint)
                canvas.restore()
            }
        }
    }

    private fun startScanAnim() {
        mScanAnim?.takeIf { !it.isRunning }?.start()
    }

    fun stopScanAnimAndReset() {
        mScanAnim?.takeIf { it.isRunning }?.cancel()
    }
}
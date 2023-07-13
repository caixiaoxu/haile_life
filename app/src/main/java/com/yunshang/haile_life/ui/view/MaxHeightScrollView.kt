package com.yunshang.haile_life.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import com.yunshang.haile_life.R

/**
 * Title : 含最大高度的ScrollView
 * Author: Lsy
 * Date: 2023/4/17 17:30
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class MaxHeightScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScrollView(context, attrs) {

    var maxHeight: Int = -1

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.MaxHeightScrollView)
        maxHeight = array.getDimensionPixelOffset(R.styleable.MaxHeightScrollView_maxHeight, -1)
        array.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (0 < maxHeight) {
            if (measuredHeight > 0 && measuredHeight > maxHeight) {
                setMeasuredDimension(measuredWidth, maxHeight)
            }
        }
    }
}
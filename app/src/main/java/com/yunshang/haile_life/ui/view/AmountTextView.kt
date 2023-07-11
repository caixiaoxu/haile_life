package com.yunshang.haile_life.ui.view

import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.yunshang.haile_life.R

/**
 * Title :
 * Author: Lsy
 * Date: 2023/5/27 09:39
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AmountTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    init {
        // 设置金额专属字体
        try {
            ResourcesCompat.getFont(context, R.font.money)?.let {
                typeface = it
            }
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }
    }
}
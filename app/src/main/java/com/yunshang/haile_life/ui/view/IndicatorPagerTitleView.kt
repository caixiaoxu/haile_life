package com.yunshang.haile_life.ui.view

import android.content.Context
import android.graphics.Typeface
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/8 14:25
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class IndicatorPagerTitleView(context: Context?) : SimplePagerTitleView(context) {

    var normalFontSize: Float = 16f
    var selectFontSize: Float = 18f

    override fun onSelected(index: Int, totalCount: Int) {
        super.onSelected(index, totalCount)

        textSize = selectFontSize
        typeface = Typeface.defaultFromStyle(Typeface.BOLD)
    }

    override fun onDeselected(index: Int, totalCount: Int) {
        super.onDeselected(index, totalCount)
        textSize = normalFontSize
        typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
    }
}
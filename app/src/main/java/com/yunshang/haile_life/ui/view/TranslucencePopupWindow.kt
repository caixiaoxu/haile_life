package com.yunshang.haile_life.ui.view

import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow


/**
 * Title : 半透明背景popupwindow
 * Author: Lsy
 * Date: 2023/7/24 18:13
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class TranslucencePopupWindow(
    contentView: View?,
    private val window: Window,
    width: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    height: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    alpha: Float = 0.4f
) :
    PopupWindow(contentView, width, height) {

    init {
        isTouchable = true
        isFocusable = true
        isOutsideTouchable = true
        backgroundAlpha(alpha)

        setOnDismissListener {
            backgroundAlpha(1f)
        }
    }

    /**
     * 设置背景透明
     */
    private fun backgroundAlpha(alpha: Float) {
        window.attributes.let { windowAttr ->
            windowAttr.alpha = alpha
            if (alpha == 1f) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) //不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) //此行代码主要是解决在华为手机上半透明效果无效的bug
            }
            window.attributes = windowAttr
        }
    }


    /**
     * 计算出PopUpWindow显示的高度
     * @return PopUpWindow的高度
     */
    private fun calculatePopWindowHeight(): Int {
        val screenWidth = contentView.context.resources.displayMetrics.widthPixels
        //若popupWindow高度已被定义,可直接返回
        if (height > 0) {
            return height
        }
        val popupWindowWidth = width
        //根据popupWindow的宽度来选择PopUpWindow的测量模式
        val measureWidthParams = when {
            popupWindowWidth > 0 -> {
                View.MeasureSpec.makeMeasureSpec(popupWindowWidth, View.MeasureSpec.EXACTLY)
            }
            popupWindowWidth == WindowManager.LayoutParams.MATCH_PARENT -> {
                View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY)
            }
            popupWindowWidth == WindowManager.LayoutParams.WRAP_CONTENT -> {
                //若popupWindow的宽度为WRAP_CONTENT，测量会存在偏差。因此需要设置PopUpWindow的宽度为屏幕宽度的一半，测试模式为精准测量
                width = screenWidth / 2
                View.MeasureSpec.makeMeasureSpec(screenWidth / 2, View.MeasureSpec.EXACTLY)
            }
            else -> 0
        }
        contentView.measure(
            measureWidthParams,
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return contentView.measuredHeight
    }

    /**
     * 显示PopUpWindow在参考布局之上
     * @param anchorView 参考布局
     */
    fun showAsAbove(anchorView: View?, xOff: Int = 0, yOff: Int = 0) {
        val popupWindowHeight = calculatePopWindowHeight()
        val anchorViewHeight = anchorView?.height ?: 0
        //实际需要偏移的高度为参考view的高度加上PopUpWindow的高度
        val yOffset = -(popupWindowHeight + anchorViewHeight)
        this.showAsDropDown(anchorView, xOff, yOffset + yOff)
    }
}
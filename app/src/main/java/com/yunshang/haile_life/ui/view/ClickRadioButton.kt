package com.yunshang.haile_life.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatRadioButton

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/23 12:01
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class ClickRadioButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatRadioButton(context, attrs) {

    private var onRadioClickListener: ((v: View) -> Boolean)? = null
    fun setOnRadioClickListener(onRadioClickListener: (v: View) -> Boolean) {
        this.onRadioClickListener = onRadioClickListener
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            if (true == onRadioClickListener?.invoke(this)) {
                return true
            }
        }
        return super.onTouchEvent(ev)
    }
}
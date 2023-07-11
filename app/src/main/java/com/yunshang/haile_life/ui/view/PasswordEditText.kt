package com.yunshang.haile_manager_android.ui.view

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import java.util.regex.Pattern

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/7 10:20
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class PasswordEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    init {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        maxLines = 1
        filters = arrayOf(
            InputFilter.LengthFilter(12),
            object : InputFilter {
                override fun filter(
                    source: CharSequence,// source:当前输入的字符
                    start: Int,// start:输入字符的开始位置
                    end: Int,// end:输入字符的结束位置
                    dest: Spanned,// dest：当前已显示的内容
                    dStart: Int,// dstart:当前光标开始位置
                    dEnd: Int// dent:当前光标结束位置
                ): CharSequence {
                    val p = Pattern.compile("[A-Za-z\\d]+")
                    val m = p.matcher(source.toString())
                    if (!m.matches()) return ""
                    return source
                }
            }
        )
    }
}
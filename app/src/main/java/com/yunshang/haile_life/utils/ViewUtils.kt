package com.yunshang.haile_life.utils

import android.graphics.Color
import android.text.InputFilter
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.yunshang.haile_life.R

/**
 * Title : 控件的工具类
 * Author: Lsy
 * Date: 2023/4/7 09:59
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object ViewUtils {

    /**
     * 给textView设置spannable
     */
    fun initAgreementToTextView(textView: TextView, onClick: OnClickListener) {
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
        textView.text =
            SpannableString(textView.context.resources.getString(R.string.login_agreement)).apply {
                setSpan(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            textView.context,
                            R.color.colorPrimary
                        )
                    ),
                    length-7,
                    length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    object : ClickableSpan() {
                        override fun onClick(view: View) {
                            onClick.onClick(view)
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            //去掉下划线
                            ds.isUnderlineText = false
                        }
                    }, length-7,
                    length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
    }

    /**
     * 设置edittext 金额输入限制
     * @param editText
     * @param maxInputLen 输入金额的最大整数位数
     * @param maxPointLen 输入金额的最大小数位数
     */
    fun inputAmountLimit(editText: EditText, maxInputLen: Int = 4, maxPointLen: Int = 2) {
        editText.filters = arrayOf(
            InputFilter { source, _, _, dest, dstart, dend ->
                // source:当前输入的字符
                // start:输入字符的开始位置
                // end:输入字符的结束位置
                // dest：当前已显示的内容
                // dstart:当前光标开始位置
                // dent:当前光标结束位置

                val font = dest.subSequence(0, dstart) // 替换位置前段字符串
                val back = dest.subSequence(dend, dest.length) // 替换位置后段字符串
                val target = font.toString() + source + back // 替换成功之后的字符串
                val backup = dest.subSequence(dstart, dend) // 将要被替换的字符串

                if (!target.contains(".") && target.length > maxInputLen) {
                    return@InputFilter backup
                }
                if (target.contains(".") && target.length > (maxInputLen + maxPointLen + 1)) {
                    return@InputFilter backup
                }
                // 只允许输入一个"."
                if ((source == ".") && dest.subSequence(0, dest.length).toString().contains(".")) {
                    return@InputFilter backup
                }
                // 不允许首字符为"."
                if (target.startsWith(".")) {
                    return@InputFilter if (dest.subSequence(0, dest.length).toString()
                            .contains(".")
                    ) "0"
                    else "0."
                }
                // 不允许0开头，但非0.1或单独0的情况
                if (target.startsWith("0") && !target.startsWith("0.") && !("0" == target)) {
                    return@InputFilter backup
                }
                // 限制小数点后两位
                val index = target.indexOf(".")
                if (index >= 0 && index + 2 + maxPointLen <= target.length) {
                    return@InputFilter backup
                }

                return@InputFilter source
            }
        )
    }

    /**
     * 刷新列表的子View
     */
    fun <T> refreshLinearLayoutChild(
        linearLayout: LinearLayout,
        arr: Array<T>,
        layoutParams: ViewGroup.LayoutParams? = null,
        createChildView: (item: T) -> View,
    ) {
        if (linearLayout.childCount > 0) {
            linearLayout.removeAllViews()
        }
        arr.forEach {
            linearLayout.addView(
                createChildView(it),
                layoutParams ?: ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }
}
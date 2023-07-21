package com.yunshang.haile_life.data.entities

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.lsy.framelib.data.constants.Constants
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.rule.IMultiTypeEntity
import com.yunshang.haile_life.utils.string.StringUtils

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/12 10:44
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class StarfishRefundRecordEntity(
    val createTime: String,
    val id: Int,
    val refundPrice: Double,
    val state: Int,//0—申请中，1—退款成功, 2—退款失败, 3—申请驳回
    val stateDesc: String
) : IMultiTypeEntity {

    fun getRefundAmountVal(): SpannableString {
        val prefix = com.lsy.framelib.utils.StringUtils.getString(R.string.refund_amount) + "："
        val content =
            prefix + refundPrice.toString() + com.lsy.framelib.utils.StringUtils.getString(R.string.unit_yuan)
        return StringUtils.formatMultiStyleStr(
            content,
            arrayOf(
                StyleSpan(Typeface.BOLD)
            ),
            prefix.length, content.length
        )
    }

    override fun getMultiType(): Int = when (state) {
        0 -> 1
        1 -> 0
        else -> 2
    }

    override fun getMultiTypeBgRes(): IntArray = intArrayOf()

    override fun getMultiTypeTxtColors(): IntArray = intArrayOf(
        ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_black_65),
        ContextCompat.getColor(Constants.APP_CONTEXT, R.color.secondColorPrimary),
        ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_ff630e),
    )
}
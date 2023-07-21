package com.yunshang.haile_life.data.entities

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/10 17:26
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class RechargeStarfishDetailEntity(
    val afterAmount: Int,
    val amount: Int,
    val createTime: String,
    val id: Int,
    val orderNo: String,
    val presentAmount: Int,
    val principalAmount: Int,
    val remark: String,
    val shopId: Int,
    val subType: Int,
    val type: Int,
    val userId: Int,
    val typeDesc: String,
) {
    private val moneyUnit: String
        get() = if (100 == type) "+" else "-"

    fun reachVal(): SpannableString {
        val prefix = StringUtils.getString(R.string.recharge_starfish)
        val content = "$prefix$moneyUnit$principalAmount"
        return if (100 == type) com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            content, arrayOf(
                ForegroundColorSpan(
                    ContextCompat.getColor(Constants.APP_CONTEXT, R.color.secondColorPrimary)
                )
            ), prefix.length, content.length
        ) else SpannableString(content)
    }

    fun rewardVal(): String =
        "${StringUtils.getString(R.string.reward_starfish)}$moneyUnit$presentAmount"
}
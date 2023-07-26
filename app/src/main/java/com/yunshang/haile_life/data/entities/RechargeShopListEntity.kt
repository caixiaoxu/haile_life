package com.yunshang.haile_life.data.entities

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/10 16:17
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class RechargeShopListEntity(
    val items: MutableList<RechargeShopListItem>
)

data class RechargeShopListItem(
    val createTime: String,
    val id: Int,
    val presentAmount: Long,
    val principalAmount: Long,
    val shopId: Int,
    val shopName: String,
    val totalAmount: Long,
    val userId: Int
) {
    fun getReachVal(): SpannableString {
        val prefix = StringUtils.getString(R.string.recharge_starfish)
        val content = "$prefix $principalAmount"
        return com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            "$prefix $principalAmount",
            arrayOf(
                AbsoluteSizeSpan(
                    DimensionUtils.sp2px(12f),
                ),
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        Constants.APP_CONTEXT, R.color.color_black_45
                    )
                ),
                StyleSpan(Typeface.NORMAL)
            ), 0, prefix.length
        )
    }

    fun getRewardVal(): SpannableString {
        val prefix = StringUtils.getString(R.string.reward_starfish)
        val content = "$prefix $presentAmount"
        return com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            content,
            arrayOf(
                AbsoluteSizeSpan(
                    DimensionUtils.sp2px(12f),
                ),
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        Constants.APP_CONTEXT, R.color.color_black_45
                    )
                ),
                StyleSpan(Typeface.NORMAL)
            ),
            0, prefix.length,
        )
    }
}
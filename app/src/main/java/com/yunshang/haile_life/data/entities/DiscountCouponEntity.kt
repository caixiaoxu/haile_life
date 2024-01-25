package com.yunshang.haile_life.data.entities

import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import androidx.core.content.ContextCompat
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.rule.IMultiTypeEntity
import com.yunshang.haile_life.utils.DateTimeUtils

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/13 09:58
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class DiscountCouponEntity(
    val activateTime: String,
    val endAt: String,
    val id: Int,
    val onsetTime: String,
    val productId: Int,
    val promotion: Promotion,
    val promotionId: Int,
    val startAt: String,
    val state: Int,
    val tag: String,
    val usedTime: String,
    val usedTradeNo: String,
    val userId: Int,
    val validityTime: String
) : IMultiTypeEntity {

    fun cutOffVal(): String = when (state) {
        1 -> DateTimeUtils.formatDateFromString(endAt)?.let { endDate ->
            val num = (endDate.time - System.currentTimeMillis()) / 1000 / 3600 / 24
            StringUtils.getString(R.string.cutoff_prompt, if (num < 1) 1 else num)
        } ?: ""

        30 -> StringUtils.getString(R.string.used)
        else -> StringUtils.getString(R.string.expired)
    }

    fun cutOffShow(): Boolean = when (state) {
        1 -> DateTimeUtils.formatDateFromString(endAt)?.let { endDate ->
            ((endDate.time - System.currentTimeMillis()) / 1000 / 3600 / 24) <= 7
        } ?: false

        30 -> true
        else -> true
    }

    fun indateVal(): String = "${
        DateTimeUtils.formatDateTimeForStr(
            onsetTime,
            "yyyy/MM/dd"
        )
    }-${DateTimeUtils.formatDateTimeForStr(validityTime, "yyyy/MM/dd")}"

    override fun getMultiType(): Int = when (state) {
        1 -> 0
        else -> 1
    }

    override fun getMultiTypeBgRes(): IntArray = intArrayOf(
        R.drawable.shape_s19ff630e_r4,
        R.drawable.shape_s0c000000_r4
    )

    override fun getMultiTypeTxtColors(): IntArray = intArrayOf(
        ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_ff630e),
        ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_black_45),
    )
}

data class Promotion(
    val activateNum: Int,
    val activateStartDay: Int,
    val activateTime: Int,
    val activateTimeType: Int,
    val blockedShopIds: List<Any>,
    val blockedShopVOs: List<BlockedShopVO>,
    val couponStatus: Int,
    val couponType: Int,
    val createTime: String,
    val dayOfWeek: List<Any>,
    val deleteFlag: Int,
    val endAt: String,
    val feeShareDetails: List<FeeShareDetail>,
    val feeShareType: Int,
    val goodsCategoryIds: List<Int>,
    val goodsCategoryNames: List<String>,
    val hourMinuteEndTime: String,
    val hourMinuteStartTime: String,
    val isCalculateTimeFromActivation: Boolean,
    val isFree: Boolean,
    val isFreeItem: Boolean,
    val lastEditor: String,
    val limitedChannelIds: List<Int>,
    val maxDiscountPrice: String,
    val needPay: String,
    val needPayItem: Int,
    val operatorIds: List<Int>,
    val operatorVOs: List<OperatorVO>,
    val orderReachPrice: String,
    val organizationType: Int,
    val percentage: Int,
    val promotionId: Int,
    val reduce: String,
    val reduceItem: Int,
    val shopIds: List<Int>,
    val shopNames: List<String>,
    val shopVOs: List<ShopVO>,
    val specifiedPrice: Double,
    val startAt: String,
    val stock: Int,
    val title: String,
    val unlimitedStock: Boolean,
    val unlimitedUserActivation: Boolean,
    val userActivationLimit: Int
) {
    fun discountCouponValue(): SpannableString = when (couponType) {
        1 -> "¥$reduce".let { formatValue(it, 1, it.length) }
        3 -> "${percentage}折".let { formatValue(it, 0, it.length - 1) }
        4 -> if (0.0 == specifiedPrice) "免".let {
            formatValue(
                it,
                0,
                it.length
            )
        } else "¥$specifiedPrice".let { formatValue(it, 1, it.length) }

        else -> SpannableString("")
    }

    fun cutOffVal(): String = if (isCalculateTimeFromActivation) StringUtils.getString(
        R.string.cutoff_prompt,
        activateTime
    ) else DateTimeUtils.formatDateFromString(endAt)?.let { endDate ->
        val num = (endDate.time - System.currentTimeMillis()) / 1000 / 3600 / 24
        if (num < 0) {
            "已过期"
        } else {
            StringUtils.getString(R.string.cutoff_prompt, if (num < 1) 1 else num)
        }
    } ?: ""

    private fun formatValue(content: String, start: Int, end: Int): SpannableString {
        val index = content.indexOf(".")
        val e = if (index == -1) {
            end
        } else {
            index
        }
        return com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            content, arrayOf(
                AbsoluteSizeSpan(DimensionUtils.sp2px(32f))
            ), start, e
        )
    }

    fun discountCouponTitle(): String =
        StringUtils.getStringArray(R.array.discount_coupon_type)[couponType]

    fun limitDesc(): String =
        when (couponType) {
            1 -> "满${orderReachPrice}元可用"
            3 -> "最高抵扣${maxDiscountPrice}元"
            4 -> if (0.0 == specifiedPrice) "全额减免" else "本次下单只需要支付${specifiedPrice}元"
            else -> ""
        }

    fun useRuleVal(): String = "1.可用时段：${hourMinuteStartTime}-${hourMinuteEndTime}\n2${
        if (organizationType == 1) "适用于全部店铺" else "适用店铺：${shopNames.joinToString(" ")}"
    }\n3.${
        if (goodsCategoryNames.isEmpty()) "不限设备" else "适用于：${
            goodsCategoryNames.joinToString(
                " "
            )
        }"
    }"
}

data class BlockedShopVO(
    val operatorPhone: String,
    val shopAddress: String,
    val shopId: Int,
    val shopName: String
)

data class FeeShareDetail(
    val feeShareType: Int,
    val percentage: Int
)

data class OperatorVO(
    val account: String,
    val accountId: Int,
    val amount: String,
    val createTime: String,
    val headImage: String,
    val id: Int,
    val organizationId: Int,
    val organizationType: Int,
    val `property`: Int,
    val realName: String,
    val status: Int,
    val tagName: String
)

data class ShopVO(
    val operatorPhone: String,
    val shopAddress: String,
    val shopId: Int,
    val shopName: String
)
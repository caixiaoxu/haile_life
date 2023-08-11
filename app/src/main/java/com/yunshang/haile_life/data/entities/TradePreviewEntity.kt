package com.yunshang.haile_life.data.entities

import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.utils.DateTimeUtils

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/3 14:25
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class TradePreviewEntity(
    val discountPrice: String,
    val itemList: List<TradePreviewGoodItem>,
    val originPrice: String,
    val promotionList: List<TradePreviewPromotion>,
    val realPrice: String
) {
    fun isZero(): Boolean = try {
        realPrice.toDouble() == 0.0
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

data class TradePreviewGoodItem(
    val discountAmount: String,
    val goodsCategoryId: Int,
    val goodsCategoryCode: String,
    val goodsId: Int,
    val goodsItemId: Int,
    val goodsItemName: String,
    val goodsName: String,
    val num: Int,
    val originAmount: String,
    val originUnitAmount: String,
    val realAmount: String,
    val realUnitAmount: String,
    val shopId: Int
) {
    fun getCategoryIcon(): Int =
        if (goodsCategoryCode.isNullOrEmpty()) 0 else when (goodsCategoryCode) {
            DeviceCategory.Washing -> R.mipmap.icon_order_wash
            DeviceCategory.Dryer -> R.mipmap.icon_order_dryer
            DeviceCategory.Shoes -> R.mipmap.icon_order_shoes
            DeviceCategory.Hair -> R.mipmap.icon_order_hair
            else -> 0
        }

    fun getOriginAmountStr(): String =
        com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(originAmount) ?: ""

    fun getDiscountAmountStr(): String =
        com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr("-$discountAmount")
            ?: ""

    fun getRealAmountStr(): String =
        com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(realAmount) ?: ""
}

data class TradePreviewPromotion(
    val available: Boolean,
    val discountPrice: String,
    val options: List<TradePreviewParticipate>,
    val originPrice: String,
    val participateList: List<TradePreviewParticipate>,
    val promotionProduct: Int,//1-限时特惠，2-商家优惠券，3-折扣卡，4-平台优惠券，5-海星折扣
    val realPrice: String,
    val used: Boolean,
    val forceUse:Boolean,//是否强制使用
) {
    fun getDiscountTitle(): String = StringUtils.getString(
        when (promotionProduct) {
            1 -> R.string.limited_time_offer
            2 -> R.string.shop_coupon
            4 -> R.string.platform_coupon
            5 -> R.string.starfish_discount
            else -> R.string.order_discount_coupon
        }
    )

    fun getDiscountIcon(): Int = when (promotionProduct) {
        1 -> R.mipmap.icon_order_limited_time_offer
        5 -> R.mipmap.icon_order_starfish
        else -> R.mipmap.icon_order_discount_coupon
    }
}

data class TradePreviewParticipate(
    val promotionId: Int,
    val promotionProduct: Int,
    val assetId: Int,
    val shopId: Int,
    val available: Boolean,
    val discountAmount: String,
    val originAmount: String,
    val realAmount: String,
    val used: Boolean,
    val forceUse:Boolean,//是否强制使用
    val promotionDetail: TradePreviewPromotionDetail
) {
    var isCheck = false

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is TradePreviewParticipate) return false
        if (other.promotionId == promotionId && other.assetId == assetId && other.shopId == shopId) return true
        return super.equals(other)
    }
}

data class TradePreviewPromotionDetail(
//    1-满减reduce 3-折扣percentage 4-体验specifiedPrice
    //满减
    val couponActivationAssetId: Int,
    val couponId: Int,
    val couponType: Int,
    val endAt: String,
    val hourMinuteEndTime: String,
    val hourMinuteStartTime: String,
    val isParticipate: Boolean,
    val machineParentTypeIds: List<String>,
    val machineParentTypeNames: List<String>,
    val orderReachPrice: String,
    val orgIds: List<Int>,
    val organizationType: Int,
    val reduce: String,
    val shopNames: List<String>,
    val startAt: String,

    //折扣
    val maxDiscountPrice: String,
    val percentage: String,

    // 体验
    val specifiedPrice: String,
) {
    fun discountCouponTitle(): String =
        StringUtils.getStringArray(R.array.discount_coupon_type)[couponType]

    fun discountCouponValue(): SpannableString = when (couponType) {
        1 -> "¥ ${
            com.yunshang.haile_life.utils.string.StringUtils.checkAmountStrIsIntOrDouble(
                reduce
            )
        }".let { formatValue(it, 2, it.length) }
        3 -> "${
            com.yunshang.haile_life.utils.string.StringUtils.checkAmountStrIsIntOrDouble(
                percentage
            )
        }折".let {
            formatValue(it, 0, it.length - 1)
        }
        4 -> "¥ ${
            com.yunshang.haile_life.utils.string.StringUtils.checkAmountStrIsIntOrDouble(
                specifiedPrice
            )
        }".let { formatValue(it, 2, it.length) }
        else -> SpannableString("")
    }

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

    fun limitDesc(): String =
        when (couponType) {
            1 -> "满${
                com.yunshang.haile_life.utils.string.StringUtils.checkAmountStrIsIntOrDouble(
                    orderReachPrice
                )
            }可用"
            3 -> "最高抵扣${
                com.yunshang.haile_life.utils.string.StringUtils.checkAmountStrIsIntOrDouble(
                    maxDiscountPrice
                )
            }元"
            4 -> if (specifiedPrice.isEmpty()) "全额减免" else "额外支付金额"
            else -> ""
        }

    fun dealLineDateStr(): String = DateTimeUtils.formatDateTimeForStr(endAt, "yyyy/MM/dd") + "到期"
}
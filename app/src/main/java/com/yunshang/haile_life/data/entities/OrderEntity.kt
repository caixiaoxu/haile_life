package com.yunshang.haile_life.data.entities

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.rule.IMultiTypeEntity
import com.yunshang.haile_life.utils.DateTimeUtils
import java.util.*

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/4 18:32
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class OrderEntity(
    val appointmentReason: String,
    val appointmentState: Int,
    val appointmentTime: String,
    val appointmentUsageTime: String,
    val buyerId: Int,
    val buyerPhone: String,
    val createTime: String,
    val hasFeedback: Boolean,
    val id: Int,
    val invalidTime: String,
    val invalidTimeStamp: Int,
    val orderCategory: Int,
    val orderItemList: List<OrderItem>,
    val orderNo: String,
    val orderSubCategory: Int,
    val orderSubType: Int,
    val orderType: String,
    val organizationId: Int,
    val originPrice: String,
    val payAmount: String,
    val promotionParticipationList: List<PromotionParticipation>,
    val realPrice: String,
    val sellerId: Int,
    val serviceTelephone: String,
    val state: Int,
    val stateDesc: String,
    val viewReply: Boolean,
) : IMultiTypeEntity {
    override fun getMultiType(): Int = when (state) {
        100, 500 -> 0
        1000, 2099 -> 2
        else -> 1
    }

    fun getMultiType(isAppoint: Boolean): Int = if (isAppoint) {
        when (appointmentState) {
            0, 1, 2 -> 0
            3, 4 -> 2
            else -> 1
        }
    } else getMultiType()

    override fun getMultiTypeBgRes(): IntArray? = null

    override fun getMultiTypeTxtColors(): IntArray = intArrayOf(
        ContextCompat.getColor(
            Constants.APP_CONTEXT,
            R.color.colorPrimary
        ),
        ContextCompat.getColor(
            Constants.APP_CONTEXT,
            R.color.color_ff630e
        ),
        ContextCompat.getColor(
            Constants.APP_CONTEXT,
            R.color.color_black_65
        ),
    )

    fun getOrderStatusTitle(): String = when (state) {
        500 -> orderItemList.firstOrNull()
            ?.let { DeviceCategory.categoryName(it.categoryCode).replace("机", "中") } ?: ""
        else -> stateDesc
    }

    fun getOrderDetailFinishTimePrompt(): SpannableString = when (state) {
        50 -> com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            "先使用，订单结束后根据用水量扣费",
            arrayOf(
                ForegroundColorSpan(
                    ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_ff630e)
                )
            ),
        )
        100 -> if (DeviceCategory.isDrinking(orderItemList.firstOrNull()?.categoryCode)) com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            "请尽快完成支付",
            arrayOf(
                ForegroundColorSpan(
                    ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_ff630e)
                )
            ),
        ) else SpannableString("订单待付款")
        401 -> SpannableString("订单超时关闭，请重新下单～")
        411 -> SpannableString("订单已取消，请重新下单～")
        500 -> StringUtils.getString(R.string.predict_finish_time).let { prefix ->
            (DateTimeUtils.formatDateFromString(orderItemList.firstOrNull()?.finishTime)?.let {
                val calendar = Calendar.getInstance().apply {
                    time = it
                }
                "$prefix ${StringUtils.getString(if (0 == calendar.get(Calendar.AM_PM)) R.string.am else R.string.pm)}${
                    DateTimeUtils.formatDateTime(
                        calendar.time,
                        "HH点mm分"
                    )
                }"
            } ?: "").let { content ->
                if (content.isNotEmpty()) {
                    com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
                        content,
                        arrayOf(
                            ForegroundColorSpan(
                                ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_ff630e)
                            )
                        ),
                        prefix.length, content.length
                    )
                } else SpannableString("")
            }
        }
        1000 -> SpannableString("订单已完成，祝您生活愉快～")
        2000 -> SpannableString("订单退款中，请耐心等待～")
        2099 -> SpannableString("订单已退款，祝您生活愉快～")
        else -> SpannableString("祝您生活愉快～")
    }

    fun getOrderDetailAppointTimePrompt(): SpannableString = when (appointmentState) {
        0 -> SpannableString("订单待付款")
        1 -> {
            val useDate = DateTimeUtils.formatDateFromString(appointmentUsageTime)
            val time = if (DateTimeUtils.isSameDay(useDate, Date())) {
                "今天${DateTimeUtils.formatDateTime(useDate, "HH:mm")}"
            } else appointmentTime
            val content = StringUtils.getString(
                R.string.order_submit_used_time_prompt,
                time
            )
            com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
                content, arrayOf(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            Constants.APP_CONTEXT, R.color.color_ff630e
                        )
                    )
                ), 3, 3 + time.length
            )
        }
        2 -> SpannableString("订单已完成，祝您生活愉快～")
        3 -> SpannableString("订单已失效，请重新下单～")
        4 -> SpannableString("订单已取消，祝您生活愉快～")
        else -> SpannableString("祝您生活愉快～")
    }

    fun getOrderDeviceName(): String =
        if (orderItemList.isNotEmpty()) orderItemList.first().goodsName else ""

    fun getOrderDeviceModel(): String = orderItemList.firstOrNull()?.let {
        if (DeviceCategory.isDrinking(it.categoryCode)) {
            orderItemList.joinToString(",") { item -> item.goodsItemName }
        } else "${it.goodsItemName} x1"
    } ?: ""

    fun getGoodInfo(): String = if (orderItemList.isNotEmpty()) orderItemList.first()
        .run { "${shopName}\n${area}${address}\n${goodsName}" } else ""

    fun getOrderDiscountTotalPrice(): String = try {
        com.yunshang.haile_life.utils.string.StringUtils.formatAmountStr(originPrice.toDouble() - payAmount.toDouble())
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    fun drinkingOverTime(): String = orderItemList.firstOrNull()?.goodsItemInfo?.overTime ?: ""
    fun drinkingPauseTime(): String = orderItemList.firstOrNull()?.goodsItemInfo?.pauseTime ?: ""
}

data class OrderItem(
    val address: String,
    val area: String,
    val categoryCode: String,
    val finishTime: String,
    val goodsId: Int,
    val goodsItemId: Int,
    val goodsItemName: String,
    val goodsName: String,
    val id: Int,
    val num: Int,
    val orderNo: String,
    val originPrice: String,
    val realPrice: String,
    val shopName: String,
    val unit: String,
    val goodsItemExtAttr: String,
    val originUnitPrice: String,
    val realUnitPrice: String,
) {
    val goodsItemInfo: GoodsItemInfoEntity?
        get() = if (DeviceCategory.isDrinking(categoryCode)) GsonUtils.json2Class(
            goodsItemExtAttr,
            GoodsItemInfoEntity::class.java
        ) else null

    fun getOrderDeviceUnit(state: Int): String = if (DeviceCategory.isDrinking(categoryCode)) {
        if (1 == goodsItemInfo?.priceCalculateMode) "${originUnitPrice}元/ml${if (50 != state) " X ${unit}/ml" else ""}" else "${originUnitPrice}元/s${if (50 != state) " X ${unit}/s" else ""}"
    } else "${unit}分钟"

    fun getOrderDeviceOriginPrice(state: Int): String = if (50 == state) ""
    else
        "${
            com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(
                originPrice
            )
        }"
}

data class GoodsItemInfoEntity(
    val priceCalculateMode: Int,
    val overTime: String,
    val pauseTime: String,
    val priceCalculateUnit: String,
    val singlePulseQuantity: String,
    val waterTypeId: Int
)

data class PromotionParticipation(
    val discountPrice: String,
    val promotionProduct: Int,
    val promotionProductName: String
)
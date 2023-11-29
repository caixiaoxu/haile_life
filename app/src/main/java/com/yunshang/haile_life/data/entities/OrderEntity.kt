package com.yunshang.haile_life.data.entities

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.google.gson.annotations.SerializedName
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.extend.toDefaultDouble
import com.yunshang.haile_life.data.extend.toRemove0Str
import com.yunshang.haile_life.data.rule.IMultiTypeEntity
import com.yunshang.haile_life.utils.DateTimeUtils
import java.util.*
import kotlin.math.ceil

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
    var state: Int,
    val stateDesc: String,
    val viewReply: Boolean,
    val payTime: String? = null,
    val reserveAutoRefund: Int? = null,
    val timesOfRestart: Int? = null,
    val checkInfo: CheckInfo? = null,
    val discountPrice: Double? = null,
    val reserveInfo: ReserveInfo? = null,
    val tipRemark: String? = null,
    val refundTag: String? = null,
    val refundTime: String? = null,
    val refundCouponTime: String? = null,
    val redirectWorking: Boolean? = false,
    val fulfillInfo: FulfillInfo? = null
) : BaseObservable(), IMultiTypeEntity {

    fun showDiscount(): Boolean = orderItemList.size > 1 &&
            try {
                discountPrice?.let { it > 0 } ?: false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

    val isNormalOrder: Boolean
        get() = state >= 1000 || state in 400 until 500

    @Transient
    @get:Bindable
    var showDetail: Boolean = false
        set(value) {
            field = value
            notifyPropertyChanged(BR.showDetail)
        }

    override fun getMultiType(): Int = if ("300" == orderType)
        when (appointmentState) {
            0, 1, 2 -> 0
            3, 4 -> 2
            else -> 1
        }
    else
        when (state) {
            100, 500 -> 0
            1000, 2099 -> 2
            else -> 1
        }

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

    fun getOrderStatusTitle(): String =
//        if ("300" == orderType)
//        OrderStatus.getAppointStateName(appointmentState, false)
//    else if (500 == state) {
//        orderItemList.firstOrNull()
//            ?.let { DeviceCategory.categoryName(it.categoryCode).replace("机", "中") } ?: ""
//    } else
        stateDesc

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
        500 -> if (DeviceCategory.isWashingOrShoes(orderItemList.firstOrNull()?.categoryCode) || DeviceCategory.isDryer(
                orderItemList.firstOrNull()?.categoryCode
            )
        ) calculateRemnantTime()
        else StringUtils.getString(R.string.predict_finish_time).let { prefix ->
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
                                ContextCompat.getColor(
                                    Constants.APP_CONTEXT,
                                    R.color.color_ff630e
                                )
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

    fun calculateRemnantTime() =
        StringUtils.getString(R.string.predict_remnant_time).let { prefix ->
            ((DateTimeUtils.formatDateFromString(orderItemList.firstOrNull()?.finishTime)?.let {
                val diff = it.time - System.currentTimeMillis()
                "$prefix ${if (diff <= 0) "即将完成" else "${ceil(diff * 1.0 / 1000 / 60).toInt()}分钟"}"
            } ?: "$prefix") + " 图").let { content ->
                if (content.isNotEmpty()) {
                    SpannableString(content).apply {
                        setSpan(
                            ForegroundColorSpan(
                                ContextCompat.getColor(
                                    Constants.APP_CONTEXT,
                                    R.color.color_ff630e
                                )
                            ), prefix.length, content.length - 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            object : ClickableSpan() {
                                override fun onClick(v: View) {
                                    LiveDataBus.post(BusEvents.PROMPT_POPUP, true)
                                }
                            }, content.length - 1,
                            content.length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            ImageSpan(Constants.APP_CONTEXT, R.mipmap.icon_order_desc_prompt),
                            content.length - 1,
                            content.length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                } else SpannableString("")
            }
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

    fun getOrderPositionName(): String =
        if (orderItemList.isNotEmpty()) orderItemList.first().positionName else ""

    fun getOrderDeviceModel(): String = (orderItemList.firstOrNull()?.let {
        if (DeviceCategory.isDrinking(it.categoryCode)) {
            orderItemList.joinToString(",") { item -> item.goodsItemName }
        } else "${it.goodsItemName} x1"
    } ?: "") + orderItemList.filter { item -> DeviceCategory.isDispenser(item.categoryCode) }
        .let { list ->
            if (list.isNotEmpty()) {
                "\n" + list.joinToString(" + ") { item -> "${item.goodsItemName}${item.num}ml" }
            } else ""
        }

    fun getGoodInfo(): String = if (orderItemList.isNotEmpty()) orderItemList.first()
        .run { "${shopName}${if (positionName.isNotEmpty()) "\n${positionName}" else ""}\n${area}${address}\n${goodsName}" } else ""

    fun getOrderDeviceUnit(): String =
        if (orderItemList.isNotEmpty()) "${orderItemList.first().unit}分钟" else ""

    fun getOrderDeviceOriginPrice(): String =
        if (orderItemList.isNotEmpty()) "${
            com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(
                orderItemList.first().originPrice
            )
        }" else ""

    fun getOrderDiscountTotalPrice(): String = try {
        val price = discountPrice ?: 0.0
        if (price <= 0.0) ""
        else com.yunshang.haile_life.utils.string.StringUtils.formatAmountStr(price)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }

    fun drinkingOverTime(): String = orderItemList.firstOrNull()?.let { first ->
        first.goodsItemInfoDto?.overTime ?: run { first.goodsItemInfo?.overTime }
    } ?: ""

    fun drinkingPauseTime(): String = orderItemList.firstOrNull()?.let { first ->
        first.goodsItemInfoDto?.pauseTime ?: run { first.goodsItemInfo?.pauseTime }
    } ?: ""

    val hasRefundMoney: Boolean
        get() = refundTag?.split(",")?.contains("1") ?: false

    val hasRefundCoupon: Boolean
        get() = refundTag?.split(",")?.contains("2") ?: false

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
    val num: String,
    val orderNo: String,
    val originPrice: String,
    val discountPrice: String,
    val priceCalculateMode: Int,
    val unitCode: Int,
    val realPrice: String,
    val shopName: String,
    val unit: String,
    @SerializedName("goodsItemInfo")
    val _goodsItemInfo: String,
//    val goodsItemExtAttr: String,
    val originUnitPrice: String,
    val realUnitPrice: String,
    val volumeVisibleState: Int,
    val goodsItemInfoDto: GoodsItemInfoDto?,
    val positionId: Int,
    val positionName: String,
    val spuCode: String,
    val selfClean: Boolean = false,
) {

    val goodsItemInfo: GoodsItemInfoEntity?
        get() = if (DeviceCategory.isDrinking(categoryCode)) GsonUtils.json2Class(
            _goodsItemInfo,
            GoodsItemInfoEntity::class.java
        ) else null

    val unitValue: String
        get() = goodsItemInfoDto?.let {
            if (1 == goodsItemInfoDto.priceCalculateMode) {
                // 流量
                if (1 == goodsItemInfoDto.unitCode) "ml" else "L"
            } else {
                //时间
                if (1 == goodsItemInfoDto.unitCode) "秒" else "分钟"
            }
        } ?: run {
            // 兼容老数据
            if (1 == priceCalculateMode) {
                // 流量
                if (1 == unitCode) "ml" else "L"
            } else {
                //时间
                if (1 == unitCode) "秒" else "分钟"
            }
        }

    fun getOrderDeviceUnit(state: Int): String = goodsItemInfoDto?.let {
        if (DeviceCategory.isDrinkingOrShower(categoryCode)) {
            "${originUnitPrice}元/${if (1.0 == goodsItemInfoDto.unitAmount?.toDefaultDouble(1.0)) "" else goodsItemInfoDto.unitAmount.toRemove0Str()}${unitValue}${if (1 == volumeVisibleState && 50 != state) " X ${goodsItemInfoDto.unit.toRemove0Str()}${unitValue}" else ""}"
        } else "${goodsItemInfoDto.unit.toRemove0Str()}${unitValue}"
    } ?: run {
        if (DeviceCategory.isDrinkingOrShower(categoryCode)) {
            "${originUnitPrice}元/${unitValue}${if (50 != state) " X ${unit.toRemove0Str()}${unitValue}" else ""}"
        } else "${unit.toRemove0Str()}${unitValue}"
    }

    fun getOrderDeviceOriginPrice(state: Int): String = if (50 == state) ""
    else
        "${
            com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(
                originPrice
            )
        }"

    fun getOrderDeviceDiscountPrice(state: Int): String = if (50 == state) ""
    else
        "${
            com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(
                "-$discountPrice"
            )
        }"
}

data class GoodsItemInfoDto(
    val categoryCode: String,
    val categoryName: String,
    val goodsItemName: String,
    val goodsName: String,
    val overTime: String,
    val pauseTime: String,
    val priceCalculateMode: Int,
    val pulse: Int,
    val skuCode: String,
    val soldType: Int,
    val unit: String,
    val unitCode: Int,
    val unitAmount: String?,
    val unitPrice: String?,
)

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
) {
    fun getOrderDeviceDiscountPrice(): String = try {
        val price = discountPrice.toDouble()
        if (price <= 0) ""
        else com.yunshang.haile_life.utils.string.StringUtils.formatAmountStr(price)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

data class CheckInfo(
    val checkState: Int? = null,
    val enableCheck: Boolean = false,
    val invalidTime: String? = null,
    val invalidTimeStamp: Int? = null
)

data class ReserveInfo(
    val appointmentReason: String? = null,
    val appointmentState: Int? = null,
    val appointmentTime: String? = null,
    val appointmentUsageTime: String? = null,
    val reserveAutoRefund: Int? = null
)

data class FulfillInfo(
    val fulfill: Int = 0,
    val fulfillingItem: FulfillingItem? = null
) {
    fun selfCleanFinish(): Boolean =
        2 == fulfill || (1 == fulfill && (false == fulfillingItem?.selfClean || 3 == fulfillingItem?.state))
}

data class FulfillingItem(
    val finishTime: String? = null,
    val finishTimeTimeStamp: Int? = null,
    val fulfillId: Int? = null,
    val selfClean: Boolean? = null,
    val state: Int? = null
) {
    fun selfCleanFinishTime(): String {
        var timeStamp = finishTimeTimeStamp ?: 0
        if (timeStamp <= 0) {
            timeStamp = 1
        }

        val timeVal = if (timeStamp > 60) {
            " ${ceil(timeStamp * 1.0 / 60).toInt()}分钟"
        } else {
            " 1分钟"
        }
        return "${StringUtils.getString(R.string.predict_remnant)} $timeVal"
    }
}


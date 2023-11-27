package com.yunshang.haile_life.data.entities

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.extend.toDefaultDouble
import com.yunshang.haile_life.data.rule.IOrderConfigEntity


/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/9 14:35
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class DeviceDetailItemEntity(
    val amount: Int,
//    val attach: Int,
    val attachMap: Map<String, Boolean>? = null,
    val channelStatus: Int,
    val couponAmount: Double,
    val defaultModel: Boolean,
    val discount: String,
    val discountPrice: String,
    val extAttr: String,
    val feature: String,
    val id: Int,
    val name: String,
    val price: String,
    val skuBrandKey: String,
    val skuId: Int,
    val soldState: Int,
    val stockState: Int,
    val unit: String,
    val vipDiscount: String,
    val vipDiscountPrice: String,
    val extAttrDto: ExtAttrDto
) : IOrderConfigEntity {

    fun drinkingTitle(isShower: Boolean): SpannableString = "${
        extAttrDto.items.firstOrNull()?.let { it.unitPrice + "元/" + it.getTitle("") } ?: ""
    }".let { price ->
        val content = price + "\n${if (isShower) "单价" else name}"
        com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            content,
            arrayOf(
                AbsoluteSizeSpan(DimensionUtils.sp2px(14f)),
                StyleSpan(Typeface.NORMAL)
            ), 0, price.length
        )
    }

    fun drinkingIcon(isShower: Boolean): Int =
        if (isShower) R.mipmap.icon_shower_configure else if (1 == extAttrDto.items.firstOrNull()?.goodsType) R.mipmap.icon_drinking_configure_normal else R.mipmap.icon_drinking_configure_hot

    override fun getTitle(code: String?): String =
        if (DeviceCategory.isHair(code) && 0 == amount) "使用中..." else name

    override fun getTitleTxtColor(code: String?): Int =
        if (DeviceCategory.isHair(code) && 0 == amount) R.color.color_black_25
        else if (DeviceCategory.isDryerOrHair(code)) R.color.selector_black85_ff630e
        else R.color.selector_black85_04d1e5

    override fun getTitleBg(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.drawable.selector_device_model_item_dryer
        else R.drawable.selector_device_model_item

    override fun defaultVal(): Boolean = extAttrDto.items.any { item -> item.defaultVal() }
}

data class ExtAttrDto(
    val items: MutableList<ExtAttrDtoItem>
)

data class ExtAttrDtoItem(
    val canMerchantEdit: Boolean?,
    val compatibleGoodsCategoryCode: List<String>?,
    val description: String?,
    val functionType: Int,
    val goodsType: Int,
    val isDefault: Boolean,
//    val isOn: Boolean,
    val isEnabled: Boolean,
    val priceCalculateMode: Int,
    val priceType: Int,
    val pulse: String,
    val pulseVolumeFactor: String,
    val sequence: Int?,
    var unitAmount: String,
    val unitCode: Int,
    val unitPrice: String
) : IOrderConfigEntity {

    override fun getTitle(code: String?): String =
        if (unitAmount.isEmpty()) "不需要"
        else (if (unitAmount.toDefaultDouble(1.0) == 1.0) "" else unitAmount) + if (1 == priceCalculateMode) {
            // 流量
            if (1 == unitCode) "ml" else "L"
        } else {
            // 时间
            if (1 == unitCode) "秒" else "分钟"
        }

    override fun getTitleTxtColor(code: String?): Int =
        if (DeviceCategory.isHair(code) && (unitAmount.isEmpty() || unitAmount == "0")) R.color.color_black_25
        else if (DeviceCategory.isDryerOrHair(code)) R.color.selector_black85_ff630e
        else R.color.selector_black85_04d1e5

    override fun getTitleBg(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.drawable.selector_device_model_item_dryer
        else R.drawable.selector_device_model_item1

    override fun defaultVal(): Boolean = isDefault
}

data class ExtAttrBean(
    val minutes: Int,
    var price: String,
) : IOrderConfigEntity {
    override fun getTitle(code: String?): String =
        "${minutes}${StringUtils.getString(R.string.minute)}"

    override fun getTitleTxtColor(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.color.selector_black85_ff630e
        else R.color.selector_black85_04d1e5

    override fun getTitleBg(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.drawable.selector_device_model_item_dryer
        else R.drawable.selector_device_model_item1

    override fun defaultVal(): Boolean = false
}
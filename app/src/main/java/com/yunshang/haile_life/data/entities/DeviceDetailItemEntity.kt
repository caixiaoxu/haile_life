package com.yunshang.haile_life.data.entities

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
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
    val attach: Int,
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
    val dosingConfigDTOS: List<DosingConfigDTOS>
) : IOrderConfigEntity {

    override fun getTitle(code: String?): String =
        if (DeviceCategory.isHair(code) && 0 == amount) "使用中..." else name

    override fun getTitleTxtColor(code: String?): Int =
        if (DeviceCategory.isHair(code) && 0 == amount) R.color.color_black_25
        else if (DeviceCategory.isDryerOrHair(code)) R.color.selector_black85_ff630e
        else R.color.selector_black85_04d1e5

    override fun getTitleBg(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.drawable.selector_device_model_item_dryer
        else R.drawable.selector_device_model_item

    fun getExtAttrs(isDryerOrHair: Boolean) =
        if (isDryerOrHair) {
            GsonUtils.json2List(extAttr, ExtAttrBean::class.java) ?: arrayListOf()
        } else {
            try {
                arrayListOf(ExtAttrBean(unit.toInt(), price.toDouble()))
            } catch (e: Exception) {
                e.printStackTrace()
                arrayListOf()
            }
        }

    val drinkingTitle: SpannableString
        get() = com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            "${name} ${price}${if (1 == getDrinkingExtAttr()?.priceCalculateMode) "ml" else "s"}",
            arrayOf(
                AbsoluteSizeSpan(DimensionUtils.sp2px(14f)),
                StyleSpan(Typeface.NORMAL)
            ), 0, name.length
        )

    val drinkingIcon: Int
        get() = if (1 == getDrinkingExtAttr()?.waterTypeId) R.mipmap.icon_drinking_configure_normal else R.mipmap.icon_drinking_configure_hot

    var drinkAttrConfigure: DrinkAttrConfigure? = null

    fun getDrinkingExtAttr() =
        (drinkAttrConfigure ?: GsonUtils.json2Class(extAttr, DrinkAttrConfigure::class.java).also {
            drinkAttrConfigure = it
        })
}

data class ExtAttrBean(
    val minutes: Int,
    var price: Double,
) : IOrderConfigEntity {
    override fun getTitle(code: String?): String =
        "${minutes}${StringUtils.getString(R.string.minute)}"

    override fun getTitleTxtColor(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.color.selector_black85_ff630e
        else R.color.selector_black85_04d1e5

    override fun getTitleBg(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.drawable.selector_device_model_item_dryer
        else R.drawable.selector_device_model_item1
}

data class DrinkAttrConfigure(
    val overTime: String,
    val pauseTime: String,
    val priceCalculateMode: Int,//1 按流量，2按时间
    val priceCalculateUnit: String,
    val singlePulseQuantity: String,
    val waterTypeId: Int
)

data class DosingConfigDTOS(
    val amount: String,
    val delayTime: Int,
    val description: String,
    val isDefault: Boolean,
    val isOn: Boolean,
    val itemId: Int,
    val liquidTypeId: Int,
    val name: String,
    val price: String
):IOrderConfigEntity{
    override fun getTitle(code: String?): String = name

    override fun getTitleTxtColor(code: String?): Int = R.color.selector_black85_04d1e5

    override fun getTitleBg(code: String?): Int = R.drawable.selector_device_model_item1

}
package com.yunshang.haile_life.data.entities

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
    val vipDiscountPrice: String
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
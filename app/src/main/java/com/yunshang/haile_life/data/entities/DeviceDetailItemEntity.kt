package com.yunshang.haile_life.data.entities

import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory

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
    val amount: Double,
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
) {
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
) {
    fun minutesStr(): String = "${minutes}${StringUtils.getString(R.string.minute)}"
}
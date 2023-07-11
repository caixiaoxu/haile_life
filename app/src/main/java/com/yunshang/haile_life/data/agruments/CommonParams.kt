package com.yunshang.haile_life.data.agruments

import androidx.annotation.StringDef

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/7 19:48
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object DeviceCategory {
    const val CategoryId = "categoryId"
    const val CategoryCode = "categoryCode"
    const val CommunicationType = "communicationType"

    // 洗衣机
    const val Washing = "00"

    // 洗鞋机
    const val Shoes = "01"

    // 烘干机
    const val Dryer = "02"

    // 吹风机
    const val Hair = "03"

    // 饮水机
    const val Water = "04"

    @StringDef(Washing, Shoes, Dryer, Hair, Water)
    @Retention(AnnotationRetention.SOURCE)
    annotation class IDeviceCategoryType

    /**
     * 是否是烘干机
     */
    @JvmStatic
    fun isDryerOrHair(categoryCode: String?) = Dryer == categoryCode || Hair == categoryCode

    fun categoryName(categoryCode: String?): String = when (categoryCode) {
        Washing -> "洗衣机"
        Shoes -> "洗鞋机"
        Dryer -> "烘干机"
        Hair -> "吹风机"
        Water -> "饮水机"
        else -> ""
    }

    /**
     * 是否是脉冲设备
     * 10-串口 20-脉冲
     */
    fun isPulseDevice(communicationType: Int?) = 20 == communicationType

    /**
     * 可显示的设备类型
     */
    @JvmStatic
    fun canShowDeviceCategory(categoryCode: String): Boolean =
        categoryCode in arrayOf(Washing, Shoes, Dryer, Hair)
}

object OrderStatus {
    @JvmStatic
    fun getAppointStateName(code: Int): String = when (code) {
        0 -> "待支付"
        1 -> "待生效"
        2 -> "已生效"
        3 -> "已失效"
        4 -> "已取消"
        else -> ""
    }
}



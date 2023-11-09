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

    // 沐浴
    const val Shower = "08"

    // 投放器
    const val Dispenser = "09"

    @StringDef(Washing, Shoes, Dryer, Hair, Water, Dispenser)
    @Retention(AnnotationRetention.SOURCE)
    annotation class IDeviceCategoryType

    /**
     * 是否是洗衣机
     */
    fun isWashing(categoryCode: String?) = Washing == categoryCode

    /**
     * 是否是洗鞋机
     */
    fun isShoes(categoryCode: String?) = Shoes == categoryCode

    /**
     * 是否是洗衣机或洗鞋机
     */
    fun isWashingOrShoes(categoryCode: String?) = Washing == categoryCode || Shoes == categoryCode

    /**
     * 是否是烘干机或吹风机
     */
    @JvmStatic
    fun isDryerOrHair(categoryCode: String?) = Dryer == categoryCode || Hair == categoryCode

    /**
     * 是否是烘干机或吹风机或投放器
     */
    @JvmStatic
    fun isDryerOrHairOrDispenser(categoryCode: String?) =
        Dryer == categoryCode || Hair == categoryCode || Dispenser == categoryCode

    /**
     * 是否是烘干机
     */
    @JvmStatic
    fun isDryer(categoryCode: String?) = Dryer == categoryCode

    /**
     * 是否是吹风机
     */
    @JvmStatic
    fun isHair(categoryCode: String?) = Hair == categoryCode

    /**
     * 是否是饮水机或淋浴
     */
    @JvmStatic
    fun isDrinkingOrShower(categoryCode: String?) = Water == categoryCode || Shower == categoryCode

    /**
     * 是否是饮水机
     */
    @JvmStatic
    fun isDrinking(categoryCode: String?) = Water == categoryCode

    /**
     * 是否是淋浴
     */
    @JvmStatic
    fun isShower(categoryCode: String?) = Shower == categoryCode

    /**
     * 是否是投放机
     */
    @JvmStatic
    fun isDispenser(categoryCode: String?) = Dispenser == categoryCode

    fun categoryName(categoryCode: String?): String = when (categoryCode) {
        Washing -> "洗衣机"
        Shoes -> "洗鞋机"
        Dryer -> "烘干机"
        Hair -> "吹风机"
        Water -> "饮水机"
        Dispenser -> "投放器"
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
    fun getAppointStateName(code: Int, formScan: Boolean = false): String = when (code) {
        0 -> "待支付"
        1 -> if (formScan) "您已预约该机器" else "待生效"
        2 -> "已生效"
        3 -> "已失效"
        4 -> "已取消"
        5 -> "待使用"
        else -> ""
    }
}

object TypeStatus {

    /**
     * 海星交易类型
     */
    @JvmStatic
    fun subTypeStatus(subType: Int): String = when (subType) {
        101 -> "用户充值收入"
        102 -> "商家充值收入"
        103 -> "订单未支付返还收入"
        104 -> "订单退款返还收入"
        105 -> "商家预先充值"
        106 -> "用户退款失败收入"
        107 -> "用户退款驳回收入"
        201 -> "订单使用支出"
        202 -> "商家回收支出"
        203 -> "用户退款申请支出"
        204 -> "平台回收支出"
        else -> ""
    }
}



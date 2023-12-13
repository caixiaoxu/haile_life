package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/13 18:25
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class OrderStateListEntity(
    val checkInfo: CheckInfo? = null,
    val fulfillInfo: FulfillInfo? = null,
    val orderCategory: Int? = null,
    val orderNo: String? = null,
    val orderSubCategory: Int? = null,
    val orderSubType: Int? = null,
    val orderType: Int? = null,
    val positionName: String? = null,
    val redirectWorking: Boolean? = null,
    val reserveInfo: ReserveInfo? = null,
    val stateList: List<DeviceStateEntity>? = null
) {
    val isAppoint: Boolean
        get() = 300 == orderType || 106 == orderSubType
}
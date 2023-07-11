package com.yunshang.haile_life.data.entities

import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/7 19:32
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class StoreDeviceEntity(
    val categoryCode: String,
    val categoryName: String,
    val idleCount: Int,
    val total: Int
) {

    fun shopIcon(): Int = when (categoryCode) {
        DeviceCategory.Washing -> R.mipmap.icon_home_device_wash
        DeviceCategory.Shoes -> R.mipmap.icon_home_device_shoes
        DeviceCategory.Dryer -> R.mipmap.icon_home_device_dryer
        DeviceCategory.Hair -> R.mipmap.icon_home_device_hair
        DeviceCategory.Water -> 0
        else -> 0
    }
}
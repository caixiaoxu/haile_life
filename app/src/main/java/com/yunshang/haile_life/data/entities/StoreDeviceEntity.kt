package com.yunshang.haile_life.data.entities

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.yunshang.haile_life.BR
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
    val categoryCode: String? = null,
    val categoryName: String? = null,
    val idleCount: Int? = null,
    val total: Int? = null
) : BaseObservable() {

    var page: Int = 1

    var deviceList: MutableList<ShopPositionDeviceEntity> = mutableListOf()

    @get:Bindable
    val hasMore: Boolean
        get() = deviceList.size < (total ?: 0)

    fun refreshDeviceList(items: MutableList<ShopPositionDeviceEntity>?) {
        items?.let {
            deviceList.addAll(items)
        }
        notifyPropertyChanged(BR.hasMore)
    }

    init {
        page = 1
        deviceList = mutableListOf()
    }

    fun shopIcon(): Int = when (categoryCode) {
        DeviceCategory.Washing -> R.mipmap.icon_home_device_wash
        DeviceCategory.Shoes -> R.mipmap.icon_home_device_shoes
        DeviceCategory.Dryer -> R.mipmap.icon_home_device_dryer
        DeviceCategory.Hair -> R.mipmap.icon_home_device_hair
        DeviceCategory.Water -> R.mipmap.icon_home_device_drink
        DeviceCategory.Dispenser -> R.mipmap.icon_home_device_dispenser
        else -> 0
    }

}
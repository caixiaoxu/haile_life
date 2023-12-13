package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/6 17:40
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class DeviceStateListEntity(
    val stateList: List<DeviceStateEntity>? = null
)

data class DeviceStateEntity(
    val state: Int,
    val stateName: String,
    val time: String
)
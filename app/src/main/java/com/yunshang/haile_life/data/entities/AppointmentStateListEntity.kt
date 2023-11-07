package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/7 17:41
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class AppointmentStateListEntity(
    val lineOrder: Int,
    val inLineList: List<AppointmentState>? = null,
    val underwayList: List<AppointmentState>? = null
)

data class AppointmentState(
    val state: Int? = null,
    val stateName: String? = null,
    val time: String? = null
)
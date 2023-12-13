package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/10/16 16:39
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopPositionDeviceEntity(
    val floorCode: String? = null,
    val id: Int? = null,
    val name: String? = null,
    val state: Int? = null,
    val enableReserve: Boolean? = null,
    val reserveState: Int? = null
) {
    val floorCodeVal: String
        get() = floorCode?.let { if (it.isEmpty() || it.last() == 'F' || it == "其它") it else "${it}F" }
            ?: ""
}
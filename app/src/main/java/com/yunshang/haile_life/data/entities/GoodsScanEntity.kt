package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/9 14:28
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class GoodsScanEntity(
    val categoryCode: String,
    val categoryName: String,
    val goodsId: Int,
    val shopId: Int,
    val shopType: Int,
    val deviceId: Int? = null,
    val deviceName: String? = null,
)
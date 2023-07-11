package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/7 14:35
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopStarfishTotalEntity(
    val createTime: String,
    val id: Int,
    val presentAmount: Int,
    val principalAmount: Int,
    val shopId: Int,
    val shopName: String,
    val totalAmount: Int,
    val userId: Int
)
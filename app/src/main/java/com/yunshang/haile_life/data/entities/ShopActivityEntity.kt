package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2024/1/18 14:55
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopActivityEntity(
    val activityExecuteNodeId: Int? = 0,
    val activityId: Int? = 0,
    val activitySubTypeId: Int? = 0,
    var status: Int? = 0,
    val activityCouponDTOS: List<CouponDTOS>? = listOf(),
)

data class CouponDTOS(
    val assetIds: List<Int>? = null,
    val count: Int? = 0,
    val couponVO: Promotion? = null,
    val promotionId: Int? = 0,
    val promotionProductId: Int? = 0,
    val promotionProductSubId: Int? = 0
)
package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/3 10:00
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopUmpListEntity(
    val items: List<ShopUmpItem>
)

data class ShopUmpItem(
    val promotionInfoOfShop: PromotionInfoOfShop,
    val promotionProduct: Int
)

data class PromotionInfoOfShop(
    val banner: String,
    val icon: String
)
package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/7 14:34
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopStarfishListEntity(
    val goodsCategoryCode: String,
    val goodsCategoryId: Int,
    val goodsId: Int,
    val id: Int,
    val rewardList: List<Reward>,
    val shopId: Int,
    val shopName: String,
    val exchangeRate: Int,
)

data class Reward(
    val goodsItemId: Int,
    val id: Int,
    val price: String,
    val reach: Int,
    val reward: Int,
    val shopId: Int,
    val shopTokenCoinId: Int
)
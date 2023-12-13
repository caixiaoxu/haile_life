package com.yunshang.haile_life.data.agruments

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/7 15:08
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class NewOrderParams(
    val purchaseList: List<Purchase> = arrayListOf(),
    var reserveMethod: Int? = null
)

data class Purchase(
    val goodsId: Int? = null,
    val goodsItemId: Int? = null,
    val num: String? = null,
    val soldType: Int? = null,
    val unitCode: Int? = null
)
package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/7 15:45
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class OrderSubmitResultEntity(
    val orderCategory: Int? = null,
    val orderNo: String? = null,
    val orderSubCategory: Int? = null,
    val orderSubType: Int? = null,
    val orderType: Int? = null
)
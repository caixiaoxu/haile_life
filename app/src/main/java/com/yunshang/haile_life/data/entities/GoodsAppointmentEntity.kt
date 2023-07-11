package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/9 14:31
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class GoodsAppointmentEntity(
    val appointmentTime: String,
    val discountPrice: Int,
    val itemList: List<GoodsAppointmentItem>,
    val orderNo: String,
    val originPrice: Int,
    val realPrice: Int
)

data class GoodsAppointmentItem(
    val goodsCategoryCode: String,
    val goodsId: Int,
    val goodsItemId: Int,
    val goodsItemName: String,
    val unit: Int
)
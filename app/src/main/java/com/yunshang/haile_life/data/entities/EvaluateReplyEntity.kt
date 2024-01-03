package com.yunshang.haile_life.data.entities

import com.yunshang.haile_life.data.agruments.DeviceCategory

/**
 * Title :
 * Author: Lsy
 * Date: 2024/1/3 10:58
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class EvaluateReplyEntity(
    val id: Int? = null,
    val orderId: Int? = null,
    val type: Int? = null,
    val reply: String? = null,
    val replyName: String? = null,
    val replyTime: String? = null,
    val createTime: String? = null,
    val pictures: List<String>? = null,
    val tradeOrder: TradeOrder? = null
) {
    fun getOrderDeviceName(): String = (tradeOrder?.itemList?.firstOrNull()?.let {
        if (DeviceCategory.isDrinking(it.goodsCategoryCode)) {
            tradeOrder.itemList.filter { item -> !item.goodsItemName.isNullOrEmpty() }
                .joinToString(",") { item -> item.goodsItemName!! }
        } else "${it.goodsItemName}"
    } ?: "")

    /**
     * 1平台回复 2商家回复 3用户追评
     */
    fun replyTitle(): String = "${
        when (type) {
            1 -> "平台回复"
            2 -> "商家回复"
            3 -> "追评"
            else -> ""
        }
    }：($replyTime)"
}

data class TradeOrder(
    val positionName: String? = null,
    val createTime: String? = null,
    val itemList: List<Item>? = null,
)

data class Item(
    val goodsCategoryCode: String? = null,
    val goodsItemName: String? = null,
)
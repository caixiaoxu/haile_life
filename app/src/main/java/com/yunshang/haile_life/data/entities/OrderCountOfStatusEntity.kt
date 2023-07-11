package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/11 10:27
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class OrderCountOfStatusEntity(
    val orderCount: Int,
    val orderState: Int //订单状态 ，100：待支付；500：进行中；1000：已完成；2099：已退款
)
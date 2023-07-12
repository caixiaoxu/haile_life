package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/12 13:35
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class StarfishRefundApplyEntity(
    val createTime: String,
    val exchangeRate: Int,
    val id: Int,
    val operatorPhone: String,
    val phone: String,
    val presentAmount: Int,
    val principalAmount: Int,
    val shopId: Int,
    val shopName: String,
    val totalAmount: Int,
    val userId: Int
) {
    var isCheck: Boolean = false

    fun getAmountVal(): Double = totalAmount * 1.0 / exchangeRate
}
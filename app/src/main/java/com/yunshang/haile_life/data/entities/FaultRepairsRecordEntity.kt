package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/21 11:07
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class FaultRepairsRecordEntity(
    val createTime: String? = null,
    val creator: Int? = null,
    val description: String? = null,
    val deviceId: Int? = null,
    val deviceName: String? = null,
    val fixSubTypeCode: String? = null,
    val fixSubTypeName: String? = null,
    val id: Int? = null,
    val pics: List<String>? = null,
    val pointName: String? = null,
    val replyDTOS: List<ReplyDTOS>? = null,
    val userAccount: String? = null
) {
    val hasReply: Boolean
        get() = !replyDTOS.isNullOrEmpty()
}

data class ReplyDTOS(
    val deviceFixLogIds: List<Int>? = null,
    val deviceIds: List<Int>? = null,
    val replyContent: String? = null,
    val replyRole: Int? = null,
    val replyUserId: Int? = null,
    val createTime: String? = null
) {
    val replyContentVal: String
        get() = "商家回复(${createTime}）：$replyContent"
}
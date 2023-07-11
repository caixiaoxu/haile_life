package com.yunshang.haile_life.data.entities

import com.lsy.framelib.utils.gson.GsonUtils

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/8 14:56
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class MessageEntity(
    val accountId: Int,
    val appType: Int,
    val content: String,
    val createTime: String,
    val id: Int,
    val logo: String,
    val msgKey: String,
    val read: Int,
    val readTime: String,
    val subtype: String,
    val subtypeId: Int,
    val title: String,
    val typeId: Int
) {
    fun messageContent(): String =
        GsonUtils.json2Class(content, MessageContentEntity::class.java)?.title ?: ""
}

data class MessageContentEntity(
    val endTime: String,
    val goodsId: Int,
    val goodsName: String,
    val itemId: Int,
    val itemName: String,
    val orderId: Int,
    val orderNo: String,
    val shopId: Int,
    val shopName: String,
    val startTime: String,
    val subject: String,
    val tags: String,
    val title: String
)
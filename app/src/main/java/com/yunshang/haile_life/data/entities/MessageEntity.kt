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

    fun getTitleVal(): String =
        when (subtype) {
            "user:order:appoint" -> title
            "user:order:start" -> "您有1笔订单正在进行中"
            else -> "您有1笔订单已完成"
        }

    fun messageContent(): String =
        GsonUtils.json2Class(content, MessageContentEntity::class.java)?.let {
            when (subtype) {
                "user:order:appoint" -> it.content
                "user:order:start" -> "预计${it.endTime}完成"
                else -> "完成时间:${it.endTime}"
            }
        } ?: ""
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
    val content: String,
)
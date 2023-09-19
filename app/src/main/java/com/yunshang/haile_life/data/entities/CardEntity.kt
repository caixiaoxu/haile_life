package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/16 10:32
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class CardEntity(
    val bindStatus: Int,
    val bindTime: String,
    val cardSn: String,
    val createTime: String,
    val id: Int,
    val lastEditor: Int,
    val lastEditorName: String,
    val status: Int,
    val updateTime: String,
    val userAccount: String,
    val userId: Int
)
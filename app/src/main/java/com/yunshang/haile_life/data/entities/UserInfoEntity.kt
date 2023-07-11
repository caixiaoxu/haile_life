package com.yunshang.haile_life.data.entities

/**
 * Title : 用户信息
 * Author: Lsy
 * Date: 2023/4/6 11:28
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class UserInfoEntity(
    val avatar: String,
    val city: String,
    val gender: Int,
    val nickname: String,
    val phone: String,
    val province: String
) {
    fun formatPhone(): String = if (phone.length > 8) phone.replaceRange(3, 7, "****") else phone
}



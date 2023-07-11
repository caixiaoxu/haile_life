package com.yunshang.haile_life.data.entities

/**
 * Title : 登录实体类
 * Author: Lsy
 * Date: 2023/4/4 18:21
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class LoginEntity(
    val organizationId: Int,
    val organizationType: Int,
    val token: String,
    val userId: Int
)
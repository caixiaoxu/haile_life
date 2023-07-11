package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/5 18:24
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ThirdLoginEntity(
    val accountInfoDto: UserInfoEntity,
    val code: String,
    val organizationId: Int,
    val organizationType: Int,
    val token: String,
    val userId: Int
)
package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/7 10:20
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class BindPhoneEntity(
    val accountBindFlag: Boolean,
    val code: String,
    val tokenLicenseDto: TokenLicenseDto
)

data class TokenLicenseDto(
    val organizationId: Int,
    val organizationType: Int,
    val token: String,
    val userId: Int
)
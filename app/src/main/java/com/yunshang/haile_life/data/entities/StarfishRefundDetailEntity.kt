package com.yunshang.haile_life.data.entities

import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/12 16:03
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class StarfishRefundDetailEntity(
    val account: String,
    val createTime: String,
    val id: Int,
    val refundNo: String,
    val refundPrice: Double,
    val remark: String,
    val serviceTelephone: String,
    val state: Int,
    val stateDesc: String,
    val transAccount: String,
    val transAccountType: Int,
    val transRealName: String,
    val updateTime: String,
    val userId: Int,
    val userTokenCoinRefundItemRecordDTOList: List<UserTokenCoinRefundItemRecordDTO>
) {
    fun refundAccountTitle(): String =
        StringUtils.getString(if (1 == transAccountType) R.string.alipay_account else R.string.wechat_account)
}

data class UserTokenCoinRefundItemRecordDTO(
    val presentAmount: Int,
    val principalAmount: Int,
    val refundPrice: Double,
    val shopId: Int,
    val shopName: String
)
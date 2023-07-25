package com.yunshang.haile_life.data.entities

import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.rule.IMultiTypeEntity

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/7 18:36
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class NearStoreEntity(
    val address: String,
    val appointmentState: Int,
    val area: String,
    val distance: Double,
    val id: Int,
    val name: String,
    val state: Int,
    val rechargeFlag: Boolean,
    val idleCount: Int,
) : IMultiTypeEntity {
    fun formatDistance(isJoin: Boolean = true): String =
        "${StringUtils.getString(R.string.distance)}${StringUtils.getString(R.string.you)} " +
                (if (distance >= 1000) String.format(
                    "%.2fkm", distance / 1000
                ) else String.format(
                    "%.2fm", distance
                )) + if (isJoin && getAddressVal().isNotEmpty()) " | " else ""

    fun getAddressVal(): String = address

    fun appointStateVal(): String =
        StringUtils.getString(if (1 == appointmentState) R.string.can_appointment else R.string.can_not_appointment)

    fun stateVal(): String =
        StringUtils.getString(if (1 == state) R.string.in_operation else R.string.stop_of_business)

    override fun getMultiType(): Int = 0

    override fun getMultiTypeBgRes(): IntArray = intArrayOf(
        R.drawable.shape_s26ff630e_r4,
        R.drawable.shape_s2604cee5_r4,
    )

    override fun getMultiTypeTxtColors(): IntArray = intArrayOf(

    )
}
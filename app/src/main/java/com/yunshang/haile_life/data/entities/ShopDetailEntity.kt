package com.yunshang.haile_life.data.entities

import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.rule.IMultiTypeEntity

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/6 18:06
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopDetailEntity(
    val address: String,
    val appointment: Boolean,
    val area: String,
    val distance: Double,
    val id: Int,
    val name: String,
    val rechargeFlag: Boolean,
    val serviceTelephone: String,
    val shopDeviceDetailList: List<StoreDeviceEntity>,
    val state: Int,
    val timeMarketVOList: List<TimeMarketVO>,
    val workTime: String
) : IMultiTypeEntity {

    fun formatDistance(): String =
        "${StringUtils.getString(R.string.distance)}" +
                "${StringUtils.getString(R.string.you)} " +
                "${
                    if (distance >= 1000) String.format(
                        "%.2fkm",
                        distance / 1000
                    ) else String.format("%.2fm", distance)
                }"

    fun getBusinessTimeVal(): String =
        "${StringUtils.getString(R.string.business_time)} ${workTime.ifEmpty { "全天24小时" }}"

    fun getAddressVal(): String = if (distance >= 0) " | " else "" + area + address
    override fun getMultiType(): Int = 0

    override fun getMultiTypeBgRes(): IntArray = intArrayOf(
        R.drawable.shape_s26ff630e_r4,
        R.drawable.shape_s2604cee5_r4,
    )

    override fun getMultiTypeTxtColors(): IntArray = intArrayOf(

    )
}

data class TimeMarketVO(
    val categoryList: List<Category>,
    val discount: Int
)

data class Category(
    val categoryCode: String,
    val categoryId: Int,
    val categoryName: String
)
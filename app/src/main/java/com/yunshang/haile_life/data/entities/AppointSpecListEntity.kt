package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/10 10:27
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class AppointSpecListEntity(
    val specValueList: List<AppointSpec>
)

data class AppointSpec(
    val extAttr: ExtAttrBean,
    val feature: String,
    val name: String,
    val specValueId: Int,
    val unitList: List<Int>
)
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
data class AppointCategoryListEntity(
    val categoryList: List<AppointCategory>
)

data class AppointCategory(
    val goodsCategoryCode: String,
    val goodsCategoryId: Int,
    val goodsCategoryName: String
)
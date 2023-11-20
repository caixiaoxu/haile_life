package com.yunshang.haile_life.data.entities

import com.yunshang.haile_life.data.rule.ICommonBottomItemEntity

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/16 17:14
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class FaultCategoryEntity(
    val compatibleCategories: String? = null,
    val description: String? = null,
    val fixTypeCode: String? = null,
    val fixTypeName: String? = null,
    val id: Int? = null,
    val parentId: Int? = null
) : ICommonBottomItemEntity {
    override fun getTitle(): String = fixTypeName ?: ""
}
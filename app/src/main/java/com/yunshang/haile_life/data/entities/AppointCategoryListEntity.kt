package com.yunshang.haile_life.data.entities

import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.rule.IOrderConfigEntity

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
) : IOrderConfigEntity {
    override fun getTitle(code: String?): String = goodsCategoryName
    override fun getTitleTxtColor(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.color.selector_black85_ff630e
        else R.color.selector_black85_04d1e5

    override fun getTitleBg(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.drawable.selector_device_model_item_dryer
        else R.drawable.selector_device_model_item

}
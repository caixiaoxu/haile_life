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
data class AppointSpecListEntity(
    val specValueList: List<AppointSpec>
)

data class AppointSpec(
    val extAttr: ExtAttrBean,
    val feature: String,
    val name: String,
    val specValueId: Int,
    val unitList: List<Int>
) : IOrderConfigEntity {
    override fun getTitle(code: String?): String = name
    override fun getTitleTxtColor(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.color.selector_black85_ff630e
        else R.color.selector_black85_04d1e5

    override fun getTitleBg(code: String?): Int =
        if (DeviceCategory.isDryerOrHair(code)) R.drawable.selector_device_model_item_dryer
        else R.drawable.selector_device_model_item

    override fun defaultVal(): Boolean {
        TODO("Not yet implemented")
    }

}
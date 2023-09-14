package com.yunshang.haile_life.data.agruments

import com.yunshang.haile_life.data.entities.ActiveDayParam

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/14 15:56
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object ShopParam {
    val businessDay = arrayListOf(
        ActiveDayParam("周一", 1),
        ActiveDayParam("周二", 2),
        ActiveDayParam("周三", 3),
        ActiveDayParam("周四", 4),
        ActiveDayParam("周五", 5),
        ActiveDayParam("周六", 6),
        ActiveDayParam("周日", 7),
    )
}
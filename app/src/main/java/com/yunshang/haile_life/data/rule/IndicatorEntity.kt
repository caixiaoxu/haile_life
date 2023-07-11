package com.yunshang.haile_life.data.rule

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/23 14:27
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class IndicatorEntity<T>(
    val title: String,
    var num: Int = 0,
    val value: T,
)

package com.yunshang.haile_life.data.rule

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/17 16:16
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface IMultiSelectItemEntity {
    var isCheck: Boolean

    fun getTitle(): String
}
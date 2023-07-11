package com.yunshang.haile_life.data.rule

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/28 14:34
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface IMultiTypeEntity {
    /**
     * 获取类型
     */
    fun getMultiType(): Int

    fun getMultiTypeBgRes(): IntArray?

    fun getMultiTypeTxtColors(): IntArray
}
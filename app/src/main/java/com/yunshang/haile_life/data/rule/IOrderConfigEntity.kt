package com.yunshang.haile_life.data.rule

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/27 14:52
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface IOrderConfigEntity {

    fun getTitle(code: String?): String

    fun getTitleTxtColor(code: String?): Int

    fun getTitleBg(code: String?): Int

    fun defaultVal():Boolean
}
package com.yunshang.haile_life.utils.scheme

import android.os.Bundle

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/13 17:53
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface ISchemeURLParser {
    fun parsePath(): String
    fun parseParams(params: String): Bundle?
}
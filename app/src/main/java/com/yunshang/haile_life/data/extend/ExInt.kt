package com.yunshang.haile_life.data.extend

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/16 10:42
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
fun Int?.hasVal(): Boolean = null != this && -1 != this

fun Int?.isGreaterThan0(): Boolean = null != this && 0 < this
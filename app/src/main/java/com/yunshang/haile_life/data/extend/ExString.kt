package com.yunshang.haile_life.data.extend

/**
 * Title :
 * Author: Lsy
 * Date: 2023/9/8 16:05
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
fun String?.toDefaultDouble(def: Double = 0.0): Double = if (this.isNullOrEmpty()) def else try {
    this.toDouble()
} catch (e: Exception) {
    e.printStackTrace()
    def
}

fun String?.toRemove0Str(): String = if (this.isNullOrEmpty()) "" else try {
    val arr = this.split(".")
    if (arr.size > 1) {
        val temp = arr[1].dropLastWhile { it == '0' }
        arr[0] + if (temp.isEmpty()) "" else ".$temp"
    } else this
} catch (e: Exception) {
    e.printStackTrace()
    ""
}

fun String?.toDefaultInt(def: Int = 0): Int = if (this.isNullOrEmpty()) def else try {
    this.toInt()
} catch (e: Exception) {
    e.printStackTrace()
    def
}

fun String?.toDefaultInt(def: Number = 0): Number? {
    if (this.isNullOrEmpty()) return null
    // 如果是数字
    return if (this.matches("-?\\d+(\\.\\d+)?".toRegex())) {
        try {
            // 有小数点就转double
            if (this.contains(".")) this.toDouble()
            else this.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    } else null
}

package com.yunshang.haile_life.utils

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Title : 数字处理工具类
 * Author: Lsy
 * Date: 2023/4/10 14:07
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object NumberUtils {

    /**
     * 保留两位小数
     */
    @JvmStatic
    fun keepTwoDecimals(num: Double): String =
        BigDecimal(num).run { setScale(2, RoundingMode.DOWN).toString() }
}
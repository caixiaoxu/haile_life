package com.yunshang.haile_life.data.entities

import com.yunshang.haile_life.utils.DateTimeUtils
import kotlin.math.ceil

/**
 * Title :
 * Author: Lsy
 * Date: 2023/12/11 11:19
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class SelfCleanRefreshEntity(
    val status: Int = 0, //设备执行状态。<40 执行中，40 执行完成，50 执行失败
    val finishTime: String? = null // 预计结束时间.格式：yyyy-MM-dd HH:mm:ss
) {
    val selfCleanStateVal: String
        get() = if (status < 40) "清洁中" else "筒自洁结束"

    //<40 执行中，40 执行完成，50 执行失败.
    fun selfPrompt(remain: String?): String =
        if (status < 40) {
            var minute = ceil(
                ((DateTimeUtils.formatDateFromString(finishTime)?.time
                    ?: 0) - System.currentTimeMillis()) / 1000 / 60.0
            ).toInt()
            if (minute < 1) {
                minute = 1
            }
            "筒自洁清洁中，预计耗时${minute}分钟"
        } else if (40 == status) {
            "筒自洁已结束"
        } else "选择筒自洁，享受${remain}分钟免费筒自洁"
}
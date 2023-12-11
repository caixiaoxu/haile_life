package com.yunshang.haile_life.data.entities

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
    val selfCleanStateVal:String
    get() = if (status < 40) "清洁中" else "筒自洁结束"
}
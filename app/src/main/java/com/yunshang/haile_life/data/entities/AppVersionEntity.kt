package com.yunshang.haile_life.data.entities

import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/13 20:47
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class AppVersionEntity(
    val createTime: String,
    val forceUpdate: Boolean,
    val id: Int,
    val name: String,
    val needUpdate: Boolean,
    val updateLog: String,
    val updateUrl: String,
    val versionCode: String,
    val versionMin: String,
    val versionName: String
) {
    fun getVersionVal(): String = "${StringUtils.getString(R.string.new_version)}${versionName}"
    fun getUpdateTitle(): String = StringUtils.getString(R.string.update_version_title,versionName)
}
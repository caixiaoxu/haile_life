package com.yunshang.haile_life.data.entities

import android.os.Build
import android.text.Html
import android.text.Html.ImageGetter
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.TextView
import com.yunshang.haile_life.utils.DateTimeUtils


/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/17 10:07
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopNoticeEntity(
    val account: String,
    val createTime: String,
    val deleteFlag: Int,
    val endTime: String,
    val id: Int,
    val lastEditor: Int,
    val noticeShopDtos: List<NoticeShopDto>,
    val organizationId: Int,
    val startTime: String,
    val state: Int,
    val templateContent: String,
    val templateEndTime: String,
    val templateId: Int,
    val templateName: String,
    val templateStartTime: String,
    val updateTime: String,
    val userId: Int,
    val version: Int
) {
    fun dateVal(): String = "时间：${
        DateTimeUtils.formatDateTimeForStr(
            templateStartTime,
            "MM月dd日HH:mm",
            "yyyy-MM-dd HH:mm"
        )
    }-${DateTimeUtils.formatDateTimeForStr(templateEndTime, "MM月dd日HH:mm", "yyyy-MM-dd HH:mm")}"
}

data class NoticeShopDto(
    val id: Int,
    val noticeId: Int,
    val shopId: Int,
    val shopName: String
)
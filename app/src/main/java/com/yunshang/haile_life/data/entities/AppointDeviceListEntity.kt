package com.yunshang.haile_life.data.entities

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.lsy.framelib.data.constants.Constants
import com.yunshang.haile_life.R
import com.yunshang.haile_life.utils.string.StringUtils

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/10 10:27
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class AppointDeviceListEntity(
    val itemList: MutableList<AppointDevice>
)

data class AppointDevice(
    val appointmentTime: String,
    val extAttr: String,
    val goodsId: Int,
    val goodsItemId: Int,
    val goodsItemName: String,
    val goodsName: String,
    val price: Double,
    val queueNum: Int,
    val shopName: String,
    val skuId: Int
) {

    fun queueNumVal(): SpannableString {
        val prefix = com.lsy.framelib.utils.StringUtils.getString(R.string.queue_num)
        val content = "$prefix $queueNum"
        return StringUtils.formatMultiStyleStr(
            content,
            arrayOf(
                StyleSpan(Typeface.BOLD)
            ), prefix.length, content.length
        )
    }


    fun usedTimeVal(): SpannableString {
        val prefix = "${com.lsy.framelib.utils.StringUtils.getString(R.string.appoint_use_time)}："
        val content = "$prefix $appointmentTime"
        return StringUtils.formatMultiStyleStr(
            content,
            arrayOf(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        Constants.APP_CONTEXT,
                        R.color.color_22d6e8,
                    )
                ),
                StyleSpan(Typeface.BOLD)
            ), prefix.length, content.length
        )
    }
}
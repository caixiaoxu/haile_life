package com.yunshang.haile_life.data.entities

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.rule.IMultiTypeEntity

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/7 18:36
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class NearStoreEntity(
    val address: String,
    val appointmentState: Int,
    val area: String,
    val distance: Double,
    val id: Int,
    val name: String,
    val state: Int,
    val rechargeFlag: Boolean,
    val idleCount: Int,
) : IMultiTypeEntity {
    fun formatDistance(isJoin: Boolean = true): String =
        "${StringUtils.getString(R.string.distance)}${StringUtils.getString(R.string.you)} " +
                (if (distance >= 1000) String.format(
                    "%.2fkm", distance / 1000
                ) else String.format(
                    "%.2fm", distance
                ))

    fun getAddressVal(): SpannableString {
        val distance = formatDistance()
        val address = if (address.isNotEmpty()) " | $address" else ""

        return com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
            distance + address,
            arrayOf(
                ForegroundColorSpan(
                    ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_black_45),
                ),
                StyleSpan(Typeface.NORMAL)
            ), 0, distance.length
        )
    }

    fun appointStateVal(): String =
        StringUtils.getString(if (1 == appointmentState) R.string.can_appointment else R.string.can_not_appointment)

    fun stateVal(): String =
        StringUtils.getString(if (1 == state) R.string.in_operation else R.string.stop_of_business)

    override fun getMultiType(): Int = 0

    override fun getMultiTypeBgRes(): IntArray = intArrayOf(
        R.drawable.shape_s19ff630e_r4,
        R.drawable.shape_s1904cee5_r4,
    )

    override fun getMultiTypeTxtColors(): IntArray = intArrayOf(

    )
}
package com.yunshang.haile_life.data.entities

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import androidx.databinding.BaseObservable
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.ShopParam
import com.yunshang.haile_life.data.rule.IMultiTypeEntity
import java.util.Calendar

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/6 18:06
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopDetailEntity(
    val address: String,
    val appointment: Boolean,
    val area: String,
    val distance: Double,
    val id: Int,
    val name: String,
    val rechargeFlag: Boolean,
    val serviceTelephone: String,
    val shopDeviceDetailList: List<StoreDeviceEntity>,
    val state: Int,
    val timeMarketVOList: List<TimeMarketVO>,
    val workTime: String,
    val workTimeStr: String,
) : IMultiTypeEntity {

    fun formatDistance(): String =
        "${StringUtils.getString(R.string.distance)}${StringUtils.getString(R.string.you)} " +
                if (distance >= 1000) String.format(
                    "%.2fkm", distance / 1000
                ) else String.format(
                    "%.2fm", distance
                )

    fun workTimeArr(): MutableList<BusinessHourEntity> =
        GsonUtils.json2List(workTimeStr, BusinessHourParams::class.java)?.map {
            BusinessHourEntity().apply {
                formatData(it.weekDays, it.workTime)
            }
        }?.toMutableList() ?: run {
            try {
                GsonUtils.json2List(workTime, String::class.java)?.mapIndexed { index, s ->
                    BusinessHourEntity(listOf(ShopParam.businessDay[index]), s.replace(",", " "))
                }?.filter { item -> item.workTime.isNotEmpty() }?.toMutableList()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } ?: run {
                mutableListOf(
                    BusinessHourEntity(ShopParam.businessDay, workTime)
                )
            }
        }

    fun getBusinessTimeVal(): String {
        var weekNum = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (weekNum == 0) {
            weekNum = 7
        }
        val timeArr = workTimeArr()
        val sb = StringBuilder()
        timeArr.forEach { hour ->
            hour._weekDays.find { item -> item.id == weekNum }?.let {
                sb.append(" ${hour.workTime}")
            }
        }
        return "${StringUtils.getString(R.string.business_time)} " + if (sb.isNotEmpty()) sb.substring(
            1
        ) else ""
    }

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

    override fun getMultiType(): Int = 0

    override fun getMultiTypeBgRes(): IntArray = intArrayOf(
        R.drawable.shape_s19ff630e_r4,
        R.drawable.shape_s1904cee5_r4,
    )

    override fun getMultiTypeTxtColors(): IntArray = intArrayOf(

    )
}

data class BusinessHourEntity(
    var _weekDays: List<ActiveDayParam> = listOf(),
    var workTime: String = ""
) : BaseObservable() {

    val hourWeekVal: String
        get() = if (_weekDays.isEmpty()) "" else {
            val first = _weekDays.first()
            val last = _weekDays.last()
            val temp = last.id - first.id
            if (_weekDays.size == (temp + 1) && temp > 2) {
                "${first.name}至${last.name}"
            } else {
                val sb = StringBuilder()
                _weekDays.forEachIndexed { index, activeDayParam ->
                    if (index % 2 != 0) {
                        sb.append("${activeDayParam.name}\n")
                    } else {
                        sb.append("${activeDayParam.name}、")
                    }
                }
                if (sb.isNotEmpty()) {
                    sb.substring(0, sb.length - 1)
                } else ""
            }
        }

    fun isEmpty(): Boolean = _weekDays.isEmpty() && workTime.isEmpty()

    fun formatData(days: List<Int>, time: String) {
        _weekDays = ShopParam.businessDay.filter { item -> item.id in days }
        workTime = time
    }
}

data class BusinessHourParams(
    val weekDays: List<Int>,
    val workTime: String
)

data class TimeMarketVO(
    val categoryList: List<Category>,
    val discount: Int
)

data class ActiveDayParam(val name: String, val id: Int)

data class Category(
    val categoryCode: String,
    val categoryId: Int,
    val categoryName: String
)
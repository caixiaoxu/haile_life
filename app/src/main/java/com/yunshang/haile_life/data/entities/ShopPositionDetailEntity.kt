package com.yunshang.haile_life.data.entities

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.ShopParam
import com.yunshang.haile_life.data.extend.isGreaterThan0
import com.yunshang.haile_life.data.rule.IMultiTypeEntity
import java.util.*

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
data class ShopPositionDetailEntity(
    val address: String,
    val appointment: Boolean,
    val reserveNum: Int,// 可预约的数据
    val area: String,
    val distance: Double,
    val id: Int,
    val name: String,
    val rechargeFlag: Boolean,
    val serviceTelephone: String,
    val positionDeviceDetailList: List<StoreDeviceEntity>?,
    val state: Int,
    val timeMarketVOList: List<TimeMarketVO>,
    val workTime: String,
    val workTimeStr: String,

    val gpsList: List<Gps>? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val shopId: Int? = null,
) : IMultiTypeEntity {

    var floorList: MutableList<FloorParam>? = mutableListOf()

    fun refreshFloorList(list: MutableList<String>) {
        if (null == floorList) {
            floorList = mutableListOf()
        }
        floorList?.clear()
        floorList?.add(FloorParam("全部", ""))
        floorList?.addAll(list.map {
            FloorParam(if (it.last() == 'F' || it == "其它") it else "${it}F", it)
        })
    }

    val appointmentNumVal: String
        get() = StringUtils.getString(R.string.can_appointment) + if (reserveNum.isGreaterThan0()) "${reserveNum}${StringUtils.getString(R.string.unit_tai)}" else ""

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
        if (2 == state) return "休息中"

        var weekNum = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
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
        return if (sb.isNotEmpty()) "${StringUtils.getString(R.string.business_time)} ${
            sb.substring(
                1
            )
        }" else "休息中"
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

data class Gps(
    val lat: Int? = null,
    val lng: Int? = null,
    val type: Int? = null
)

data class FloorParam(
    val name: String,
    val value: String
)
package com.yunshang.haile_life.utils

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.max

/**
 * Title : 日期时间工具类
 * Author: Lsy
 * Date: 2023/3/21 19:13
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
</desc></version></time></author> */
object DateTimeUtils {
    /**
     * 当月第一天
     */
    @JvmStatic
    val curYearFirst: Date
        get() = getCurYearFirst(Date())

    /**
     * 指定日期当月的第一天
     */
    @JvmStatic
    fun getCurYearFirst(date: Date?): Date {
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        cal[Calendar.MONTH] = 0
        cal[Calendar.DAY_OF_MONTH] = 1
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        return cal.time
    }

    /**
     * 当月第一天
     */
    @JvmStatic
    val curMonthFirst: Date
        get() = getMonthFirst(Date())

    /**
     * 指定日期当月的第一天
     */
    @JvmStatic
    fun getMonthFirst(date: Date?): Date {
        val cal = Calendar.getInstance()
        if (date != null) {
            cal.time = date
        }
        cal[Calendar.DAY_OF_MONTH] = 1
        cal[Calendar.HOUR_OF_DAY] = 0
        cal[Calendar.MINUTE] = 0
        cal[Calendar.SECOND] = 0
        return cal.time
    }

    /**
     * 当月的最后一天
     */
    @JvmStatic
    val curMonthLast: Date
        get() = getMonthLast(Date())

    /**
     * 指定日期当月的最后一天
     */
    @JvmStatic
    fun getMonthLast(date: Date?): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal[Calendar.DAY_OF_MONTH] = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal[Calendar.HOUR_OF_DAY] = 23
        cal[Calendar.MINUTE] = 59
        cal[Calendar.SECOND] = 59
        return cal.time
    }

    /**
     * 是否是同一年
     */
    @JvmStatic
    fun isSameYear(date1: Date?, date2: Date?): Boolean {
        if (null == date1 || null == date2) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    /**
     * 是否是同一月
     */
    @JvmStatic
    fun isSameMonth(date1: Date?, date2: Date?): Boolean {
        if (null == date1 || null == date2) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
    }

    /**
     * 是否是同一日
     */
    @JvmStatic
    fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (null == date1 || null == date2) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
                && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
                && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH))
    }

    /**
     * 前几天
     */
    @JvmStatic
    fun beforeDay(date: Date?, num: Int = 1): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_MONTH, -num)
        return cal.time
    }

    @JvmStatic
    fun beforeWeekFirstDay(date: Date) = Calendar.getInstance().run {
        time = date
        add(Calendar.DAY_OF_MONTH, -7)
        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            add(Calendar.DAY_OF_MONTH, -1)
        }
        time
    }

    @JvmStatic
    fun beforeWeekLastDay(date: Date) = Calendar.getInstance().run {
        time = date
        add(Calendar.DAY_OF_MONTH, -7)
        while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        time
    }

    /**
     * 前一月
     */
    @JvmStatic
    fun beforeMonth(date: Date?): Date {
        val cal = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.MONTH, -1)
        return cal.time
    }

    /**
     * 格式化开始日期参数
     */
    @JvmStatic
    fun formatDateTimeStartParam(date: Date?): String =
        "${formatDateTime(date, "yyyy-MM-dd")} 00:00:00"

    fun formatDateTimeYearStartParam(date: Date?): String =
        "${formatDateTime(date, "yyyy")}-01-01 00:00:00"

    /**
     * 格式化结束日期参数
     */
    @JvmStatic
    fun formatDateTimeEndParam(date: Date?): String =
        "${formatDateTime(date, "yyyy-MM-dd")} 23:59:59"

    fun formatDateTimeYearEndParam(date: Date?): String =
        "${formatDateTime(date, "yyyy")}-12-31 23:59:59"

    /**
     * 格式化
     */
    @JvmStatic
    fun formatDateTime(date: Date?, format: String? = null): String {
        return if (null == date) "" else DateFormat.format(format ?: "yyyy-MM-dd HH:mm:ss", date)
            .toString()
    }

    /**
     * 格式化
     */
    @JvmStatic
    fun formatDateTimeForStr(
        dateStr: String?,
        format: String? = null,
        originFormat: String? = null,
    ): String {
        return formatDateTime(formatDateFromString(dateStr, originFormat), format)
    }

    /**
     * 反格式化
     */
    @JvmStatic
    fun formatDateFromString(dateStr: String?, format: String? = null): Date? {
        val sdf = SimpleDateFormat(format ?: "yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        try {
            return sdf.parse(dateStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 获取年区间列表
     */
    fun getYearSection(minYear: Int, maxYear: Int): List<String> =
        ArrayList<String>().apply {
            for (i in 0 until (maxYear - minYear + 1)) {
                add("${minYear + i}年")
            }
        }

    /**
     * 获取月份列表
     */
    fun getMonthSection(minMonth: Int? = null, maxMonth: Int? = null): List<String> =
        ArrayList<String>().apply {
            for (i in (minMonth ?: 1)..(maxMonth ?: 12)) {
                add("${i}月")
            }
        }

    /**
     * 获取小时列表
     */
    fun getHourSection(): List<String> =
        ArrayList<String>().apply {
            for (i in 0..23) {
                add("${i}时")
            }
        }

    /**
     * 获取分钟列表
     */
    fun getMinuteSection(): List<String> =
        ArrayList<String>().apply {
            for (i in 0..59) {
                add("${i}分")
            }
        }

    /**
     * 获取秒列表
     */
    fun getSecondSection(): List<String> =
        ArrayList<String>().apply {
            for (i in 0..59) {
                add("${i}分")
            }
        }

    /**
     * 获取天份列表
     */
    fun getDaySection(minDay: Int? = null, maxDay: Int): List<String> =
        ArrayList<String>().apply {
            for (i in (minDay ?: 1) until (maxDay + 1)) {
                add("${i}日")
            }
        }

    /**
     * 获取周份列表
     */
    fun getWeekSection(
        firstDay: Int,
        dayNum: Int,
        limitFunc: (sunday: Int) -> Boolean
    ): List<String> =
        ArrayList<String>().apply {
            var i = 0
            var monthDay: Int
            var sunday: Int
            do {
                monthDay = firstDay + (7 * i)
                sunday = monthDay + 6
                add("${monthDay}日-${if (sunday > dayNum) sunday - dayNum else sunday}日")
                i++
            } while (sunday < dayNum && limitFunc(sunday))
        }

    /**
     * 是否两个时间是否为同一天
     *
     * @param date1,date2 两个对比时间
     */
    private fun isSameDate(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        val isSameYear = cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
        val isSameMonth = (isSameYear
                && cal1[Calendar.MONTH] == cal2[Calendar.MONTH])
        return (isSameMonth
                && cal1[Calendar.DAY_OF_MONTH] == cal2[Calendar.DAY_OF_MONTH])
    }

    /**
     * 天数增加
     *
     * @param date 时间
     * @param day  增加天数 可为负整数、正整数,例如-1 相当于减少一天,1 相当于增加一天
     */
    fun addDay(date: Date, day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar[Calendar.DATE] = calendar[Calendar.DATE] + day
        return calendar.time
    }

    /**
     * 计算两个日期间的相差天数
     */
    fun calTwoDaySpace(date1: Date, date2: Date) =
        (date1.time - date2.time) / (1000 * 60 * 60 * 24)

    /**
     * 计算两个日期间的相差天数 绝对值
     */
    fun calTwoDaySpaceAbs(date1: Date, date2: Date) = abs(calTwoDaySpace(date1, date2))

    /**
     * 获取友好时间
     *
     * @param date 时间
     * @param isAf 是否显示将来时间
     */
    fun getFriendlyTime(date: Date?, isAf: Boolean): String {
        if (date == null) {
            return ""
        }
        val ct = ((System.currentTimeMillis() - date.time) / 1000).toInt()
        if (isAf) {
            if (ct < 0) {
                if (ct < -86400) { //86400 * 30
                    val day = ct / -86400
                    if (day == 1) {
                        return "后天 " + formatDateTime(date, "HH:mm")
                    }
                    return if (date.year == Date().year) {
                        formatDateTime(date, "MM-dd HH:mm")
                    } else {
                        formatDateTime(date, "yyyy-MM-dd HH:mm")
                    }
                }
                if (!isSameDate(Date(), date)
                ) { //判断跨天
                    return "明天 " + formatDateTime(date, "HH:mm")
                }
                if (-86400 < ct && ct < -3600) {
                    return String.format(
                        "%d小时后 %s",
                        ct / -3600,
                        formatDateTime(date, "HH:mm")
                    )
                }
                if (-3600 < ct && ct < -61) {
                    return String.format(
                        "%d分钟后 %s",
                        max(ct / -60, 3),
                        formatDateTime(date, "HH:mm")
                    )
                }
                if (-61 < ct && ct < 0) {
                    return String.format("即将到点 %s", formatDateTime(date, "HH:mm"))
                }
            }
        }
        if (ct < 61) { //1分钟内
            return "刚刚"
        }
        if (isSameDate(Date(), date)) { //
            if (ct < 3600) { //1小时内
                return String.format("%d分钟前", max(ct / 60, 3))
            }
            if (ct in 3600..86399) { //当天超过一小时显示
                return formatDateTime(date, "HH:mm")
            }
        } else {
            //不是当天时进入
            if (ct < 86400) {
                return "昨天 " + formatDateTime(date, "HH:mm")
            }
            if (ct in 86400..259199) { //86400 * 3 (三天)
                var day = ct / 86400
                if (!isSameDate(addDay(Date(), -day), date)
                ) { //判断时间匹配日期
                    day += 1
                }
                return if (day == 1) {
                    "昨天 " + formatDateTime(date, "HH:mm")
                } else {
                    formatDateTime(date, "MM-dd HH:mm")
                }
            }
        }
        return if (date.year == Date().year) {
            formatDateTime(date, "MM-dd HH:mm")
        } else {
            formatDateTime(date, "yyyy-MM-dd HH:mm")
        }
    }
}
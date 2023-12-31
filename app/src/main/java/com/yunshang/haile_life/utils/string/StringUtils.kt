package com.yunshang.haile_life.utils.string

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.utils.NumberUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.UnsupportedEncodingException
import java.text.DecimalFormat
import java.util.regex.Pattern
import kotlin.math.abs


/**
 * Title : 字符串工具类
 * Author: Lsy
 * Date: 2023/4/4 18:12
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object StringUtils {

    /**
     * 验证密码
     * 6-12位包含大小写字母和数字三种组合
     *
     * @param password
     * @return
     */
    @JvmStatic
    fun checkPassword(password: String?): Boolean =
        try {
            val check = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z\\d]{6,12}$"
            val regex = Pattern.compile(check)
            val matcher = regex.matcher(password)
            matcher.matches()
        } catch (e: Exception) {
            false
        }

    /**
     * 创建金额字符串
     */
    @JvmStatic
    fun createPriceStr(context: Context, price: Double): SpannableString =
        SpannableString("¥${NumberUtils.keepTwoDecimals(price)}").apply {
            setSpan(
                AbsoluteSizeSpan(DimensionUtils.sp2px(24f, context)),
                0,
                1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE
            )

        }

    /**
     * 格式化手机
     */
    @JvmStatic
    fun formatPhone(phone: String): String = phone.replaceRange(IntRange(3, 7), "****")

    /**
     * 格式化多样式字符串
     * @param content 内容
     * @param spans 多样式
     * @param start 开始位置
     * @param end 结束位置
     */
    fun formatMultiStyleStr(
        content: String,
        spans: Array<Any>,
        start: Int = 0,
        end: Int = content.length
    ): SpannableString = SpannableString(content).apply {
        spans.forEach {
            setSpan(it, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * 格式化地区
     */
    fun formatArea(provinceName: String?, cityName: String?, districtName: String?): String =
        if (null == provinceName || null == cityName || null == districtName) "" else String.format(
            "%s-%s-%s",
            provinceName,
            cityName,
            districtName
        )

    // 获取一个汉字的首字母
    fun getFirstLetter(ch: Char): Char? {
        // 判断是否为汉字，如果左移7为为0就不是汉字，否则是汉字
        if (ch.code.shr(7) == 0) {
            return null
        }
        return try {
            var uniCode: ByteArray = ch.toString().toByteArray(charset("GBK"))
            if (uniCode[0] in 1..127) { // 非汉字
                null
            } else {
                convert(uniCode)
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            null
        }
    }


    private const val GB_SP_DIFF = 160

    // 存放国标一级汉字不同读音的起始区位码
    private val secPosValueList = intArrayOf(
        1601, 1637, 1833, 2078, 2274, 2302,
        2433, 2594, 2787, 3106, 3212, 3472, 3635, 3722, 3730, 3858, 4027,
        4086, 4390, 4558, 4684, 4925, 5249, 5600
    )

    // 存放国标一级汉字不同读音的起始区位码对应读音
    private val firstLetter = charArrayOf(
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
        'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'w', 'x',
        'y', 'z'
    )

    /**
     * 获取一个汉字的拼音首字母。 GB码两个字节分别减去160，转换成10进制码组合就可以得到区位码
     * 例如汉字“你”的GB码是0xC4/0xE3，分别减去0xA0（160）就是0x24/0x43
     * 0x24转成10进制就是36，0x43是67，那么它的区位码就是3667，在对照表中读音为‘n’
     */
    private fun convert(bytes: ByteArray): Char {
        var result = '-'
        var i = 0
        while (i < bytes.size) {
            bytes[i] = (bytes[i].toInt() - GB_SP_DIFF).toByte()
            i++
        }
        val secPosValue: Int = bytes[0] * 100 + bytes[1]
        i = 0
        while (i < 23) {
            if (secPosValue >= secPosValueList[i] && secPosValue < secPosValueList[i + 1]) {
                result = firstLetter[i]
                break
            }
            i++
        }
        return result
    }

    /**
     * 格式化距离
     */
    fun friendJuli(juli: Int): String {
        val df = DecimalFormat("0.00")
        return if (juli >= 1000) {
            "${df.format(juli / 1000)}km"
        } else {
            "${df.format(juli)}m"
        }
    }

    private val PayCodeH5 = "^(https://h5.haier-ioc.com/scan\\?N=\\S*)$"
    private val PayImeiCode = "^(https://h5.haier\\-ioc.com/scan\\?IMEI=\\S*)$"
    private val RechargeCode = "^(https://h5.haier-ioc.com/scan\\?rechargeId=\\S*)$"
    private val RefundCode = "^(https://h5.haier-ioc.com/scan\\?refundId=\\S*)$"
    private val HaiLiCode1 = "^((http|https)://(uhome.haier.net|app.mrhi.cn)" +
            "/download/app/washcall/index.html\\?devid=\\S*)"
    private val HaiLiCode2 =
        "^((http|https)://(barcodewasher.haier.net/washer|bcw.haier.net)/barCode/\\S*)"
    private val IMEICode = "^\\d{15}\$"

    /**
     * 获取二码合一
     */
    fun getPayImeiCode(payCodeStr: String): String? = try {
        if (payCodeStr.matches(Regex(PayImeiCode))) {
            payCodeStr.split("\\?IMEI=".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        } else null
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        null
    }

    /**
     * 截取付款码
     */
    fun getPayCode(payCodeStr: String): String? = try {
        if (payCodeStr.matches(Regex(PayCodeH5))) {
            payCodeStr.split("\\?N=".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        } else if (payCodeStr.matches(Regex(HaiLiCode1))) {
            payCodeStr.split("\\?devid=".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        } else if (payCodeStr.matches(Regex(HaiLiCode2))) {
            payCodeStr.split("barCode/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        } else {
            null
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        null
    }

    /**
     * 是否是充值码
     */
    fun rechargeCode(codeStr: String): String? = try {
        if (codeStr.matches(Regex(RechargeCode))) {
            codeStr.split("\\?rechargeId=".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        } else null
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        null
    }

    /**
     * 是否是退款码
     */
    fun refundCode(codeStr: String): String? = try {
        if (codeStr.matches(Regex(RefundCode))) {
            codeStr.split("\\?refundId=".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[1]
        } else null
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        null
    }

    /**
     * 是否是付款码
     */
    fun isImeiCode(code: String?): Boolean = try {
        code?.matches(Regex(IMEICode)) ?: false
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        false
    }

    /**
     * 复制到剪切板
     */
    @JvmStatic
    fun copyToShear(text: String) {
        (Constants.APP_CONTEXT.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
            ClipData.newPlainText(null, text)
        )
        SToast.showToast(msgResId = R.string.copy_success)
    }

    /**
     * 格式化数字字符，正数+，负数-
     * @param numStr
     * @return
     */
    fun formatNumberStrOfStr(numStr: String?): String? {
        return try {
            numStr?.let {
                formatNumberStr(numStr.toDouble())
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 格式化数字字符，正数+，负数-
     * @param amount
     * @return
     */
    fun formatNumberStr(amount: Double): String {
        return (if (amount > 0) "+" else "") + if (0 == (amount * 100).toInt() % 100) amount.toInt()
        else String.format("%.2f", amount)
    }

    /**
     * 格式化数字字符，加上金额符号，正数，负数-
     * @param numStr
     * @return
     */
    @JvmStatic
    fun formatAmountStrOfStr(numStr: String?): String? {
        return try {
            numStr?.let {
                formatAmountStr(numStr.toDouble())
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            numStr
        }
    }

    /**
     * 格式化数字字符，加上金额符号，正数，负数-
     */
    @JvmStatic
    fun formatAmountStr(amount: Double): String {
        return (if (amount >= 0) "¥" else "-¥") + String.format("%.2f", abs(amount))
    }

    /**
     * 判断是否是整数
     */
    @JvmStatic
    fun checkAmountStrIsIntOrDouble(numStr: String?): String? {
        return try {
            numStr?.let {
                checkAmountIsIntOrDouble(numStr.toDouble())
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            numStr
        }
    }

    /**
     * 判断是否是整数
     */
    @JvmStatic
    fun checkAmountIsIntOrDouble(amount: Double): String = abs(amount).let {
        if (0 == (it * 100).toInt() % 100) "${it.toInt()}"
        else String.format("%.2f", it)
    }

    /**
     * 余额不足样式
     */
    fun formatBalancePayStyleStr(context: Context) = "余额  余额不足".let { content ->
        formatMultiStyleStr(
            content,
            arrayOf(
                AbsoluteSizeSpan(
                    DimensionUtils.sp2px(
                        10f,
                        context
                    )
                ),
                StyleSpan(Typeface.NORMAL),
                RoundBackgroundColorSpan(
                    ContextCompat.getColor(
                        context,
                        R.color.color_appoint_bg
                    ),
                    ContextCompat.getColor(
                        context,
                        R.color.color_ff630e
                    ),
                    DimensionUtils.dip2px(context, 4f)
                        .toFloat()
                )
            ),
            3, content.length
        )
    }

    private var textView: TextView? = null
    private var htmlTxt: String? = null

    fun loadHtmlText(tv: TextView?, html: String?) {
        if (null == tv || null == html) return
        textView = tv
        htmlTxt = html
        if (Build.VERSION.SDK_INT >= 24)
            textView?.text = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT, imageGetter, null);
        else
            textView?.text = Html.fromHtml(html, imageGetter, null)
    }

    private val map = mutableMapOf<String, Drawable>()

    private val imageGetter by lazy {
        Html.ImageGetter() {
            //多张图片情况根据drawableMap.get(s)获取drawable
            map[it] ?: run {
                initDrawable(it)
                null
            }
        }
    }

    private fun initDrawable(url: String) {
        GlobalScope.launch(Dispatchers.IO) {
            textView?.let {
                try {
                    Glide.with(it).load(url).submit().get()?.let { drawable ->
                        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                        map[url] = drawable
                        withContext(Dispatchers.Main) {
                            loadHtmlText(textView, htmlTxt)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun clearHtmlCaches() {
        textView = null
        htmlTxt = null
        map.clear()
    }
}
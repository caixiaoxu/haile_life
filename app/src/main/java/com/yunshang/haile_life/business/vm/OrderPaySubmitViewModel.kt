package com.yunshang.haile_life.business.vm

import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.extend.isGreaterThan0
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.string.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/3 11:47
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class OrderPaySubmitViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)
    val isDryer: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    val inValidOrder: MutableLiveData<Boolean> = MutableLiveData(false)
    var validTime: Int? = null
    val countDownTime: MutableLiveData<SpannableString> by lazy { MutableLiveData() }

    // 计时器
    var timer: Timer? = null

    /**
     * 检测有效时间
     */
    fun checkValidTime() {
        if (!validTime.isGreaterThan0()) return
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (validTime.isGreaterThan0()) {
                    inValidOrder.postValue(false)
                    val time = "%02d:%02d".format(validTime!! / 60, validTime!! % 60)
                    val content =
                        com.lsy.framelib.utils.StringUtils.getString(
                            R.string.page_time_prefix
                        ) + " " + time
                    countDownTime.postValue(
                        StringUtils.formatMultiStyleStr(
                            content,
                            arrayOf(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        Constants.APP_CONTEXT,
                                        R.color.color_ff630e,
                                    )
                                ),
                                AbsoluteSizeSpan(DimensionUtils.sp2px(26f)),
                                TypefaceSpan("money")
                            ), content.length - time.length, content.length
                        )
                    )
                    validTime = validTime!! - 1
                } else {
                    countDownTime.postValue(SpannableString("支付，已超时"))
                    inValidOrder.postValue(true)
                    jump.postValue(1)
                    timer?.cancel()
                }
            }
        }, 0, 1000)
    }

    private var isEachPayStatus = false
    fun eachRefreshPayStatus(orderNo: String, reStart: Boolean = false, callBack: () -> Unit) {
        if (isEachPayStatus == reStart) return

        if (!isEachPayStatus) {
            isEachPayStatus = true
        }

        launch({
            ApiRepository.dealApiResult(mOrderRepo.requestOrderDetailSimple(orderNo))
                ?.let {
                    if (isEachPayStatus) {
                        if (it.state != 100) {
                            isEachPayStatus = false
                            withContext(Dispatchers.Main) {
                                callBack()
                            }
                        } else {
                            Handler(Looper.getMainLooper()).postDelayed({
                                if (isEachPayStatus) {
                                    eachRefreshPayStatus(orderNo, false, callBack)
                                }
                            }, 3000)
                        }
                    }
                }
        })
    }

    override fun onCleared() {
        super.onCleared()
        isEachPayStatus = false
        timer?.cancel()
        timer = null
    }
}
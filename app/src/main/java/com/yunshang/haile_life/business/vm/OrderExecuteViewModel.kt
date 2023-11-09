package com.yunshang.haile_life.business.vm

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/3 16:19
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class OrderExecuteViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    private var coerceDeviceTime = 0L
    private val defaultDiff = 2 * 60 * 1000L
    private val coerceDeviceCount: MutableLiveData<Int> = MutableLiveData(0)

    fun startOrderDevice(orderNo: String?, callback: () -> Unit) {
        if (orderNo.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.startAppointOrderDevice(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "orderNo" to orderNo
                        )
                    )
                )
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        })
    }

    fun coerceDevice(context: Context, orderNo: String?) {
        if (orderNo.isNullOrEmpty()) return

        val diff = System.currentTimeMillis() - coerceDeviceTime
        if (0L == coerceDeviceTime || diff >= defaultDiff) {
            launch({
                ApiRepository.dealApiResult(
                    mOrderRepo.startByOrder(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "orderNo" to orderNo
                            )
                        )
                    )
                )
                coerceDeviceCount.postValue((coerceDeviceCount.value ?: 0) + 1)

                coerceDeviceTime = System.currentTimeMillis()
                withContext(Dispatchers.Main) {
                    showCoerceDevicePrompt(context, defaultDiff)
                }
            })
        } else {
            showCoerceDevicePrompt(context, defaultDiff - diff)
        }
    }

    private fun showCoerceDevicePrompt(context: Context, diff: Long) {
        val minute = diff / 1000 / 60
        val second = diff / 1000 % 60
        SToast.showToast(
            context,
            "强启设备间隔时间还需要${minute}分" + if (second > 0) "${second}秒" else "钟"
        )
    }

    /**
     * 结束订单
     */
    fun finishOrder(orderNo: String?) {
        if (orderNo.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.finishByOrder(
                    ApiRepository.createRequestBody(
                        hashMapOf("orderNo" to orderNo)
                    )
                )
            )
            Handler(Looper.getMainLooper()).postDelayed({
                jump.postValue(1)
            }, 1000)
        })
    }

    var totalTime: Int = 0
    val remainingTime: MutableLiveData<Int> by lazy {
        MutableLiveData()
    }

    // 计时器
    var timer: Timer? = null

    /**
     * 检测有效时间
     */
    fun checkValidTime() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                remainingTime.value = remainingTime.value!! - 1
            }
        }, 0, 1000)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        timer = null
    }
}
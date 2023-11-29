package com.yunshang.haile_life.business.vm

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.ceil

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

    private val defaultDiff = 2 * 60
    val coerceDeviceTime: MutableLiveData<Int> = MutableLiveData(0)
    val coerceDeviceTimeVal: LiveData<String> = coerceDeviceTime.map {
        StringUtils.getString(R.string.coerce_device) + if (it > 0) "（${it}s）" else ""
    }
    val coerceDeviceCount: MutableLiveData<Int> = MutableLiveData(0)

    /**
     * 强启设备
     */
    fun coerceDevice(context: Context, orderNo: String?, fulId: Int? = null) {
        if (orderNo.isNullOrEmpty()) return
        if (0 == coerceDeviceTime.value || coerceDeviceCount.value!! < 3) {
            launch({
                ApiRepository.dealApiResult(
                    mOrderRepo.startByOrder(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "orderNo" to orderNo,
                                "fulfillIdList" to fulId?.let { listOf(it) }
                            )
                        )
                    )
                )
                coerceDeviceTime.postValue(defaultDiff)
                coerceDeviceCount.postValue((coerceDeviceCount.value ?: 0) + 1)
                checkCoerceTime()

                withContext(Dispatchers.Main) {
                    SToast.showToast(context, "发送强启指令")
                }
            })
        }
    }

    // 计时器
    var timerCoerce: Timer? = null

    /**
     * 检测有效时间
     */
    private fun checkCoerceTime() {
        timerCoerce?.cancel()
        timerCoerce = Timer()
        timerCoerce?.schedule(object : TimerTask() {
            override fun run() {
                if (coerceDeviceTime.value!! > 0) {
                    coerceDeviceTime.postValue(coerceDeviceTime.value!! - 1)
                } else {
                    timerCoerce?.cancel()
                }
            }
        }, 0, 1000)
    }

    /**
     * 结束订单
     */
    fun finishOrder(orderNo: String?, callback: () -> Unit) {
        if (orderNo.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.finishByOrder(
                    ApiRepository.createRequestBody(
                        hashMapOf("orderNo" to orderNo)
                    )
                )
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        })
    }

    /**
     * 检测订单是否完成
     */
    fun checkOrderFinish(orderNo: String?) {
        if (orderNo.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(mOrderRepo.requestOrderDetailSimple(orderNo))
                ?.let {
                    if (it.state >= 1000) {
                        jump.postValue(1)
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            checkOrderFinish(orderNo)
                        }, 3000)
                    }
                }
        }, {}, showLoading = false)
    }

    var totalTime: Int = 0
    val remainingTime: MutableLiveData<Int> by lazy {
        MutableLiveData()
    }
    val remainingTimeVal: LiveData<String> = remainingTime.map {
        remainingTime.value?.let { second ->
            if (second > 0) {
                ceil(second * 1.0 / 60).toInt().toString()
            } else "1"
        } ?: ""
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
                remainingTime.value?.let { time ->
                    if (time > 0) {
                        remainingTime.postValue(time - 1)
                    } else {
                        timer?.cancel()
                    }
                } ?: timer?.cancel()
            }
        }, 0, 1000)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        timer = null
        timerCoerce?.cancel()
        timerCoerce = null
    }
}
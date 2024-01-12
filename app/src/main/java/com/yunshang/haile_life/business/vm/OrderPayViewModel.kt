package com.yunshang.haile_life.business.vm

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.business.apiService.CapitalService
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.BalanceEntity
import com.yunshang.haile_life.data.entities.OrderItem
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.thrid.AlipayHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.*

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/19 15:15
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class OrderPayViewModel : BaseViewModel() {
    private val mCapitalRepo = ApiRepository.apiClient(CapitalService::class.java)
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)
    var orderNo: String? = null
    var isAppoint: Boolean = false

    var timeRemaining: String? = null

    var isDrinking: Boolean = false

    val remaining: MutableLiveData<Long> by lazy {
        MutableLiveData()
    }

    val remainingVal: LiveData<String> = remaining.map {
        it?.let {
            if (0L == it) {
                LiveDataBus.post(BusEvents.PAY_OVERTIME_STATUS, true)
            }
            val minute = (it / 60000)
            val second = (it % 60000) / 1000
            String.format(
                "%02d:%02d",
                minute,
                second
            )
        } ?: "00:00"
    }

    var price: String = ""

    var orderItems: MutableList<OrderItem>? = null

    val balance: MutableLiveData<BalanceEntity> by lazy {
        MutableLiveData()
    }

    //支付方式 1001-余额 103--支付宝app支付 203--微信app支付
    var payMethod: Int = -1

    val prepayParam: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        launch({
            ApiRepository.dealApiResult(
                mCapitalRepo.requestBalance(
                    ApiRepository.createRequestBody("")
                )
            )?.let {
                balance.postValue(it)
            }
        })
    }

    // 计时器
    var timer: Timer? = null

    /**
     * 倒计时
     */
    fun countDownTimer() {
        launch({
            DateTimeUtils.formatDateFromString(timeRemaining)?.let { invidateTime ->
                timer?.cancel()
                timer = Timer()
                try {
                    timer?.schedule(object : TimerTask() {
                        override fun run() {
                            val temp = invidateTime.time - System.currentTimeMillis()
                            Timber.i("倒计时：$temp")
                            if (temp > 0) {
                                remaining.postValue(temp)
                            } else {
                                remaining.postValue(0L)
                                timer?.cancel()
                            }
                        }
                    }, 0, 1000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, showLoading = false)
    }

    fun requestPrePay(context: Context, callBack: ((orderNo: String?) -> Unit)? = null) {
        if (-1 == payMethod) return
        if (orderNo.isNullOrEmpty()) return

        launch({
            orderItems?.forEach {
                if (!verifyGoods(context, it)) {
                    return@launch
                }
            }

            callBack?.invoke(orderNo) ?:run {
                ApiRepository.dealApiResult(
                    mOrderRepo.prePay(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "orderNo" to orderNo,
                                "payMethod" to payMethod
                            )
                        )
                    )
                )?.let { prePay ->
                    if (1001 == payMethod) {
                        ApiRepository.dealApiResult(
                            mOrderRepo.balancePay(
                                ApiRepository.createRequestBody(
                                    hashMapOf(
                                        "prepayParam" to prePay.prepayParam
                                    )
                                )
                            )
                        )?.let {
                            requestAsyncPay()
                        }
                    } else {
                        prepayParam.postValue(prePay.prepayParam)
                    }
                }
            }
        })
    }


    fun alipay(activity: Activity, prepayParam: String) {
        launch({
            val resultStatus: String? = AlipayHelper.requestPay(activity, prepayParam).resultStatus
            //用户取消不去请求接口查询支付状态
            if (TextUtils.equals(resultStatus, "6001")) {
                return@launch
            }

            if (orderNo.isNullOrEmpty() || -1 == payMethod) {
                return@launch
            }
            requestAsyncPay()
        })
    }

    fun requestAsyncPayAsync() {
        if (orderNo.isNullOrEmpty() || -1 == payMethod) {
            return
        }
        launch({
            requestAsyncPay()
        })
    }

    private suspend fun requestAsyncPay() {
        ApiRepository.dealApiResult(
            mOrderRepo.asyncPay(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "orderNo" to orderNo,
                        "payMethod" to payMethod
                    )
                )
            )
        )?.let {
            LiveDataBus.post(BusEvents.PAY_SUCCESS_STATUS, true)
        }
    }

    private suspend fun verifyGoods(
        context: Context,
        orderItem: OrderItem
    ): Boolean {

        // 如果是饮水、沐浴或投放器，跳过验证
        if (DeviceCategory.isDrinkingOrShower(orderItem.categoryCode)
            || DeviceCategory.isDispenser(orderItem.categoryCode)
        ) {
            return true
        }

        val result = ApiRepository.dealApiResult(
            mDeviceRepo.verifyGoods(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "goodsId" to orderItem.goodsId,
                        "categoryCode" to orderItem.categoryCode,
                    )
                )
            )
        )

        return if (false == result?.isSuccess) {
            withContext(Dispatchers.Main) {
                SToast.showToast(context, result.msg)
            }
            false
        } else true
    }
}
package com.yunshang.haile_life.business.vm

import android.app.Activity
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.CapitalService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.entities.BalanceEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.thrid.AlipayHelper

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
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var orderNo: String? = null

    var timeRemaining: String? = null

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

    /**
     * 倒计时
     */
    fun countDownTimer() {
        launch({
            DateTimeUtils.formatDateFromString(timeRemaining)?.let { invidateTime ->
                var temp = invidateTime.time - System.currentTimeMillis()
                while (temp > 0) {
                    remaining.postValue(temp)
                    temp = invidateTime.time - System.currentTimeMillis()
                }
                remaining.postValue(0L)
            }
        }, showLoading = false)
    }

    fun requestPrePay() {
        if (-1 == payMethod) return
        if (orderNo.isNullOrEmpty()) return

        launch({
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
}
package com.yunshang.haile_life.business.vm

import android.app.Activity
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.thrid.AlipayHelper

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/5 19:45
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class OrderDetailViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var orderNo: String? = null

    //支付方式 1001-余额 103--支付宝app支付 203--微信app支付
    var payMethod: Int = -1

    val prepayParam: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val orderDetail: MutableLiveData<OrderEntity> by lazy {
        MutableLiveData()
    }

    val showContactShop: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            detail.buyerPhone.isNotEmpty() && 100 != detail.state
        } ?: false
    }

    val showCancelOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            100 == detail.state || 500 == detail.state
        } ?: false
    }

    val showPayOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            100 == detail.state
        } ?: false
    }

    // 是否显示底部
    val showBottom: MediatorLiveData<Boolean> = MediatorLiveData(false).apply {
        addSource(showContactShop) {
            value = checkShowAnyBtn()
        }
        addSource(showCancelOrder) {
            value = checkShowAnyBtn()
        }
        addSource(showPayOrder) {
            value = checkShowAnyBtn()
        }
    }

    private fun checkShowAnyBtn() =
        (true == showContactShop.value || true == showCancelOrder.value || true == showPayOrder.value)

    fun requestOrderDetailAsync() {
        if (orderNo.isNullOrEmpty()) return
        launch({ requestOrderDetail() })
    }

    private suspend fun requestOrderDetail() {
        ApiRepository.dealApiResult(mOrderRepo.requestOrderDetail(orderNo!!))?.let {
            orderDetail.postValue(it)
        }
    }

    fun cancelOrder(v: View) {
        if (orderNo.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.cancelOrder(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "orderNo" to orderNo
                        )
                    )
                )
            )?.let {
                requestOrderDetail()
            }
        })
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
            requestOrderDetail()
        }
    }
}
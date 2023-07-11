package com.yunshang.haile_life.business.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.CapitalService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.entities.OrderCountOfStatusEntity
import com.yunshang.haile_life.data.model.ApiRepository

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
class MineViewModel : BaseViewModel() {
    private val mCapitalRepo = ApiRepository.apiClient(CapitalService::class.java)
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    val balance: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val starfishTotal: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val couponTotal: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val orderCountOfStatus: MutableLiveData<MutableList<OrderCountOfStatusEntity>> =
        MutableLiveData()

    val waitPay: LiveData<String> = orderCountOfStatus.map {
        it?.find { item -> 100 == item.orderState }?.orderCount?.toString() ?: ""
    }

    val running: LiveData<String> = orderCountOfStatus.map {
        it?.find { item -> 500 == item.orderState }?.orderCount?.toString() ?: ""
    }

    val finish: LiveData<String> = orderCountOfStatus.map {
        it?.find { item -> 1000 == item.orderState }?.orderCount?.toString() ?: ""
    }

    val refund: LiveData<String> = orderCountOfStatus.map {
        it?.find { item -> 2099 == item.orderState }?.orderCount?.toString() ?: ""
    }

    fun requestData(request: suspend () -> Unit) {
        launch({
            request()
            requestBalance()
            requestStarfishTotal()
            requestCouponTotal()
            requestOrderCountOfStatus()
        })
    }

    fun requestUserInfo(request: suspend () -> Unit) {
        launch({ request() })
    }

    fun requestBalanceAsync() {
        launch({
            requestBalance()
        })
    }

    private suspend fun requestBalance() {
        ApiRepository.dealApiResult(
            mCapitalRepo.requestBalance(
                ApiRepository.createRequestBody("")
            )
        )?.let {
            balance.postValue(it.amount)
        }
    }

    fun requestStarfishTotalAsync() {
        launch({
            requestStarfishTotal()
        })
    }

    private suspend fun requestStarfishTotal() {
        ApiRepository.dealApiResult(
            mCapitalRepo.requestStarfishTotal()
        )?.let {
            starfishTotal.postValue(it)
        }
    }

    fun requestCouponTotalAsync() {
        launch({
            requestCouponTotal()
        })
    }

    private suspend fun requestCouponTotal() {
        ApiRepository.dealApiResult(
            mCapitalRepo.requestCouponTotal()
        )?.let {
            couponTotal.postValue(it)
        }
    }

    fun requestOrderCountOfStatusAsync() {
        launch({
            requestOrderCountOfStatus()
        })
    }

    private suspend fun requestOrderCountOfStatus() {
        ApiRepository.dealApiResult(
            mOrderRepo.requestOrderCountOfStatus()
        )?.let {
            orderCountOfStatus.postValue(it)
        }
    }
}
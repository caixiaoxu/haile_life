package com.yunshang.haile_life.business.vm

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

    var isAppoint: Boolean = false

    var formScan: Boolean = false

    val changeUseModel: MutableLiveData<Boolean> = MutableLiveData(false)

    val orderDetail: MutableLiveData<OrderEntity> by lazy {
        MutableLiveData()
    }

    val showContactShop: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (formScan)
                false
            else
                detail.serviceTelephone.isNotEmpty()
        } ?: false
    }

    val showCancelOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (formScan) {
                false
            } else {
                if (isAppoint) 0 == detail.appointmentState || 1 == detail.appointmentState
                else 100 == detail.state
            }
        } ?: false
    }

    val showPayOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (formScan) {
                false
            } else {
                if (isAppoint) 0 == detail.appointmentState else 100 == detail.state
            }
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
        addSource(changeUseModel) {
            value = checkShowAnyBtn()
        }
    }

    private fun checkShowAnyBtn() =
        (((true == showContactShop.value || true == showCancelOrder.value || true == showPayOrder.value) && !formScan) || (formScan && true == changeUseModel.value))

    fun requestOrderDetailAsync() {
        if (orderNo.isNullOrEmpty()) return
        launch({ requestOrderDetail() })
    }

    private suspend fun requestOrderDetail() {
        ApiRepository.dealApiResult(mOrderRepo.requestOrderDetail(orderNo!!))?.let {
            orderDetail.postValue(it)
        }
    }

    fun cancelOrder() {
        if (orderNo.isNullOrEmpty()) return

        val body = ApiRepository.createRequestBody(
            hashMapOf(
                "orderNo" to orderNo
            )
        )
        launch({
            ApiRepository.dealApiResult(
                if (isAppoint) mOrderRepo.cancelAppointOrder(body) else mOrderRepo.cancelOrder(body)
            )
            LiveDataBus.post(BusEvents.ORDER_CANCEL_STATUS, true)
            requestOrderDetail()
        })
    }

    fun useAppointOrder(v: View) {
        if (orderNo.isNullOrEmpty()) return

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.useAppointOrder(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "orderNo" to orderNo
                        )
                    )
                )
            )
            LiveDataBus.post(BusEvents.APPOINT_ORDER_USE_STATUS, true)
        })
    }
}
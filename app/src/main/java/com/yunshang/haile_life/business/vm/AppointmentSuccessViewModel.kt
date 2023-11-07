package com.yunshang.haile_life.business.vm

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.entities.AppointmentStateListEntity
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.delay

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/3 11:14
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AppointmentSuccessViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var orderNo: String? = null

    val orderDetails: MutableLiveData<OrderEntity> by lazy { MutableLiveData() }
    val appointStateList: MutableLiveData<AppointmentStateListEntity> by lazy { MutableLiveData() }

    fun requestData() {
        if (orderNo.isNullOrEmpty()) return

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestOrderDetail(orderNo!!)
            )?.let {
                orderDetails.postValue(it)
            }

            ApiRepository.dealApiResult(
                mOrderRepo.requestAppointStateList(
                    ApiRepository.createRequestBody(
                        hashMapOf("orderNo" to orderNo!!)
                    )
                )
            )?.let {
                appointStateList.postValue(it)
            }
        })
    }


    /**
     * 取消订单
     */
    fun cancelOrder(v:View) {
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
            )
            LiveDataBus.post(BusEvents.ORDER_CANCEL_STATUS, true)
            jump.postValue(1)
        })
    }
}
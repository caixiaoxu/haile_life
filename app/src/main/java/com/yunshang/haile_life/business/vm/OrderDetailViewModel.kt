package com.yunshang.haile_life.business.vm

import android.os.Handler
import android.os.Looper
import android.text.SpannableString
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
import com.yunshang.haile_life.utils.DateTimeUtils
import kotlinx.coroutines.delay
import timber.log.Timber
import java.util.*

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

    val isAppoint: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    val formScan: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    val changeUseModel: MutableLiveData<Boolean> = MutableLiveData(false)

    val orderDetail: MutableLiveData<OrderEntity> by lazy {
        MutableLiveData()
    }

    val showContactShop: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (true == formScan.value)
                false
            else
                detail.serviceTelephone.isNotEmpty()
        } ?: false
    }

    val showCancelOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (true == formScan.value) {
                false
            } else {
                if (true == isAppoint.value) 0 == detail.appointmentState || 1 == detail.appointmentState
                else 100 == detail.state
            }
        } ?: false
    }

    val showPayOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (true == formScan.value) {
                false
            } else {
                if (true == isAppoint.value) 0 == detail.appointmentState else 100 == detail.state
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
        (((true == showContactShop.value || true == showCancelOrder.value || true == showPayOrder.value) && true != formScan.value) || (true == formScan.value && false == changeUseModel.value))

    fun requestOrderDetailAsync() {
        if (orderNo.isNullOrEmpty()) return
        timer?.cancel()
        launch({ requestOrderDetail() })
    }

    private suspend fun requestOrderDetail() {
        ApiRepository.dealApiResult(mOrderRepo.requestOrderDetail(orderNo!!))?.let {
            orderDetail.postValue(it)
            getOrderStatusVal(it)
        }
    }

    /**
     * 取消订单
     */
    fun cancelOrder() {
        if (orderNo.isNullOrEmpty()) return

        val body = ApiRepository.createRequestBody(
            hashMapOf(
                "orderNo" to orderNo
            )
        )
        launch({
            ApiRepository.dealApiResult(
                if (true == isAppoint.value) mOrderRepo.cancelAppointOrder(body) else mOrderRepo.cancelOrder(
                    body
                )
            )
            LiveDataBus.post(BusEvents.ORDER_CANCEL_STATUS, true)
            delay(2_000)
            requestOrderDetail()
        })
    }

    /**
     * 使用预约时间
     */
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
            // 转换为正常订单
            isAppoint.postValue(false)
            formScan.postValue(false)
            changeUseModel.postValue(false)

            delay(2_000)
            requestOrderDetail()
        })
    }

    val remaining: MutableLiveData<Long> by lazy {
        MutableLiveData()
    }

    val orderStatusDesc: MutableLiveData<SpannableString> by lazy {
        MutableLiveData()
    }

    // 计时器
    var timer: Timer? = null

    /**
     * 订单状态描述
     */
    fun getOrderStatusVal(detail: OrderEntity) {
        if (true == isAppoint.value) {
            orderStatusDesc.postValue(detail.getOrderDetailAppointTimePrompt())
        } else {
            if (100 == detail.state) {
                DateTimeUtils.formatDateFromString(detail.invalidTime)
                    ?.let { invidateTime ->
                        //后端脚本延迟15秒
                        val time = invidateTime.time + 15000
                        timer?.cancel()
                        timer = Timer()
                        try {
                            timer?.schedule(object : TimerTask() {
                                override fun run() {
                                    val temp =
                                        time - System.currentTimeMillis()
                                    Timber.i("倒计时：$temp")
                                    if (temp > 0) {
                                        remaining.postValue(temp)
                                    } else {
                                        LiveDataBus.post(BusEvents.PAY_OVERTIME_STATUS, true)
                                        timer?.cancel()
                                    }
                                }
                            }, 0, 1000)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            } else {
                orderStatusDesc.postValue(detail.getOrderDetailFinishTimePrompt())
            }
        }
    }
}
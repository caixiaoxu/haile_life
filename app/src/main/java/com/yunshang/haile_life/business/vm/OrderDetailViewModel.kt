package com.yunshang.haile_life.business.vm

import android.content.Context
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
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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


    val formScan: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    val changeUseModel: MutableLiveData<Boolean> = MutableLiveData(false)

    val orderDetail: MutableLiveData<OrderEntity> by lazy {
        MutableLiveData()
    }

    val isAppoint: LiveData<Boolean> = orderDetail.map { "300" == it.orderType }

    val showContactShop: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (true == formScan.value)
                false
            else
                detail.serviceTelephone.isNotEmpty()
        } ?: false
    }

    val showCoerceDevice: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (true == formScan.value)
                false
            else
                (DeviceCategory.isWashingOrShoes(detail.orderItemList.firstOrNull()?.categoryCode)
                        || DeviceCategory.isDryer(detail.orderItemList.firstOrNull()?.categoryCode))
                        && 500 == detail.state
        } ?: false
    }

    val showFinishOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (true == formScan.value)
                false
            else
                (DeviceCategory.isWashingOrShoes(detail.orderItemList.firstOrNull()?.categoryCode)
                        || DeviceCategory.isDryer(detail.orderItemList.firstOrNull()?.categoryCode))
                        && 500 == detail.state
        } ?: false
    }

    val showCancelOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (true == formScan.value) {
                false
            } else {
                if (true == isAppoint.value) 0 == detail.appointmentState || 1 == detail.appointmentState
                else 100 == detail.state && !DeviceCategory.isDrinking(detail.orderItemList.firstOrNull()?.categoryCode)
            }
        } ?: false
    }

    val showDeleteOrder: LiveData<Boolean> = orderDetail.map {
        it?.let { detail ->
            if (true == formScan.value) {
                false
            } else {
                if (true == isAppoint.value) false
                else 1000 == detail.state || 2099 == detail.state
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

    fun requestOrderDetailAsync(showLoading: Boolean = true) {
        if (orderNo.isNullOrEmpty()) return
        timer?.cancel()
        launch({ requestOrderDetail() }, showLoading = showLoading)
    }

    private suspend fun requestOrderDetail() {
        ApiRepository.dealApiResult(mOrderRepo.requestOrderDetail(orderNo!!))?.let {
            orderDetail.postValue(it)
            getOrderStatusVal(it)
            startStateTime(it)
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
     * 删除订单
     */
    fun deleteOrder(callBack: () -> Unit) {
        if (orderNo.isNullOrEmpty()) return

        val body = ApiRepository.createRequestBody(
            hashMapOf(
                "orderNo" to orderNo
            )
        )
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.deleteOrder(
                    body
                )
            )
            LiveDataBus.post(BusEvents.ORDER_DELETE_STATUS, orderNo!!)
            withContext(Dispatchers.Main) {
                callBack()
            }
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
        stopShowRemnantTime = false
        if (true == isAppoint.value) {
            orderStatusDesc.postValue(detail.getOrderDetailAppointTimePrompt())
        } else {
            if (100 == detail.state && !DeviceCategory.isDrinking(detail.orderItemList.firstOrNull()?.categoryCode)) {
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
            } else if (500 == detail.state && DeviceCategory.isWashingOrShoes(detail.orderItemList.firstOrNull()?.categoryCode)
                || DeviceCategory.isDryer(detail.orderItemList.firstOrNull()?.categoryCode)
            ) {
                stopShowRemnantTime = true
                showRemnantTime(detail)
            } else {
                orderStatusDesc.postValue(detail.getOrderDetailFinishTimePrompt())
            }
        }
    }

    private var stopShowRemnantTime = true

    /**
     * 显示剩余时间
     */
    private fun showRemnantTime(detail: OrderEntity) {
        if (500 == detail.state && stopShowRemnantTime) {
            orderStatusDesc.postValue(detail.calculateRemnantTime())
            DateTimeUtils.formatDateFromString(detail.orderItemList.firstOrNull()?.finishTime)
                ?.let {
                    if ((it.time - System.currentTimeMillis()) > 0) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            showRemnantTime(detail)
                        }, 1000)
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            LiveDataBus.post(BusEvents.PROMPT_POPUP, false)
                            requestOrderDetailAsync(false)
                        }, 15000)
                    }
                }
        }
    }

    var orderStateTimer: Timer? = null
    private fun startStateTime(detail: OrderEntity) {
        // 如果是饮水并且，未创建过监听，开始一个新的监听
        if (50 == detail.state) {
            if (null == orderStateTimer) {
                orderStateTimer = Timer()
                orderStateTimer?.schedule(object : TimerTask() {
                    override fun run() {
                        if (orderNo.isNullOrEmpty() || null == orderDetail.value) return
                        launch({
                            ApiRepository.dealApiResult(mOrderRepo.requestOrderDetailSimple(orderNo!!))
                                ?.let {
                                    if (it.state != orderDetail.value!!.state) {
                                        requestOrderDetail()
                                    }
                                }
                        }, {}, showLoading = false)
                    }
                }, 2000, 2000)
            }
        } else {
            orderStateTimer?.cancel()
            orderStateTimer = null
        }
    }

    private var coerceDeviceTime = 0L
    private val defaultDiff = 2 * 60 * 1000L
    val coerceDeviceCount: MutableLiveData<Int> = MutableLiveData(0)

    fun coerceDevice(v: View) {
        orderDetail.value?.let { detail ->
            var diff = System.currentTimeMillis() - coerceDeviceTime
            if (0L == coerceDeviceTime || diff >= defaultDiff) {
                launch({
                    ApiRepository.dealApiResult(
                        mOrderRepo.startByOrder(
                            ApiRepository.createRequestBody(
                                hashMapOf(
                                    "orderNo" to detail.orderNo
                                )
                            )
                        )
                    )
                    coerceDeviceCount.postValue((coerceDeviceCount.value ?: 0) + 1)

                    coerceDeviceTime = System.currentTimeMillis()
                    withContext(Dispatchers.Main) {
                        showCoerceDevicePrompt(v.context, defaultDiff)
                    }
                })
            } else {
                showCoerceDevicePrompt(v.context, defaultDiff - diff)
            }
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
    fun finishOrder(v: View) {
        orderDetail.value?.let { detail ->
            launch({
                ApiRepository.dealApiResult(
                    mOrderRepo.finishByOrder(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "orderNo" to detail.orderNo
                            )
                        )
                    )
                )
                Handler(Looper.getMainLooper()).postDelayed({
                    requestOrderDetailAsync(false)
                }, 1000)
            })
        }
    }
}
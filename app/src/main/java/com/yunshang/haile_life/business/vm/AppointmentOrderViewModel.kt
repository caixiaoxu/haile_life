package com.yunshang.haile_life.business.vm

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.CapitalService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.entities.BalanceEntity
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.data.entities.TradePreviewEntity
import com.yunshang.haile_life.data.entities.TradePreviewParticipate
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/8 16:06
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AppointmentOrderViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)
    private val mCapitalRepo = ApiRepository.apiClient(CapitalService::class.java)

    var orderNo: String? = null

    val orderDetails: MutableLiveData<OrderEntity> by lazy { MutableLiveData() }

    fun requestData() {
        if (orderNo.isNullOrEmpty()) return

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestOrderDetail(orderNo!!)
            )?.let {
                orderDetails.postValue(it)
            }
        })
    }

    /**
     * 取消订单
     */
    fun cancelOrder() {
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

    /** ------------------------------预约成功------------------------------**/
    /**
     * 检测排队情况
     */
    fun checkLineUp() {
        if (orderNo.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestAppointStateQuery(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "orderNo" to orderNo
                        )
                    )
                )
            )?.let {
                when (it.appointmentState) {
                    5 -> {
                        //跳转到验证界面
                        requestData()
                    }
                    1 -> {
                        // 同一状态循环
                        Handler(Looper.getMainLooper()).postDelayed({
                            checkLineUp()
                        }, 3000)
                    }
                    else -> {
                        jump.postValue(1)
                    }
                }
            }
        }, showLoading = false)
    }

    /** ------------------------------预约成功------------------------------**/

    /** ------------------------------预约支付------------------------------**/
    val tradePreview: MutableLiveData<TradePreviewEntity> by lazy { MutableLiveData() }

    val balance: MutableLiveData<BalanceEntity> by lazy {
        MutableLiveData()
    }

    var selectParticipate: MutableList<TradePreviewParticipate>? = null

    private fun getCommonParams(autoSelect: Boolean) = hashMapOf<String, Any?>(
        "orderNo" to orderNo,
        "autoSelectPromotion" to if (autoSelect) (null == tradePreview.value) else false
    ).also { params ->
        if (null != selectParticipate) {
            params["promotionList"] = selectParticipate!!.map {
                hashMapOf(
                    "promotionId" to it.promotionId,
                    "promotionProduct" to it.promotionProduct,
                    "assetId" to it.assetId,
                    "shopId" to it.shopId,
                )
            }
        }
    }

    fun requestPreview() {
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestTradePreview(
                    ApiRepository.createRequestBody(getCommonParams(true))
                )
            )?.let {
                tradePreview.postValue(it)
            }

            ApiRepository.dealApiResult(
                mCapitalRepo.requestBalance(
                    ApiRepository.createRequestBody("")
                )
            )?.let {
                balance.postValue(it)
            }
        })
    }
    /** ------------------------------预约支付------------------------------**/
}
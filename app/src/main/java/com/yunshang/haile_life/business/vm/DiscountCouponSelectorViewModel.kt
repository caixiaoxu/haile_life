package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.TradePreviewEntity
import com.yunshang.haile_life.data.entities.TradePreviewParticipate
import com.yunshang.haile_life.data.entities.TradePreviewPromotion
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.DateTimeUtils
import java.util.*

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/3 11:47
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class DiscountCouponSelectorViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var goods: MutableList<IntentParams.OrderSubmitParams.OrderSubmitGood> = mutableListOf()

    var appointmentTime: Long = -1L

    var promotionProduct: Int = -1

    val promotion: MutableLiveData<TradePreviewPromotion> by lazy {
        MutableLiveData()
    }
    var selectParticipate: MutableList<TradePreviewParticipate>? = null

    val tradePreview: MutableLiveData<TradePreviewEntity> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        if (goods.isEmpty()) return

        val params = hashMapOf(
            "autoSelectPromotion" to false,
            "purchaseList" to goods.map {
                hashMapOf(
                    "goodsId" to it.goodId,
                    "goodsItemId" to it.goodItmId,
                    "soldType" to if (DeviceCategory.isDryerOrHairOrDispenser(it.categoryCode)) 2 else 1,
                    "num" to it.num,
                )
            },
        )

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

        if (-1L != appointmentTime) {
            val appoint = hashMapOf(
                "reserveTime" to DateTimeUtils.formatDateTime(Date(appointmentTime))
            )
            params["optionalInfo"] = hashMapOf(
                "reserveInfo" to appoint
            )
        }

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestTradePreview(
                    ApiRepository.createRequestBody(params)
                )
            )?.let {
                tradePreview.postValue(it)
            }
        })
    }
}
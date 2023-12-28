package com.yunshang.haile_life.business.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.TradePreviewEntity
import com.yunshang.haile_life.data.entities.TradePreviewParticipate
import com.yunshang.haile_life.data.entities.TradePreviewPromotion
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.rule.IndicatorEntity
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

    var orderNo: String? = null

    var promotionProduct: Int = -1

    val promotion: MutableLiveData<TradePreviewPromotion> by lazy {
        MutableLiveData()
    }

    var selectParticipate: MutableList<TradePreviewParticipate>? = null
    var otherSelectParticipate: MutableList<TradePreviewParticipate>? = null

    val tradePreview: MutableLiveData<TradePreviewEntity> by lazy {
        MutableLiveData()
    }

    val mCouponIndicators: LiveData<List<IndicatorEntity<Int>>> = tradePreview.map { trade ->
        val num =
            trade.promotionList.find { item -> promotionProduct == item.promotionProduct }?.options?.count { item -> item.available }
                ?: 0
        arrayListOf(
            IndicatorEntity("可用优惠", num, 1),
            IndicatorEntity("不可用", 0, 2),
        )
    }

    var selectCouponIndicator: Int = 1

    fun requestData() {
        if (orderNo.isNullOrEmpty()) return

        val params = hashMapOf<String, Any?>(
            "orderNo" to orderNo,
            "autoSelectPromotion" to false
        )

        if (null != selectParticipate) {
            val list = mutableListOf<TradePreviewParticipate>()
            otherSelectParticipate?.let {
                list.addAll(it)
            }
            selectParticipate?.let {
                list.addAll(it)
            }
            params["promotionList"] = list.map {
                hashMapOf(
                    "promotionId" to it.promotionId,
                    "promotionProduct" to it.promotionProduct,
                    "assetId" to it.assetId,
                    "shopId" to it.shopId,
                )
            }
        }

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestUnderWayOrderPreviewV2(
                    ApiRepository.createRequestBody(params)
                )
            )?.let {
                tradePreview.postValue(it)
            }
        })
    }
}
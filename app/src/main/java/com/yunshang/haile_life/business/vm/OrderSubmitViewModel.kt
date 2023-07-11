package com.yunshang.haile_life.business.vm

import android.app.Activity
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.TradePreviewEntity
import com.yunshang.haile_life.data.entities.TradePreviewParticipate
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.string.StringUtils
import com.yunshang.haile_life.utils.thrid.AlipayHelper
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
class OrderSubmitViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    val goods: MutableLiveData<MutableList<IntentParams.OrderSubmitParams.OrderSubmitGood>> =
        MutableLiveData(mutableListOf())

    val reserveTime: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val deviceName: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val shopAddress: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val shopDeviceVal: MediatorLiveData<SpannableString> =
        MediatorLiveData(SpannableString("")).apply {
            addSource(deviceName) {
                value = formatShopDeviceVal()
            }
            addSource(shopAddress) {
                value = formatShopDeviceVal()
            }
        }

    private fun formatShopDeviceVal(): SpannableString {
        val name = deviceName.value ?: ""
        val address = shopAddress.value ?: ""

        return StringUtils.formatMultiStyleStr(
            "${name}\n$address",
            arrayOf(
                ForegroundColorSpan(
                    ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_black_85),
                ),
                AbsoluteSizeSpan(DimensionUtils.sp2px(16f))
            ),
            0, name.length
        )
    }

    val reserveTimeVal: LiveData<SpannableString> = reserveTime.map {
        it?.let {
            val useDate = DateTimeUtils.formatDateFromString(it)
            val time = if (DateTimeUtils.isSameDay(useDate, Date())) {
                "今天${DateTimeUtils.formatDateTime(useDate, "HH:mm")}"
            } else it
            val content = com.lsy.framelib.utils.StringUtils.getString(
                R.string.order_submit_used_time_prompt,
                time
            )
            StringUtils.formatMultiStyleStr(
                content, arrayOf(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            Constants.APP_CONTEXT, R.color.secondColorPrimary
                        )
                    )
                ), 3, 3 + time.length
            )
        } ?: SpannableString("")
    }

    val isDryer: LiveData<Boolean> = goods.map {
        if (it.isNotEmpty()) {
            DeviceCategory.isDryerOrHair(it.first().categoryCode)
        } else false
    }

    var selectParticipate: MutableList<TradePreviewParticipate>? = null

    val tradePreview: MutableLiveData<TradePreviewEntity> by lazy {
        MutableLiveData()
    }

    //支付方式 1001-余额 103--支付宝app支付 203--微信app支付
    var payMethod: Int = -1

    var orderNo: String = ""
    val prepayParam: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    private fun getCommonParams(autoSelect: Boolean) = hashMapOf(
        "autoSelectPromotion" to if (autoSelect) (null == tradePreview.value) else false,
        "purchaseList" to goods.value!!.map {
            hashMapOf(
                "goodsId" to it.goodId,
                "goodsItemId" to it.goodItmId,
                "soldType" to if (DeviceCategory.isDryerOrHair(it.categoryCode)) 2 else 1,
                "num" to it.num,
            )
        },
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

        if (!(reserveTime.value.isNullOrEmpty())) {
            val appoint = hashMapOf(
                "reserveTime" to reserveTime.value
            )
            params["optionalInfo"] = hashMapOf(
                "reserveInfo" to appoint
            )
            params["orderSubType"] = 301
        }
    }

    fun requestData() {
        if (goods.value.isNullOrEmpty()) return

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestTradePreview(
                    ApiRepository.createRequestBody(getCommonParams(true))
                )
            )?.let {
                tradePreview.postValue(it)
            }
        })
    }

    fun requestPrePay(view: View) {
        if (goods.value.isNullOrEmpty()) return

        if (-1 == payMethod) {
            SToast.showToast(view.context, "请选择支付方式")
            return
        }

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.createTrade(
                    ApiRepository.createRequestBody(
                        getCommonParams(false)
                    )
                )
            )?.let {
                orderNo = it.orderNo
                LiveDataBus.post(BusEvents.ORDER_SUBMIT_STATUS, true)
                ApiRepository.dealApiResult(
                    mOrderRepo.prePay(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "orderNo" to it.orderNo,
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

            if (orderNo.isEmpty() || -1 == payMethod) {
                return@launch
            }
            requestAsyncPay()
        })
    }

    fun requestAsyncPayAsync() {
        if (orderNo.isEmpty() || -1 == payMethod) {
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
            LiveDataBus.post(BusEvents.PAY_SUCCESS_STATUS, it.success)
        }
    }
}
package com.yunshang.haile_life.business.vm

import android.app.Activity
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.business.apiService.CapitalService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.entities.BalanceEntity
import com.yunshang.haile_life.data.entities.Reward
import com.yunshang.haile_life.data.entities.ShopStarfishListEntity
import com.yunshang.haile_life.data.entities.ShopStarfishTotalEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.thrid.AlipayHelper

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/7 13:55
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class RechargeStarfishViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)
    private val mCapitalRepo = ApiRepository.apiClient(CapitalService::class.java)
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var shopId: Int = -1

    //支付方式 1001-余额 103--支付宝app支付 203--微信app支付
    var payMethod: Int = -1
    var orderNo: String = ""
    val prepayParam: MutableLiveData<String> by lazy {
        MutableLiveData()
    }
    val asyncPay: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    val balance: MutableLiveData<BalanceEntity> by lazy {
        MutableLiveData()
    }

    val selectGoodsItem: MutableLiveData<Reward> by lazy {
        MutableLiveData()
    }

    val shopStarfishTotal: MutableLiveData<ShopStarfishTotalEntity> by lazy {
        MutableLiveData()
    }
    val shopStarfishList: MutableLiveData<ShopStarfishListEntity> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        if (-1 == shopId) return
        launch({
            ApiRepository.dealApiResult(
                mCapitalRepo.requestShopStarfishTotal(
                    ApiRepository.createRequestBody(
                        hashMapOf("shopId" to shopId)
                    )
                )
            )?.let {
                shopStarfishTotal.postValue(it)
            }
            ApiRepository.dealApiResult(
                mShopRepo.requestShopRechargeList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "shopId" to shopId
                        )
                    )
                )
            )?.let {
                shopStarfishList.postValue(it)
                it.rewardList.firstOrNull()?.let { reward ->
                    selectGoodsItem.postValue(reward)
                }
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

    fun requestRecharge(view: View) {
        if (null == selectGoodsItem.value) {
            SToast.showToast(view.context, "请选择充值金额")
            return
        }
        if (-1 == payMethod) {
            SToast.showToast(view.context, "请选择支付方式")
            return
        }
        if (null == shopStarfishList.value) return

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.createTrade(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "autoSelectPromotion" to false,
                            "orderSubCategory" to 3001,
                            "purchaseList" to arrayOf(
                                hashMapOf<String, Any?>(
                                    "goodsId" to shopStarfishList.value?.goodsId,
                                    "goodsItemId" to selectGoodsItem.value?.goodsItemId,
                                    "soldType" to 1,
                                    "num" to 1,
                                )
                            ),
                        )
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
            asyncPay.postValue(it.success)
            LiveDataBus.post(BusEvents.RECHARGE_SUCCESS_STATUS, true)
        }
    }
}
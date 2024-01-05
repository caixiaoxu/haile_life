package com.yunshang.haile_life.business.vm

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.business.apiService.*
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.thrid.AlipayHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
class OrderStatusViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mCapitalRepo = ApiRepository.apiClient(CapitalService::class.java)
    private val mUserRepo = ApiRepository.apiClient(LoginUserService::class.java)
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    var orderNo: String? = null

    val orderDetails: MutableLiveData<OrderEntity> by lazy { MutableLiveData() }

    // 控制关注弹窗的显示
    var statusType = -1

    fun requestData(showLoading: Boolean = true) {
        if (orderNo.isNullOrEmpty()) return

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestOrderDetail(orderNo!!)
            )?.let {
                orderDetails.postValue(it)
            }
        }, showLoading = showLoading)
    }

    fun requestOfficialAccounts(callBack: (officialAccounts: OfficialAccountsEntity) -> Unit) {
        launch({
            ApiRepository.dealApiResult(
                mUserRepo.requestOfficialAccounts()
            )?.let {
                callBack(it)
            }
        }, showLoading = false)
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

    fun startSelfClean() {
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.startSelfClean(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "itemId" to tradePreview.value?.selfCleanInfo?.selfCleanItemId,
                            "tradeNo" to orderNo,
                        )
                    )
                )
            )
            requestData(false)
            requestPreviewSync()
            querySelfCleanState()
        })
    }

    val selfCleanRefresh: MutableLiveData<SelfCleanRefreshEntity> by lazy {
        MutableLiveData()
    }

    private fun querySelfCleanState() {
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestSelfCleanRefresh(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "tradeNo" to orderNo
                        )
                    )
                )
            )?.let {
                selfCleanRefresh.postValue(it)
                if (it.status < 40) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        querySelfCleanState()
                    }, 10000)
                }
            }
        }, showLoading = false)
    }

    val tradePreview: MutableLiveData<TradePreviewEntity> by lazy { MutableLiveData() }

    val balance: MutableLiveData<BalanceEntity> by lazy {
        MutableLiveData()
    }

    var selectParticipate: MutableList<TradePreviewParticipate>? = null

    val shopConfig: MutableLiveData<ShopConfigEntity?> by lazy {
        MutableLiveData()
    }

    // 强制海星
    val canForceUseStarfish: MediatorLiveData<Boolean> = MediatorLiveData(false).apply {
        addSource(tradePreview) {
            value = checkForceUseStarfish()
        }
        addSource(shopConfig) {
            value = checkForceUseStarfish()
        }
    }

    private fun checkForceUseStarfish(): Boolean =
        if (1 == shopConfig.value?.configType) {
            if (1 == shopConfig.value?.tokenCoinForceUse) {
                true == tradePreview.value?.isZero()
            } else true
        } else true

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
            requestPreviewSync()

            // 资金
            ApiRepository.dealApiResult(
                mCapitalRepo.requestBalance(
                    ApiRepository.createRequestBody("")
                )
            )?.let {
                balance.postValue(it)
            }

            querySelfCleanState()
        })
    }

    private suspend fun requestPreviewSync() {
        ApiRepository.dealApiResult(
            mOrderRepo.requestUnderWayOrderPreviewV2(
                ApiRepository.createRequestBody(getCommonParams(true))
            )
        )?.let {
            tradePreview.postValue(it)

            // 强制使用海星
            val ret = ApiRepository.dealApiResult(
                mShopRepo.requestShopConfigList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "shopId" to it.itemList.firstOrNull()?.shopId,
                            "goodsId" to it.itemList.firstOrNull()?.goodsId,
                            "goodsCategoryId" to it.itemList.firstOrNull()?.goodsCategoryId
                        )
                    )
                )
            )
            shopConfig.postValue(ret?.find { item -> 1 == item.configType })
        }
    }

    //支付方式 1001-余额 103--支付宝app支付 203--微信app支付
    var payMethod: Int = -1

    val prepayParam: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    var isPayFinish: Int = -1

    fun requestPrePay(context: Context) {
        launch({
            // 暂时只有预约，如果后期普通订单也加入就放开
            if ("300" != orderDetails.value?.orderType) {
                orderDetails.value?.orderItemList?.firstOrNull()?.let { item ->
                    if (!verifyGoods(context, item.goodsId, item.categoryCode)) {
                        return@launch
                    }
                }
            }

            ApiRepository.dealApiResult(
                mOrderRepo.createUnderWayOrder(
                    ApiRepository.createRequestBody(
                        getCommonParams(false)
                    )
                )
            )?.let {
                orderNo = it.orderNo
                ApiRepository.dealApiResult(
                    mOrderRepo.prePay(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "orderNo" to orderNo,
                                "payMethod" to payMethod
                            )
                        )
                    )
                )?.let { prePay ->
                    isPayFinish = 1
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

    private suspend fun verifyGoods(
        context: Context,
        goodId: Int,
        categoryCode: String
    ): Boolean {
        val result = ApiRepository.dealApiResult(
            mDeviceRepo.verifyGoods(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "goodsId" to goodId,
                        "categoryCode" to categoryCode,
                    )
                )
            )
        )

        return if (false == result?.isSuccess) {
            withContext(Dispatchers.Main) {
                SToast.showToast(context, result.msg)
            }
            false
        } else true
    }


    fun requestAsyncPayAsync() {
        if (orderNo.isNullOrEmpty() || -1 == payMethod) {
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

    /**
     * 支付宝支付
     */
    fun alipay(activity: Activity, prepayParam: String) {
        launch({
            isPayFinish = 2
            val resultStatus: String? = AlipayHelper.requestPay(activity, prepayParam).resultStatus
            isPayFinish = 3

            //用户取消不去请求接口查询支付状态
            if (TextUtils.equals(resultStatus, "6001")) {
                jump.postValue(1)
                return@launch
            }

            if (orderNo.isNullOrEmpty() || -1 == payMethod) {
                return@launch
            }

            requestAsyncPay()
        })
    }

    /** ------------------------------预约支付------------------------------**/
}
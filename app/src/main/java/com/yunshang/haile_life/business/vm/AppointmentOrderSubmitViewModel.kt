package com.yunshang.haile_life.business.vm

import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.extend.isGreaterThan0
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.string.StringUtils
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
class AppointmentOrderSubmitViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)
//
//    val goods: MutableLiveData<MutableList<IntentParams.OrderSubmitParams.OrderSubmitGood>> =
//        MutableLiveData(mutableListOf())
//
//    val reserveTime: MutableLiveData<String> by lazy {
//        MutableLiveData()
//    }
//
//    val deviceName: MutableLiveData<String> by lazy {
//        MutableLiveData()
//    }
//
//    val shopAddress: MutableLiveData<String> by lazy {
//        MutableLiveData()
//    }
//
//    val shopDeviceVal: MediatorLiveData<SpannableString> =
//        MediatorLiveData(SpannableString("")).apply {
//            addSource(deviceName) {
//                value = formatShopDeviceVal()
//            }
//            addSource(shopAddress) {
//                value = formatShopDeviceVal()
//            }
//        }
//
//    private fun formatShopDeviceVal(): SpannableString {
//        val name = deviceName.value ?: ""
//        val address = shopAddress.value ?: ""
//
//        return StringUtils.formatMultiStyleStr(
//            "${name}\n$address",
//            arrayOf(
//                ForegroundColorSpan(
//                    ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_black_85),
//                ),
//                AbsoluteSizeSpan(DimensionUtils.sp2px(16f))
//            ),
//            0, name.length
//        )
//    }
//
//    val reserveTimeVal: LiveData<SpannableString> = reserveTime.map {
//        it?.let {
//            val useDate = DateTimeUtils.formatDateFromString(it)
//            val time = if (DateTimeUtils.isSameDay(useDate, Date())) {
//                "今天${DateTimeUtils.formatDateTime(useDate, "HH:mm")}"
//            } else it
//            val content = com.lsy.framelib.utils.StringUtils.getString(
//                R.string.order_submit_used_time_prompt,
//                time
//            )
//            StringUtils.formatMultiStyleStr(
//                content, arrayOf(
//                    ForegroundColorSpan(
//                        ContextCompat.getColor(
//                            Constants.APP_CONTEXT,
//                            if (DeviceCategory.isDryerOrHair(goods.value?.firstOrNull()?.categoryCode)) R.color.color_ff630e else R.color.secondColorPrimary
//                        )
//                    ),
//                    StyleSpan(Typeface.BOLD)
//                ), 3, 3 + time.length
//            )
//        } ?: SpannableString("")
//    }
//
//    val isDryer: LiveData<Boolean> = goods.map {
//        if (it.isNotEmpty()) {
//            DeviceCategory.isDryerOrHair(it.first().categoryCode)
//        } else false
//    }
//
//    //支付方式 1001-余额 103--支付宝app支付 203--微信app支付
//    var payMethod: Int = -1
//
//    val prepayParam: MutableLiveData<String> by lazy {
//        MutableLiveData()
//    }
//
//    var isPaying = false
//
//    fun requestPrePay(context: Context) {
//        if (goods.value.isNullOrEmpty()) return
//
//        launch({
//            goods.value!!.forEach {
//                if (!verifyGoods(context, it)) {
//                    return@launch
//                }
//            }
//
//            ApiRepository.dealApiResult(
//                mOrderRepo.createTrade(
//                    ApiRepository.createRequestBody(
//                        getCommonParams(false)
//                    )
//                )
//            )?.let {
//                orderNo = it.orderNo
//                LiveDataBus.post(BusEvents.ORDER_SUBMIT_STATUS, true)
//                ApiRepository.dealApiResult(
//                    mOrderRepo.prePay(
//                        ApiRepository.createRequestBody(
//                            hashMapOf(
//                                "orderNo" to it.orderNo,
//                                "payMethod" to payMethod
//                            )
//                        )
//                    )
//                )?.let { prePay ->
//                    if (1001 == payMethod) {
//                        ApiRepository.dealApiResult(
//                            mOrderRepo.balancePay(
//                                ApiRepository.createRequestBody(
//                                    hashMapOf(
//                                        "prepayParam" to prePay.prepayParam
//                                    )
//                                )
//                            )
//                        )?.let {
//                            requestAsyncPay()
//                        }
//                    } else {
//                        prepayParam.postValue(prePay.prepayParam)
//                    }
//                }
//            }
//        })
//    }
//
//    var isPayFinish = false
//
//    /**
//     * 支付宝支付
//     */
//    fun alipay(activity: Activity, prepayParam: String) {
//        launch({
//            isPayFinish = false
//            val resultStatus: String? = AlipayHelper.requestPay(activity, prepayParam).resultStatus
//            isPayFinish = true
//
//            //用户取消不去请求接口查询支付状态
//            if (TextUtils.equals(resultStatus, "6001")) {
//                jump.postValue(1)
//                return@launch
//            }
//
//            if (orderNo.isEmpty() || -1 == payMethod) {
//                return@launch
//            }
//
//            requestAsyncPay()
//        })
//    }
//
//    fun requestAsyncPayAsync() {
//        if (orderNo.isEmpty() || -1 == payMethod) {
//            return
//        }
//        launch({
//            requestAsyncPay()
//        })
//    }
//
//    private suspend fun requestAsyncPay() {
//        ApiRepository.dealApiResult(
//            mOrderRepo.asyncPay(
//                ApiRepository.createRequestBody(
//                    hashMapOf(
//                        "orderNo" to orderNo,
//                        "payMethod" to payMethod
//                    )
//                )
//            )
//        )?.let {
//            LiveDataBus.post(BusEvents.PAY_SUCCESS_STATUS, it.success)
//        }
//    }
//
//    private suspend fun verifyGoods(
//        context: Context,
//        orderSubmitGood: IntentParams.OrderSubmitParams.OrderSubmitGood
//    ): Boolean {
//        val result = ApiRepository.dealApiResult(
//            mDeviceRepo.verifyGoods(
//                ApiRepository.createRequestBody(
//                    hashMapOf(
//                        "goodsId" to orderSubmitGood.goodId,
//                        "categoryCode" to orderSubmitGood.categoryCode,
//                    )
//                )
//            )
//        )
//
//        return if (false == result?.isSuccess) {
//            withContext(Dispatchers.Main) {
//                SToast.showToast(context, result.msg)
//            }
//            false
//        } else true
//    }

    val isDryer: MutableLiveData<Boolean> by lazy { MutableLiveData() }

    val inValidOrder: MutableLiveData<Boolean> = MutableLiveData(false)
    var validTime: Int? = null
    val countDownTime: MutableLiveData<SpannableString> by lazy { MutableLiveData() }

    // 计时器
    var timer: Timer? = null

    /**
     * 检测有效时间
     */
    fun checkValidTime() {
        if (!validTime.isGreaterThan0()) return
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (validTime.isGreaterThan0()) {
                    inValidOrder.postValue(false)
                    val time = "%02d:%02d".format(validTime!! / 60, validTime!! % 60)
                    val content =
                        com.lsy.framelib.utils.StringUtils.getString(
                            R.string.page_time_prefix
                        ) + " " + time
                    countDownTime.postValue(
                        StringUtils.formatMultiStyleStr(
                            content,
                            arrayOf(
                                ForegroundColorSpan(
                                    ContextCompat.getColor(
                                        Constants.APP_CONTEXT,
                                        R.color.color_ff630e,
                                    )
                                ),
                                AbsoluteSizeSpan(DimensionUtils.sp2px(26f)),
                                TypefaceSpan("money")
                            ), content.length - time.length, content.length
                        )
                    )
                    validTime = validTime!! - 1
                } else {
                    countDownTime.postValue(SpannableString("支付，已超时"))
                    inValidOrder.postValue(true)
                    jump.postValue(1)
                    timer?.cancel()
                }
            }
        }, 0, 1000)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        timer = null
    }
}
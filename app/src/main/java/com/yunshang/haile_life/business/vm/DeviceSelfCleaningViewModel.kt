package com.yunshang.haile_life.business.vm

import android.content.Context
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.data.extend.isGreaterThan0
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/21 17:29
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class DeviceSelfCleaningViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var orderNo: String? = null

    val orderDetails: MutableLiveData<OrderEntity> by lazy { MutableLiveData() }

    fun startOrderDevice(orderNo: String?, fulId: Int? = null, callback: () -> Unit) {
        if (orderNo.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.startAppointOrderDevice(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "orderNo" to orderNo,
                            "fulfillIdList" to fulId?.let { listOf(it) }
                        )
                    )
                )
            )
            withContext(Dispatchers.Main) {
                callback()
            }
        })
    }

    /**
     * 强启设备
     */
    fun coerceDevice(context: Context, orderNo: String?, fulId: Int? = null) {
        if (orderNo.isNullOrEmpty()) return

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.startByOrder(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "orderNo" to orderNo,
                            "fulfillIdList" to fulId?.let { listOf(it) }
                        )
                    )
                )
            )
            withContext(Dispatchers.Main) {
                SToast.showToast(context, "发送强启指令成功")
            }
        })
    }

    val inValidOrder: MutableLiveData<Boolean> = MutableLiveData(false)
    var validTime: Int? = null
    val countDownTime: MutableLiveData<SpannableString> by lazy {
        MutableLiveData()
    }

    // 计时器
    var timer: Timer? = null

    /**
     * 检测有效时间
     */
    fun checkValidTime() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                if (validTime.isGreaterThan0()) {
                    inValidOrder.postValue(false)
                    val time = "%02d:%02d".format(validTime!! / 60, validTime!! % 60)
                    val content =
                        com.lsy.framelib.utils.StringUtils.getString(
                            R.string.self_cleaning_prefix
                        ) + " " + time
                    countDownTime.postValue(
                        com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
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
                    countDownTime.postValue(SpannableString("启动，已超时"))
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
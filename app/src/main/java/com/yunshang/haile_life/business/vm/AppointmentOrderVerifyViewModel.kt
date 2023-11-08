package com.yunshang.haile_life.business.vm

import android.text.SpannableString
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.data.model.ApiRepository
import java.util.*


/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/3 14:52
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AppointmentOrderVerifyViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    val orderDetails: MutableLiveData<OrderEntity> by lazy { MutableLiveData() }

    var validTime: Long? = null

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
                countDownTime.postValue(SpannableString(StringUtils.getString(R.string.verify_time_prefix) + countDownTime.value))
            }
        }, 0, 1000)
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
        timer = null
    }
}
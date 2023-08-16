package com.yunshang.haile_life.business.vm

import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/16 10:10
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CardBindingViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)

    val cardSn: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val phone: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val code: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    // 是否可提交
    val canSubmit: MediatorLiveData<Boolean> = MediatorLiveData(false).apply {
        addSource(phone) {
            value = checkSubmit()
        }
        addSource(code) {
            value = checkSubmit()
        }
        addSource(cardSn) {
            value = checkSubmit()
        }
    }

    /**
     * 检测是否可提交
     */
    private fun checkSubmit(): Boolean =
        !phone.value.isNullOrEmpty() && !code.value.isNullOrEmpty() && !cardSn.value.isNullOrEmpty()


    // 验证码发送按钮内容
    private val defaultCodeTxt = com.lsy.framelib.utils.StringUtils.getString(R.string.send_again)
    var timer: CountDownTimer? = null

    fun sendCode(v: View) {
        if (cardSn.value.isNullOrEmpty()) {
            SToast.showToast(v.context, "请先输入卡号")
            return
        }
        if (phone.value.isNullOrEmpty()) {
            SToast.showToast(v.context, "请先输入手机号")
            return
        }
        timer?.cancel()
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.sendBindCode(
                    ApiRepository.createRequestBody(
                        mutableMapOf(
                            "phone" to phone.value!!,
                            "cardSn" to cardSn.value!!,
                        )
                    )
                )
            )
            viewModelScope.launch(Dispatchers.Main) {
                SToast.showToast(v.context, "验证码已发送")
                countDownTimer(v as TextView)
            }
        })
    }

    /**
     * 验证码倒计时
     */
    private fun countDownTimer(btn: TextView) {
        btn.isEnabled = false
        var num = 60
        timer?.cancel()
        timer = object : CountDownTimer((num + 1) * 1000L, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                btn.text = "${num--}s"
            }

            override fun onFinish() {
                btn.text = defaultCodeTxt
                btn.isEnabled = true
                btn.setTextColor(ContextCompat.getColor(btn.context, R.color.colorPrimary))
            }
        }
        timer?.start()
    }

    fun bindingCard(v: View) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.bindingCard(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "cardSn" to cardSn.value,
                            "phone" to phone.value,
                            "code" to code.value,
                        )
                    )
                )
            )

            LiveDataBus.post(BusEvents.CARD_BINDING_STATUS, true)
            jump.postValue(0)
        })
    }
}
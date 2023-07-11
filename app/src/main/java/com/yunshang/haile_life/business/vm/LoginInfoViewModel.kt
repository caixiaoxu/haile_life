package com.yunshang.haile_life.business.vm

import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.LoginUserService
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/8 15:46
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
open class LoginInfoViewModel : BaseViewModel() {
    protected val mLoginRepo = ApiRepository.apiClient(LoginUserService::class.java)

    // 手机号
    val phone: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    // 验证码
    val code: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    // 可验证码
    val canSendCode: LiveData<Boolean> = phone.map {
        !it.isNullOrEmpty()
    }

    private var timer: CountDownTimer? = null

    // 验证码发送按钮内容
    private val defaultCodeTxt = com.lsy.framelib.utils.StringUtils.getString(R.string.send_again)

    // 同意协议
    val isAgree: MutableLiveData<Boolean> = MutableLiveData(false)

    // 是否可提交
    val canSubmit: MediatorLiveData<Boolean> = MediatorLiveData(false).apply {
        addSource(phone) {
            value = checkSubmit()
        }
        addSource(code) {
            value = checkSubmit()
        }
        addSource(isAgree) {
            value = checkSubmit()
        }
    }

    /**
     * 检测是否可提交
     */
    private fun checkSubmit(): Boolean =
        !phone.value.isNullOrEmpty() && !code.value.isNullOrEmpty() && isAgree.value!!

    /**
     * 发送验证码
     */
    fun sendCode(view: View) {
        if (phone.value.isNullOrEmpty()) {
            SToast.showToast(view.context, "请先输入手机号")
            return
        }

        launch({
            ApiRepository.dealApiResult(
                mLoginRepo.sendCode(
                    ApiRepository.createRequestBody(
                        mutableMapOf(
                            "target" to phone.value!!,
                            "sendType" to 1,//1:短信；2:邮件
                            "method" to 1 //1:登录；2:修改密码
                        )
                    )
                )
            )
            viewModelScope.launch(Dispatchers.Main) {
                countDownTimer(view as Button)
            }
        })
    }

    /**
     * 验证码倒计时
     */
    private fun countDownTimer(btn: Button) {
        btn.isEnabled = false
        var num = 60
        if (null == timer) {
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
        }
        timer?.start()
    }
}
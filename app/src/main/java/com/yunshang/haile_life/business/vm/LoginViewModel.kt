package com.yunshang.haile_life.business.vm

import android.app.Activity
import android.view.View
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.LoginUserService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.thrid.AlipayHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title : 密码登录的viewmodel
 * Author: Lsy
 * Date: 2023/4/4 13:57
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class LoginViewModel : LoginInfoViewModel() {
    private val mRepo = ApiRepository.apiClient(LoginUserService::class.java)

    /**
     * 登录
     */
    fun login(view: View, sharedView: SharedViewModel) {
        if (phone.value.isNullOrEmpty()) {
            SToast.showToast(view.context, R.string.phone_empty)
            return
        }

        if (code.value.isNullOrEmpty()) {
            SToast.showToast(view.context, R.string.code_empty)
            return
        }

        if (true != isAgree.value) {
            SToast.showToast(view.context, R.string.agreement_err)
            return
        }

        launch(
            {
                sharedView.login(
                    phone.value,
                    code.value,
                    2
                )
                LiveDataBus.post(BusEvents.LOGIN_STATUS, true)
            })
    }

    /**
     * 支付宝登录
     */
    fun aliPayAuth(activity: Activity, thirdLogin: suspend (code: String) -> Unit) {
        launch({
            ApiRepository.dealApiResult(mRepo.requestAlipayAuthParams())?.let {
                val result = AlipayHelper.requestAuth(activity, it)
                if (result.resultStatus == "9000" && result.resultCode == "200" && !result.authCode.isNullOrEmpty()) {
                    thirdLogin(result.authCode!!)
                } else {
                    // 其他状态值则为授权失败
                    withContext(Dispatchers.Main) {
                        SToast.showToast(activity, "授权失败")
                    }
                }
            }
        })
    }
}
package com.yunshang.haile_life.business.vm

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.yunshang.haile_life.business.apiService.LoginUserService
import com.yunshang.haile_life.data.entities.BindPhoneEntity
import com.yunshang.haile_life.data.model.ApiRepository

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
class BindPhoneViewModel : LoginInfoViewModel() {

    var authCode: String? = null

    var loginType: Int = -1

    val bindPhoneSuccess: MutableLiveData<BindPhoneEntity> by lazy {
        MutableLiveData()
    }

    /**
     * 绑定手机号
     */
    fun bindPhone(view: View) {
        if (authCode.isNullOrBlank() || -1 == loginType) return
        launch({
            requestBindPhone()?.let {
                if (!it.accountBindFlag || it.code.isNotEmpty()) {
                    jump.postValue(1)
                } else {
                    bindPhoneSuccess.postValue(it)
                }
            }
        })
    }

    fun requestBindPhoneAsync(){
        launch({
            requestBindPhone(true)?.let { e ->
                bindPhoneSuccess.postValue(e)
            }
        })
    }

    private suspend fun requestBindPhone(confirmBind: Boolean = false): BindPhoneEntity? =
        ApiRepository.dealApiResult(
            mLoginRepo.bindAccountForPhone(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "phone" to phone.value,
                        "verificationCode" to code.value,
                        "code" to authCode,
                        "loginType" to loginType,
                        "authorizationClientType" to 9,
                        "confirmBind" to confirmBind
                    )
                )
            )
        )
}
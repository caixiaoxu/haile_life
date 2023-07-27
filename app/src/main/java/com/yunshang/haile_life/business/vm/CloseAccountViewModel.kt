package com.yunshang.haile_life.business.vm

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.LoginUserService
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.model.SPRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/13 20:15
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CloseAccountViewModel : BaseViewModel() {
    private val mLoginRepo = ApiRepository.apiClient(LoginUserService::class.java)

    fun closeAccount() {
        launch({
            ApiRepository.dealApiResult(
                mLoginRepo.closeAccount(ApiRepository.createRequestBody(""))
            )?.let {
                SPRepository.cleaLoginUserInfo()
                jump.postValue(1)
            }
        })
    }

    val btnVal: MutableLiveData<Int> by lazy {
        MutableLiveData()
    }

    val timer: CountDownTimer by lazy {
        var num = 3
        object : CountDownTimer((num + 1) * 1000L, 1000L) {

            override fun onTick(millisUntilFinished: Long) {
                btnVal.value = --num
            }

            override fun onFinish() {
                btnVal.value = 0
            }
        }
    }
}
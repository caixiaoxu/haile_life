package com.yunshang.haile_life.business.vm

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.LoginUserService
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/13 15:08
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class UpdateNickNameViewModel : BaseViewModel() {
    private val mUserService = ApiRepository.apiClient(LoginUserService::class.java)

    val nickName: MutableLiveData<String> by lazy {
        MutableLiveData()
    }


    fun updatePersonalName(view: View, callback: () -> Unit) {
        if (nickName.value.isNullOrEmpty()) {
            SToast.showToast(view.context, "请输入昵称")
            return
        }

        launch({
            ApiRepository.dealApiResult(
                mUserService.updateUserInfo(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "nickName" to nickName.value,
                        )
                    )
                )
            )
            withContext(Dispatchers.Main) {
                SToast.showToast(view.context, R.string.update_success)
                callback()
            }
        })
    }
}
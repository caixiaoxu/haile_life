package com.yunshang.haile_life.business.vm

import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.CommonService
import com.yunshang.haile_life.business.apiService.LoginUserService
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/13 14:53
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class PersonalInfoViewModel: BaseViewModel() {
    private val mCommonRepo = ApiRepository.apiClient(CommonService::class.java)
    private val mUserRepo = ApiRepository.apiClient(LoginUserService::class.java)

    /**
     * 上传头像
     */
    fun uploadHeadIcon(path: String, callback: (isSuccess: Boolean) -> Unit) {
        launch({
            ApiRepository.dealApiResult(
                mCommonRepo.updateLoadFile(
                    ApiRepository.createFileUploadBody(path)
                )
            )?.let {
                Timber.i("图片上传成功：$it")
                ApiRepository.dealApiResult(
                    mUserRepo.updateUserInfo(
                        ApiRepository.createRequestBody(hashMapOf("headImage" to it))
                    )
                )
                withContext(Dispatchers.Main) {
                    callback(true)
                }
            }
        }, {
            Timber.e("图片上传失败$it")
            withContext(Dispatchers.Main) {
                callback(false)
            }
        })
    }
}
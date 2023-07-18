package com.yunshang.haile_life.business.vm

import android.content.Context
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.AppPackageUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.business.apiService.CommonService
import com.yunshang.haile_life.business.apiService.LoginUserService
import com.yunshang.haile_life.data.entities.AppVersionEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.model.OnDownloadProgressListener
import com.yunshang.haile_life.data.model.SPRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/12 17:42
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class SettingViewModel : BaseViewModel() {
    private val mCommonRepo = ApiRepository.apiClient(CommonService::class.java)
    private val mLoginRepo = ApiRepository.apiClient(LoginUserService::class.java)

    val appVersionInfo: MutableLiveData<AppVersionEntity> by lazy {
        MutableLiveData()
    }

    fun checkVersion(context: Context) {
        launch({
            ApiRepository.dealApiResult(
                mCommonRepo.appVersion(
                    AppPackageUtils.getVersionName(
                        context
                    )
                )
            )
                ?.let {
                    appVersionInfo.postValue(it)
                }
        })
//
//        val json = "{\"id\":40,\"name\":\"海乐管家\",\"versionCode\":\"1.2.7\",\"versionName\":\"1.2.7\",\"versionMin\":\"1.2.7\",\"updateUrl\":\"https://static.haier-ioc.com/soft/mms/mms_v1.2.7.apk\",\"updateLog\":\"1、新增公告功能\\n2、新增收益统计\",\"createTime\":\"2023-07-05 10:10:26\",\"needUpdate\":true,\"forceUpdate\":false}"
//        appVersionInfo.value = GsonUtils.json2Class(json,AppVersionEntity::class.java)
    }

    fun logout(v: View) {
        launch({
            mLoginRepo.logout(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "authorizationClientType" to 4
                    )
                )
            )
        }, complete = {
            SPRepository.cleaLoginUserInfo()
            jump.postValue(1)
        })
    }

    fun downLoadApk(
        appVersion: AppVersionEntity,
        downloadListener: OnDownloadProgressListener,
    ) {
        if (appVersion.updateUrl.isEmpty()) {
            return
        }

        launch({
            ApiRepository.downloadFile(
                appVersion.updateUrl, "${appVersion.name}${appVersion.versionName}.apk",
                downloadListener
            )
        }, {
            withContext(Dispatchers.Main) {
                downloadListener.onFailure(it)
            }
        }, showLoading = false)
    }
}
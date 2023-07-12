package com.yunshang.haile_life.business.vm

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.AppPackageUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.CommonService
import com.yunshang.haile_life.data.entities.AppVersionEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.model.OnDownloadProgressListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title : main的viewmodel
 * Author: Lsy
 * Date: 2023/4/4 13:57
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class MainViewModel : BaseViewModel() {
    private val mCommonRepo = ApiRepository.apiClient(CommonService::class.java)

    //选择的id
    val checkId: MutableLiveData<Int> = MutableLiveData(R.id.rb_main_tab_home)

    fun checkVersion(context: Context, callBack: (appVersion: AppVersionEntity) -> Unit) {
        launch({
            ApiRepository.dealApiResult(
                mCommonRepo.appVersion(AppPackageUtils.getVersionName(context))
            )?.let {
                withContext(Dispatchers.Main) {
                    callBack(it)
                }
            }
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
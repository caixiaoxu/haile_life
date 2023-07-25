package com.yunshang.haile_life.business.vm

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.AppPackageUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.AppointmentService
import com.yunshang.haile_life.business.apiService.CommonService
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.MarketingService
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.model.OnDownloadProgressListener
import com.yunshang.haile_life.utils.string.StringUtils
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
    private val mMarketingRepo = ApiRepository.apiClient(MarketingService::class.java)
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mAppointmentRepo = ApiRepository.apiClient(AppointmentService::class.java)

    //选择的id
    val checkId: MutableLiveData<Int> = MutableLiveData(R.id.rb_main_tab_home)

    val storeAdEntity: MutableLiveData<ADEntity> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        launch({
            ApiRepository.dealApiResult(
                mMarketingRepo.requestAD(
                    ApiRepository.createRequestBody(
                        hashMapOf(
//                            "slotKey" to "mini_store"
                            "slotKey" to "home_tab_shop_android"
                        )
                    )
                )
            )?.let {
                storeAdEntity.postValue(it)
            }
        })
    }

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

    fun requestScanResult(
        code: String,
        callBack: (scanEntity: GoodsScanEntity, detailEntity: DeviceDetailEntity?, appointEntity: GoodsAppointmentEntity?) -> Unit
    ) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestGoodsScan(
                    imei = if (StringUtils.isImeiCode(code)) code else null,
                    n = if (StringUtils.isImeiCode(code)) null else code
                )
            )?.let { scan ->
                // 设备详情
                val deviceDetail =
                    ApiRepository.dealApiResult(mDeviceRepo.requestDeviceDetail(scan.goodsId))

                // 如果有预约跳转预约订单详情列表
                val appointEntity = ApiRepository.dealApiResult(
                    mAppointmentRepo.requestIsAppointmentOfGoods(
                        ApiRepository.createRequestBody(
                            hashMapOf("goodsId" to scan.goodsId)
                        )
                    )
                )

                withContext(Dispatchers.Main) {
                    callBack(scan, deviceDetail, appointEntity)
                }
            }
        })
    }
}
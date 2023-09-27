package com.yunshang.haile_life.business.vm

import android.content.Context
import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.AppPackageUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.AppointmentService
import com.yunshang.haile_life.business.apiService.CommonService
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.MarketingService
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.model.OnDownloadProgressListener
import com.yunshang.haile_life.ui.fragment.HomeFragment
import com.yunshang.haile_life.ui.fragment.MineFragment
import com.yunshang.haile_life.ui.fragment.OrderFragment
import com.yunshang.haile_life.ui.fragment.StoreFragment
import com.yunshang.haile_life.ui.view.dialog.Hint3SecondDialog
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

    // 当前的fragment
    var curFragmentTag: String? = null

    val fragments = SparseArray<Fragment>(5).apply {
        put(R.id.rb_main_tab_home, HomeFragment())
        put(R.id.rb_main_tab_store, StoreFragment())
        put(R.id.rb_main_tab_scan, HomeFragment())
        put(R.id.rb_main_tab_order, OrderFragment().apply {
            arguments = IntentParams.OrderListParams.pack(true)
        })
        put(R.id.rb_main_tab_mine, MineFragment())
    }

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
        supportFragmentManager: FragmentManager,
        callBack: (scanEntity: GoodsScanEntity, detailEntity: DeviceDetailEntity, appointEntity: GoodsAppointmentEntity?) -> Unit
    ) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestGoodsScan(
                    imei = if (StringUtils.isImeiCode(code)) code else null,
                    n = if (StringUtils.isImeiCode(code)) null else code
                )
            )?.let { scan ->
                // goodsId为空，提示设备末绑定
                if (null == scan.goodsId || 0 == scan.goodsId) {
                    withContext(Dispatchers.Main) {
                        Hint3SecondDialog.Builder("设备未激活，请换一台设备使用或联系商家")
                            .build().show(supportFragmentManager)
                    }
                    return@let
                }

                // 设备详情
                ApiRepository.dealApiResult(mDeviceRepo.requestDeviceDetail(scan.goodsId))
                    ?.let { deviceDetail ->
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
            }
        },{
            withContext(Dispatchers.Main) {
                it.message?.let { msg -> Hint3SecondDialog.Builder(msg).build().show(supportFragmentManager) }
            }
        })
    }
}
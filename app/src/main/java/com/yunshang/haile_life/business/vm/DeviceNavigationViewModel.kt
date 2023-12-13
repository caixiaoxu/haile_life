package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.AppointmentService
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.MarketingService
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.ADEntity
import com.yunshang.haile_life.data.entities.DeviceDetailEntity
import com.yunshang.haile_life.data.entities.GoodsAppointmentEntity
import com.yunshang.haile_life.data.entities.GoodsScanEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.string.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/9 15:10
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class DeviceNavigationViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mAppointmentRepo = ApiRepository.apiClient(AppointmentService::class.java)
    private val mMarketingRepo = ApiRepository.apiClient(MarketingService::class.java)

    var categoryCode: String? = null

    val isShower: Boolean
        get() = DeviceCategory.isShower(categoryCode)

    val adEntity: MutableLiveData<ADEntity> by lazy {
        MutableLiveData()
    }

    /**
     * 广告位
     */
    fun requestData() {
        launch({
            ApiRepository.dealApiResult(
                mMarketingRepo.requestAD(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "slotKey" to if (isShower) "shower_banner_top_android"  else "drinking_banner_top_android"
                        )
                    )
                )
            )?.let {
                adEntity.postValue(it)
            }
        })
    }

    fun requestScanResult(
        code: String,
        callBack: (scanEntity: GoodsScanEntity, detailEntity: DeviceDetailEntity, appointEntity: GoodsAppointmentEntity?) -> Unit
    ) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestGoodsScan(
                    imei = if (StringUtils.isImeiCode(code)) code else null,
                    n = if (StringUtils.isImeiCode(code)) null else code
                )
            )?.let { scan ->
                // 设备详情
                ApiRepository.dealApiResult(mDeviceRepo.requestDeviceDetail(scan.goodsId!!))
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
        })
    }
}
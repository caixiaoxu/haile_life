package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.AppointDevice
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/10 14:05
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class SelectAppointmentDeviceViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)
    var shopId: Int = -1
    var specValueId: Int = -1
    var unit: Int? = null

    val appointDeviceList: MutableLiveData<MutableList<AppointDevice>> by lazy {
        MutableLiveData()
    }

    fun requestAppointDevice() {
        if (-1 == shopId || -1 == specValueId) return

        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestAppointDeviceList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "shopId" to shopId,
                            "specValueId" to specValueId,
                            "unit" to unit,
                        )
                    )
                )
            )?.let {
                appointDeviceList.postValue(it.itemList)
            }
        })

    }
}
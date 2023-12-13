package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.data.entities.FaultRepairsRecordEntity
import com.yunshang.haile_life.data.extend.hasVal
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/21 10:55
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class FaultRepairsDetailViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)

    var repairsId: Int? = null

    val recordDetail:MutableLiveData<FaultRepairsRecordEntity> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        if (!repairsId.hasVal()) return
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestFaultRepairsRecordDetails(repairsId!!)
            )?.let {
                recordDetail.postValue(it)
            }
        })
    }
}
package com.yunshang.haile_life.business.vm

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.data.entities.WaterControlCodeEntity
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/10/17 15:41
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class WaterControlCodeViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)

    val waterControlCode: MutableLiveData<WaterControlCodeEntity> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestWaterCode()
            )?.let {
                waterControlCode.postValue(it)
            }
        })
    }

    fun generateWaterControlCode(v: View) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.generateWaterCode(
                    ApiRepository.createRequestBody("")
                )
            )?.let {
                waterControlCode.postValue(it)
            }
        })
    }

    fun updateWaterControlCodeState(state: Int, callback: () -> Unit) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.updateWaterCode(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "state" to state
                        )
                    )
                )
            )
            waterControlCode.value?.hasState = 1 == state
            withContext(Dispatchers.Main){
                callback()
            }
        })
    }
}
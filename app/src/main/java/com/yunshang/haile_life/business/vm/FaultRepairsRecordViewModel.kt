package com.yunshang.haile_life.business.vm

import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.data.entities.FaultRepairsRecordEntity
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/21 10:54
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class FaultRepairsRecordViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)

    fun requestData(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out FaultRepairsRecordEntity>?) -> Unit
    ) {
        launch({
            val result = ApiRepository.dealApiResult(
                mDeviceRepo.requestFaultRepairsRecordList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to page,
                            "pageSize" to pageSize,
                        )
                    )
                )
            )
            withContext(Dispatchers.Main) {
                callBack(result)
            }
        }, {
            withContext(Dispatchers.Main) {
                callBack(null)
            }
        })
    }
}
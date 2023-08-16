package com.yunshang.haile_life.business.vm

import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.data.entities.CardEntity
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/16 10:10
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CardManagerViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)

    fun requestCardList(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out CardEntity>?) -> Unit
    ) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestCardList(
                    ApiRepository.createRequestBody(
                        hashMapOf()
                    )
                )
            )?.let {
                withContext(Dispatchers.Main) {
                    callBack(it)
                }
            }
        })
    }

    fun unBindCard(cardSn: String,callBack:()->Unit) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.unbindCard(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "cardSn" to cardSn
                        )
                    )
                )
            )
            withContext(Dispatchers.Main){
                callBack()
            }
        })
    }
}
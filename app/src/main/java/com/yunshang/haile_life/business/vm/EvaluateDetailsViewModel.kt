package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.entities.EvaluateDetailsEntity
import com.yunshang.haile_life.data.extend.hasVal
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/12/28 17:53
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class EvaluateDetailsViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var orderId: Int? = null

    val evaluateDetails: MutableLiveData<EvaluateDetailsEntity> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        if (!orderId.hasVal()) return
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestEvaluateDetails(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "orderId" to orderId
                        )
                    )
                )
            )?.let {
                evaluateDetails.postValue(it)
            }
        })
    }
}
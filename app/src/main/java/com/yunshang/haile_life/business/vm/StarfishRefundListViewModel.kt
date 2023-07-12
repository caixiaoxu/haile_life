package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.StarfishRefundApplyEntity
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/12 11:33
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class StarfishRefundListViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    val refundApplyList: MutableLiveData<MutableList<StarfishRefundApplyEntity>> by lazy {
        MutableLiveData()
    }

    var refundId: String? = null

    fun requestStarfishRefundApplyList() {
        if (refundId.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestStarfishRefundApplyList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "operatorId" to refundId
                        )
                    )
                )
            )?.let {
                refundApplyList.postValue(it)
            }
        })
    }
}
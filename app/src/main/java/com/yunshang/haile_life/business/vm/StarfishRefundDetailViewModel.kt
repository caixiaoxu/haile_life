package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.StarfishRefundDetailEntity
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/12 15:48
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class StarfishRefundDetailViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    var refundId: Int = -1

    val refundDetail: MutableLiveData<StarfishRefundDetailEntity> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        if (-1 == refundId) return
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestRefundDetail(refundId)
            )?.let {
                refundDetail.postValue(it)
            }
        })
    }
}
package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.RechargeShopListEntity
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/10 16:03
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class RechargeStarfishShopListViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    val rechargeShopList: MutableLiveData<RechargeShopListEntity> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestRechargeShopList(ApiRepository.createRequestBody(""))
            )?.let {
                rechargeShopList.postValue(it)
            }
        })
    }
}
package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.ShopDetailEntity
import com.yunshang.haile_life.data.entities.ShopNoticeEntity
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/6 17:30
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class ShopDetailViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    var shopId: Int = -1

    val shopDetail: MutableLiveData<ShopDetailEntity> by lazy {
        MutableLiveData()
    }

    val shopNotice: MutableLiveData<MutableList<ShopNoticeEntity>> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        if (shopId == -1) return

        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestShopDetail(shopId)
            )?.let {
                shopDetail.postValue(it)
            }
            ApiRepository.dealApiResult(
                mShopRepo.requestShopNotice(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "shopId" to shopId
                        )
                    )
                )
            )?.let {
                shopNotice.postValue(it)
            }
        })
    }
}
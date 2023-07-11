package com.yunshang.haile_life.business.vm

import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.RechargeStarfishDetailEntity
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/10 17:14
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class RechargeStarfishDetailListViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)
    var shopId: Int = -1

    fun requestRechargeDetailList(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out RechargeStarfishDetailEntity>?) -> Unit
    ) {
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestRechargeDetailList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to page,
                            "pageSize" to pageSize,
                            "shopId" to shopId,
                        )
                    )
                )
            )?.let {
                withContext(Dispatchers.Main) {
                    callBack(it)
                }
            }
        }, showLoading = 1 == page)
    }
}
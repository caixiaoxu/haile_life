package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.MarketingService
import com.yunshang.haile_life.data.entities.DiscountCouponEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.rule.IndicatorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/13 09:47
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class DiscountCouponViewModel : BaseViewModel() {
    private val mMarketingRepo = ApiRepository.apiClient(MarketingService::class.java)

    var categoryCode: String? = null

    var curCouponStatus: MutableLiveData<Int> = MutableLiveData(1)

    val mCouponIndicators: MutableLiveData<List<IndicatorEntity<Int>>> = MutableLiveData(
        arrayListOf(
            IndicatorEntity("未使用", 0, 1),
            IndicatorEntity("已使用", 0, 30),
            IndicatorEntity("已失效", 0, 31),
        )
    )

    fun requestData(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out DiscountCouponEntity>?) -> Unit
    ) {
        launch({
            ApiRepository.dealApiResult(
                mMarketingRepo.requestDiscountCouponList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to page,
                            "pageSize" to pageSize,
                            "state" to curCouponStatus.value,
                            "categoryCode" to categoryCode
                        )
                    )
                )
            )?.let {
                withContext(Dispatchers.Main) {
                    callBack(it)
                }
            }
//            requestCouponNum()
        })
    }

//    suspend fun requestCouponNum() {
//        ApiRepository.dealApiResult(
//            mMarketingRepo.requestDiscountCouponNum()
//        )?.let {
//        }
//    }
}
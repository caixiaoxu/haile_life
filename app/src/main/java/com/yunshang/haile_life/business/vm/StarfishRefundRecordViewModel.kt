package com.yunshang.haile_life.business.vm

import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.StarfishRefundRecordEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/12 10:37
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class StarfishRefundRecordViewModel: BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    var searchDate: Date = Date()

    fun requestRechargeRefundList(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out StarfishRefundRecordEntity>?) -> Unit
    ) {
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestStarfishRefundList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to page,
                            "pageSize" to pageSize,
                            "startTime" to DateTimeUtils.formatDateTimeStartParam(
                                DateTimeUtils.getMonthFirst(
                                    searchDate
                                )
                            ),
                            "endTime" to DateTimeUtils.formatDateTimeEndParam(
                                DateTimeUtils.getMonthLast(
                                    searchDate
                                )
                            ),
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
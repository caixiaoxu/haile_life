package com.yunshang.haile_life.business.vm

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.NearStoreEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.rule.IndicatorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/6 11:48
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class NearByShopViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    var isRechargeShop: Boolean = false

    var location: Location? = null

    val curCategoryCode: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val mNearByShopIndicators = arrayListOf(
        IndicatorEntity("全部", 0, ""),
        IndicatorEntity("洗衣机", 0, "00"),
        IndicatorEntity("烘干机", 0, "02"),
        IndicatorEntity("洗鞋机", 0, "01"),
        IndicatorEntity("吹风机", 0, "03"),
    )

    /**
     * 附近门店
     */
    fun requestNearByStores(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out NearStoreEntity>?) -> Unit
    ) {
        if (null == curCategoryCode.value || null == location) return

        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestNearStores(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to page,
                            "pageSize" to pageSize,
                            "lng" to location!!.longitude,
                            "lat" to location!!.latitude,
                            "categoryCode" to curCategoryCode.value,
                            "rechargeFlag" to isRechargeShop
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
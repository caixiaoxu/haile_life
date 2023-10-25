package com.yunshang.haile_life.business.vm

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.ShopNoticeEntity
import com.yunshang.haile_life.data.entities.ShopPositionDetailEntity
import com.yunshang.haile_life.data.entities.ShopPositionDeviceEntity
import com.yunshang.haile_life.data.entities.StoreDeviceEntity
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
class ShopPositionDetailViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    var positionId: Int = -1

    val shopDetail: MutableLiveData<ShopPositionDetailEntity> by lazy {
        MutableLiveData()
    }

    val curDeviceCategory: MutableLiveData<StoreDeviceEntity> by lazy {
        MutableLiveData()
    }

    val shopNotice: MutableLiveData<MutableList<ShopNoticeEntity>> by lazy {
        MutableLiveData()
    }

    var location: Location? = null

    fun requestData() {
        if (positionId == -1) return

        launch({
            val params = hashMapOf<String, Any>(
                "id" to positionId,
            )
            location?.longitude?.let {
                params["lng"] = it
            }
            location?.latitude?.let {
                params["lat"] = it
            }
            ApiRepository.dealApiResult(
                mShopRepo.requestShopPositionDetail(params)
            )?.let {
                ApiRepository.dealApiResult(mShopRepo.requestPositionDeviceFloorList(positionId))
                    ?.let { floors ->
                        it.refreshFloorList(floors)
                        shopDetail.postValue(it)

                        it.positionDeviceDetailList?.firstOrNull()?.let { first ->
                            curDeviceCategory.postValue(first)
                        }
                    }
                ApiRepository.dealApiResult(
                    mShopRepo.requestShopNotice(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "shopId" to it.shopId,
                            )
                        )
                    )
                )?.let { notice ->
                    shopNotice.postValue(notice)
                }
            }
        })
    }

    fun requestDeviceList(
        refresh: Boolean,
        storeDevice: StoreDeviceEntity?,
        callback: (list: MutableList<ShopPositionDeviceEntity>) -> Unit
    ) {
        if (null == storeDevice) return
        launch({
            if (refresh) {
                storeDevice.page = 1
            }

            ApiRepository.dealApiResult(
                mShopRepo.requestPositionDeviceList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to storeDevice.page,
                            "pageSize" to 20,
                            "positionId" to positionId,
                            "categoryCode" to storeDevice.categoryCode,
                            "floorCode" to storeDevice.selectFloor?.value
                        )
                    )
                )
            )?.let {
                storeDevice.refreshDeviceList(refresh, it.items, it.total)
                withContext(Dispatchers.Main) {
                    callback(storeDevice.deviceList)
                }
            }
        })
    }
}
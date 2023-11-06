package com.yunshang.haile_life.business.vm

import android.location.Location
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.data.extend.hasVal
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
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)

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
        page: Int,
        pageSize: Int,
        callback: ((responseList: MutableList<out ShopPositionDeviceEntity>?) -> Unit)? = null
    ) {
        if (null == curDeviceCategory.value) return
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestPositionDeviceList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "page" to page,
                            "pageSize" to pageSize,
                            "positionId" to positionId,
                            "categoryCode" to curDeviceCategory.value?.categoryCode,
                            "floorCode" to curDeviceCategory.value?.selectFloor?.value
                        )
                    )
                )
            )?.let {
//                val list = mutableListOf<ShopPositionDeviceEntity>()
//                it.items?.let {
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                    list.addAll(it)
//                }
                curDeviceCategory.value?.refreshDeviceList(1 == page, it.items, it.total)
                withContext(Dispatchers.Main) {
                    callback?.invoke(it.items)
                }
            }
        })
    }

    fun requestAppointmentInfo(
        deviceId: Int?,
        callback: (deviceDetail: DeviceDetailEntity, stateList: List<DeviceStateEntity>?) -> Unit
    ) {
        if (!deviceId.hasVal()) return
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestDeviceDetail(deviceId!!)
            )?.also { deviceDetail ->
                ApiRepository.dealApiResult(
                    mDeviceRepo.requestDeviceStateList(
                        ApiRepository.createRequestBody(
                            hashMapOf("goodsId" to deviceId)
                        )
                    )
                )?.let { deviceStateList ->
                    requestDeviceStateList(deviceId)?.let {
                        callback(deviceDetail, deviceStateList.stateList)
                    }
                }
            }
        })
    }

    private suspend fun requestDeviceStateList(deviceId: Int): DeviceStateListEntity? {
        return ApiRepository.dealApiResult(
            mDeviceRepo.requestDeviceStateList(
                ApiRepository.createRequestBody(
                    hashMapOf("goodsId" to deviceId)
                )
            )
        )
    }

    fun requestDeviceStateListAsync(deviceId: Int?, callback: (stateList: List<DeviceStateEntity>?) -> Unit) {
        if (!deviceId.hasVal()) return
        launch({
            requestDeviceStateList(deviceId!!)?.let {
                callback(it.stateList)
            }
        })
    }
}
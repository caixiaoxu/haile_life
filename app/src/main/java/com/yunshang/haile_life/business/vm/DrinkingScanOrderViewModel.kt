package com.yunshang.haile_life.business.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.DeviceDetailEntity
import com.yunshang.haile_life.data.entities.GoodsScanEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.string.StringUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/9 17:16
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class DrinkingScanOrderViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    val unPayOrderNo: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val goodsScan: MutableLiveData<GoodsScanEntity> by lazy {
        MutableLiveData()
    }

    val shopName: MutableLiveData<String> = MutableLiveData("")

    val deviceDetail: MutableLiveData<DeviceDetailEntity> by lazy {
        MutableLiveData()
    }

    val isDrinking:LiveData<Boolean> = deviceDetail.map {
        DeviceCategory.isDrinking(it.categoryCode)
    }

    fun requestData(code: String?) {
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.hasUnPayOrder(ApiRepository.createRequestBody(""))
            )?.let {
                unPayOrderNo.postValue(it.orderNo)
            }

            // 如果扫码信息为空，重新请求
            val goodsScan = if (null == goodsScan.value) {
                ApiRepository.dealApiResult(
                    mDeviceRepo.requestGoodsScan(
                        imei = if (StringUtils.isImeiCode(code)) code else null,
                        n = if (StringUtils.isImeiCode(code)) null else code
                    )
                )?.also {
                    goodsScan.postValue(it)
                }
            } else goodsScan.value
            goodsScan?.let { scan ->
                // 商品详情
                if (null == deviceDetail.value) {
                    ApiRepository.dealApiResult(
                        mDeviceRepo.requestDeviceDetail(scan.goodsId!!)
                    )?.also {
                        deviceDetail.postValue(it)
                    }
                }
                ApiRepository.dealApiResult(
                    mShopRepo.requestShopDetail(scan.shopId!!)
                )?.let {
                    shopName.postValue(it.name)
                }
            }
        })
    }

    fun createOrder(callBack: (orderNo: String) -> Unit) {
        if (null == goodsScan.value?.goodsId) return
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.createLaterTrade(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "goodsId" to goodsScan.value?.goodsId
                        )
                    )
                )
            )?.let {
                withContext(Dispatchers.Main) {
                    callBack(it.orderNo)
                }
            }
        })
    }
}
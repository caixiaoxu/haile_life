package com.yunshang.haile_life.business.vm

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.MarketingService
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.string.StringUtils

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/4 13:57
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class ScanOrderViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)
    private val mMarketingRepo = ApiRepository.apiClient(MarketingService::class.java)

    val goodsScan: MutableLiveData<GoodsScanEntity> by lazy {
        MutableLiveData()
    }

    val shopConfig: MutableLiveData<ShopConfigEntity?> by lazy {
        MutableLiveData()
    }

    val isDryer: LiveData<Boolean> = goodsScan.map {
        DeviceCategory.isDryerOrHair(it.categoryCode)
    }

    val hint: LiveData<String> = goodsScan.map {
        when (it.categoryCode) {
            DeviceCategory.Washing -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_wash_hint)
            DeviceCategory.Dryer -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_dryer_hint)
            DeviceCategory.Shoes -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_shoes_hint)
            DeviceCategory.Hair -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_hair_hint)
            else -> ""
        }
    }

    val modelTitle: LiveData<String> = goodsScan.map {
        com.lsy.framelib.utils.StringUtils.getString(
            R.string.select_work_model,
            it.categoryName.replace("机", "")
        )
    }

    val timeTitle: LiveData<String> = goodsScan.map {
        com.lsy.framelib.utils.StringUtils.getString(
            R.string.select_work_time,
            it.categoryName.replace("机", "")
        )
    }

    val deviceDetail: MutableLiveData<DeviceDetailEntity> by lazy {
        MutableLiveData()
    }

    val shopUmpItem: MutableLiveData<ShopUmpItem> by lazy {
        MutableLiveData()
    }

    // 选择的设备模式
    val selectDeviceConfig: MutableLiveData<DeviceDetailItemEntity> by lazy {
        MutableLiveData()
    }

    // 选择的设备属性
    val selectExtAttr: MutableLiveData<ExtAttrBean?> by lazy {
        MutableLiveData()
    }

    // 是否可提交
    val canSubmit: MediatorLiveData<Boolean> = MediatorLiveData(false).apply {
        addSource(selectDeviceConfig) {
            value = checkSubmit()
        }
        addSource(selectExtAttr) {
            value = checkSubmit()
        }
    }

    /**
     * 检测是否可提交
     */
    private fun checkSubmit(): Boolean =
        null != selectDeviceConfig.value && null != selectExtAttr.value

    fun requestData(code: String?) {
        launch({
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
                val deviceDetail = if (null == deviceDetail.value) {
                    ApiRepository.dealApiResult(
                        mDeviceRepo.requestDeviceDetail(scan.goodsId)
                    )?.also {
                        deviceDetail.postValue(it)
                    }
                } else deviceDetail.value
                deviceDetail?.let { detail ->
                    (if (DeviceCategory.isHair(scan.categoryCode)) {
                        detail.items.find { item -> 1 == item.amount }
                    } else detail.items.firstOrNull())?.let { first ->
                        selectDeviceConfig.postValue(first)
                        first.getExtAttrs(DeviceCategory.isDryerOrHair(scan.categoryCode))
                            .firstOrNull()?.let { firstAttr ->
                                selectExtAttr.postValue(firstAttr)
                            }
                    }

                    requestShopList(detail.shopId, scan.goodsId, detail.categoryId)
                }
                if (scan.shopId > 0) {
                    ApiRepository.dealApiResult(
                        mMarketingRepo.requestShopUmpList(
                            ApiRepository.createRequestBody(
                                hashMapOf(
                                    "shopId" to scan.shopId
                                )
                            )
                        )
                    )?.let {
                        it.items.find { item -> item.promotionProduct == 5 }?.let { ump ->
                            shopUmpItem.postValue(ump)
                        }
                    }
                }
            }
        })
    }

    fun requestShopListAsync(shopId: Int, goodsId: Int, categoryId: Int) {
        launch({
            requestShopList(shopId, goodsId, categoryId)
        })
    }

    private suspend fun requestShopList(shopId: Int, goodsId: Int, categoryId: Int) {
        // 强制使用海星
        ApiRepository.dealApiResult(
            mShopRepo.requestShopConfigList(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "shopId" to shopId,
                        "goodsId" to goodsId,
                        "goodsCategoryId" to categoryId
                    )
                )
            )
        )?.let {
            shopConfig.postValue(it.find { item -> 1 == item.configType })
        } ?: shopConfig.postValue(null)
    }

    fun closeRechargeStarfish(v: View) {
        shopUmpItem.value = null
    }
}
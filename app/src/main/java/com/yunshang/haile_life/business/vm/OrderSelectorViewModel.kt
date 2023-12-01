package com.yunshang.haile_life.business.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.network.exception.CommonCustomException
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.agruments.NewOrderParams
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.math.BigDecimal

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/10 10:09
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class OrderSelectorViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)
    var deviceId: Int = -1

    val isHideDeviceInfo: MutableLiveData<Boolean> = MutableLiveData(true)
    val deviceDetail: MutableLiveData<DeviceDetailEntity> by lazy { MutableLiveData() }
    val stateList: MutableLiveData<List<DeviceStateEntity>?> by lazy { MutableLiveData() }

    val shopNotice: MutableLiveData<MutableList<ShopNoticeEntity>> by lazy {
        MutableLiveData()
    }

    // 选择的设备模式
    val selectDeviceConfig: MutableLiveData<DeviceDetailItemEntity> by lazy {
        MutableLiveData()
    }

    // 选择的设备属性
    val selectExtAttr: MutableLiveData<ExtAttrDtoItem?> by lazy {
        MutableLiveData()
    }

    var selectAttachSku: MutableMap<Int, MutableLiveData<ExtAttrDtoItem?>> = mutableMapOf()

    val needAttach: LiveData<Boolean> = selectDeviceConfig.map {
        (it?.let { item ->
            !(item.name == "单脱" || item.name == "筒自洁") && (it.attachMap?.get(DeviceDetailEntity.Dispenser)
                ?: false)
        } ?: false)
    }

    val needSelfClean: LiveData<Boolean> = selectDeviceConfig.map {
        (it?.attachMap?.get(DeviceDetailEntity.SelfClean) ?: false)
    }

    val isDryer: LiveData<Boolean> = deviceDetail.map {
        DeviceCategory.isDryerOrHair(it.categoryCode)
    }

    val totalPriceVal: MutableLiveData<Double> by lazy {
        MutableLiveData()
    }

    val attachConfigureVal: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val modelTitle: LiveData<String> = deviceDetail.map {
        StringUtils.getString(
            R.string.select_work_model,
            it.categoryName.replace("机", "")
        )
    }

    val selectSelfClean: MutableLiveData<Boolean> = MutableLiveData(false)

    /**
     * 切换选择功能的配置
     */
    fun changeDeviceConfig(itemEntity: DeviceDetailItemEntity) {
        (itemEntity.extAttrDto.items.firstOrNull() { item ->
            item.isEnabled && item.isDefault
        }
            ?: itemEntity.extAttrDto.items.firstOrNull { item -> item.isEnabled })?.let { firstAttr ->
            selectExtAttr.value = firstAttr
        }
    }

    fun totalPrice() {
        var attachTotal = BigDecimal("0.0")
        if (true == needAttach.value) {
            selectAttachSku.forEach { item ->
                if (!item.value.value?.unitPrice.isNullOrEmpty()) {
                    attachTotal = attachTotal.add(BigDecimal(item.value.value!!.unitPrice))
                }
            }
        }
        if (true == needSelfClean.value && true == selectSelfClean.value) {
            if (!deviceDetail.value?.selfCleanValue?.price.isNullOrEmpty()) {
                attachTotal =
                    attachTotal.add(BigDecimal(deviceDetail.value!!.selfCleanValue!!.price))
            }
        }
        totalPriceVal.value =
            BigDecimal(selectExtAttr.value?.unitPrice ?: "0.0").add(attachTotal).toDouble()
    }

    fun attachConfigure() {
        var configure = ""
        if (true == needAttach.value) {
            selectAttachSku.forEach { item ->
                val name = deviceDetail.value?.attachItems?.find { it -> it.id == item.key }?.name
                if (!name.isNullOrEmpty()) {
                    item.value.value?.let {
                        if (it.unitPrice.isNotEmpty()) {
                            configure += "+" + name + "￥" + it.unitPrice
                        }
                    }
                }
            }
        }
        if (true == needSelfClean.value && true == selectSelfClean.value) {
            configure += "+" + "筒自洁" + "￥" + deviceDetail.value?.selfCleanValue?.price
        }
        attachConfigureVal.value = if (configure.isNotEmpty()) {
            configure.substring(1)
        } else configure
    }

    val hint: LiveData<String> = deviceDetail.map {
        when (it.categoryCode) {
            DeviceCategory.Washing -> StringUtils.getString(R.string.scan_order_wash_hint)
            DeviceCategory.Dryer -> StringUtils.getString(R.string.scan_order_dryer_hint)
            DeviceCategory.Shoes -> StringUtils.getString(R.string.scan_order_shoes_hint)
            DeviceCategory.Hair -> StringUtils.getString(R.string.scan_order_hair_hint)
            else -> ""
        }
    }

    fun requestData() {
        if (-1 == deviceId) return
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestDeviceDetail(deviceId)
            )?.also { detail ->
                ApiRepository.dealApiResult(
                    mDeviceRepo.requestDeviceStateList(
                        ApiRepository.createRequestBody(
                            hashMapOf("goodsId" to deviceId)
                        )
                    )
                )?.let { deviceStateList ->
                    // 初始化关联的sku
                    if (detail.hasAttachGoods && !detail.attachItems.isNullOrEmpty()) {
                        selectAttachSku = mutableMapOf()
                        detail.attachItems?.forEach { item ->
                            if (item.extAttrDto.items.isNotEmpty()) {
                                selectAttachSku[item.id] =
                                    item.extAttrDto.items.find { dto -> dto.isEnabled && dto.isDefault }
                                        ?.let { default ->
                                            MutableLiveData(default)
                                        } ?: MutableLiveData(null)
                            }
                        }
                    }

                    withContext(Dispatchers.Main) {
                        totalPrice()
                        attachConfigure()
                    }

                    deviceDetail.postValue(detail)

                    // 吹风机只选中未使用，
                    val list = detail.items.filter { item -> 1 == item.soldState }
                    if (DeviceCategory.isHair(detail.categoryCode)) {
                        list.find { item -> 1 == item.amount }
                    } else {
                        //如果没有默认，就显示第一个
                        list.find { item -> item.extAttrDto.items.any { attr -> attr.isEnabled && attr.isDefault } }
                            ?: run { list.firstOrNull() }
                    }?.let { first ->
                        selectDeviceConfig.postValue(first)
                        withContext(Dispatchers.Main) {
                            changeDeviceConfig(first)
                        }
                    }

                    stateList.postValue(deviceStateList.stateList)
                }

                // 是否有公告
                if (null == shopNotice.value) {
                    ApiRepository.dealApiResult(
                        mShopRepo.requestShopNotice(
                            ApiRepository.createRequestBody(
                                hashMapOf(
                                    "shopId" to detail.shopId
                                )
                            )
                        )
                    )?.let {
                        shopNotice.postValue(it)
                    }
                }
            }
        })
    }

    fun submitOrder(
        params: NewOrderParams,
        callback: (success: Boolean, result: OrderSubmitResultEntity?) -> Unit
    ) {
        launch({
            val body = ApiRepository.createRequestBody(GsonUtils.any2Json(params))
            ApiRepository.dealApiResult(
                if (null == params.reserveMethod) {
                    mOrderRepo.scanOrderCreate(body)
                } else {
                    mOrderRepo.reserveCreate(body)
                }
            )?.let {
                LiveDataBus.post(BusEvents.ORDER_SUBMIT_STATUS, true)
                withContext(Dispatchers.Main) {
                    callback(true, it)
                }
            }
        }, {
            withContext(Dispatchers.Main) {
                // 自己定义的错误显示报错提示
                if (it is CommonCustomException) {
                    it.message?.let { it1 -> SToast.showToast(msg = it1) }
                } else {
                    SToast.showToast(msg = "网络开小差~")
                }
                Timber.d("请求失败或异常$it")
                callback(false, null)
            }
        })
    }
}
package com.yunshang.haile_life.business.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.agruments.AppointmentOrderParams
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
class AppointmentOrderSelectorViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)
    var deviceId: Int = -1

    val isHideDeviceInfo: MutableLiveData<Boolean> = MutableLiveData(true)
    val deviceDetail: MutableLiveData<DeviceDetailEntity> by lazy { MutableLiveData() }
    val stateList: MutableLiveData<List<DeviceStateEntity>?> by lazy { MutableLiveData() }

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
        it?.let { item ->
            !(item.name == "单脱" || item.name == "筒自洁")
        } ?: false
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
        selectAttachSku.forEach { item ->
            if (!item.value.value?.unitPrice.isNullOrEmpty()) {
                attachTotal = attachTotal.add(BigDecimal(item.value.value!!.unitPrice))
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
        attachConfigureVal.value = if (configure.isNotEmpty()) {
            configure.substring(1)
        } else configure
    }

    val hint: LiveData<String> = deviceDetail.map {
        when (it.categoryCode) {
            DeviceCategory.Washing -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_wash_hint)
            DeviceCategory.Dryer -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_dryer_hint)
            DeviceCategory.Shoes -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_shoes_hint)
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
                    deviceDetail.postValue(detail)
                    stateList.postValue(deviceStateList.stateList)
                }

                val list = detail.items.filter { item -> 1 == item.soldState }
                //如果没有默认，就显示第一个
                (list.find { item -> item.extAttrDto.items.any { attr -> attr.isEnabled && attr.isDefault } }
                    ?: run { list.firstOrNull() })?.let { first ->
                    selectDeviceConfig.postValue(first)
                    withContext(Dispatchers.Main) {
                        changeDeviceConfig(first)
                    }
                }

                if (detail.hasAttachGoods && !detail.attachItems.isNullOrEmpty()) {
                    // 初始化关联的sku
                    selectAttachSku = mutableMapOf()
                    detail.attachItems.forEach { item ->
                        if (item.extAttrDto.items.isNotEmpty()) {
                            selectAttachSku[item.id] =
                                item.extAttrDto.items.find { dto -> dto.isEnabled && dto.isDefault }
                                    ?.let { default ->
                                        MutableLiveData(default)
                                    } ?: MutableLiveData(null)
                        }
                    }
                }
            }
        })
    }

    fun submitOrder(
        params: AppointmentOrderParams,
        callback: (result: OrderSubmitResultEntity) -> Unit
    ) {
        launch({
            val body = ApiRepository.createRequestBody(GsonUtils.any2Json(params))
            ApiRepository.dealApiResult(
                if (null == params.reserveMethod) {
                    mOrderRepo.lockOrderCreate(body)
                } else {
                    mOrderRepo.reserveCreate(body)
                }
            )?.let {
                withContext(Dispatchers.Main) {
                    callback(it)
                }
            }
        })
    }
}
package com.yunshang.haile_life.business.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.AppointSpec
import com.yunshang.haile_life.data.entities.DeviceDetailEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.rule.IOrderConfigEntity

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
class AppointmentSubmitViewModel : BaseViewModel() {
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)
    var deviceId: Int = -1

    val isHideDeviceInfo:MutableLiveData<Boolean> = MutableLiveData(true)
    val deviceDetail: MutableLiveData<DeviceDetailEntity> by lazy {
        MutableLiveData()
    }

    val autoRefund: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    val isDryer: LiveData<Boolean> = deviceDetail.map {
        DeviceCategory.isDryerOrHair(it.categoryCode)
    }

    val hint: LiveData<String> = deviceDetail.map {
        when (it.categoryCode) {
            DeviceCategory.Washing -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_wash_hint)
            DeviceCategory.Dryer -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_dryer_hint)
            DeviceCategory.Shoes -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_shoes_hint)
            else -> ""
        }
    }

    val modelTitle: LiveData<String> = deviceDetail.map {"模式"//TODO 后端加字段
//        com.lsy.framelib.utils.StringUtils.getString(
//            R.string.select_work_model,
//            it.categoryCodeName.replace("机", "")
//        )
    }

    val timeTitle: LiveData<String> = deviceDetail.map {"时长"//TODO 后端加字段
//        com.lsy.framelib.utils.StringUtils.getString(
//            R.string.select_work_time,
//            it.categoryCodeName.replace("机", "")
//        )
    }

    val specList: MutableLiveData<List<AppointSpec>> by lazy {
        MutableLiveData()
    }

    val selectSpec: MutableLiveData<AppointSpec> by lazy {
        MutableLiveData()
    }

    val minuteList: LiveData<List<MinuteEntity>> = selectSpec.map {
        if (DeviceCategory.isDryerOrHair(deviceDetail.value?.categoryCode)) {
            it.unitList.map { unit ->
                MinuteEntity(unit)
            }
        } else {
            listOf(MinuteEntity(it.extAttr.minutes))
        }
    }

    val selectMinute: MutableLiveData<Int> by lazy {
        MutableLiveData()
    }

    fun requestData() {
        if (-1 == deviceId) return
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestDeviceDetail(deviceId)
            )?.also {
                deviceDetail.postValue(it)

                ApiRepository.dealApiResult(
                    mShopRepo.requestAppointSpecList(
                        ApiRepository.createRequestBody(
                            hashMapOf(
                                "shopId" to it.shopId,
                                "goodsCategoryId" to it.categoryId,
                            )
                        )
                    )
                )?.let {appointSpecList->
                    specList.postValue(appointSpecList.specValueList)
                    appointSpecList.specValueList.firstOrNull()?.let { first ->
                        selectSpec.postValue(first)
                    }
                }

//                ApiRepository.dealApiResult(
//                    mShopRepo.requestAppointCategoryList(
//                        ApiRepository.createRequestBody(
//                            hashMapOf(
//                                "shopId" to it.shopId
//                            )
//                        )
//                    )
//                )?.let {
//                    autoRefund.postValue(1 == it.autoRefund)
//                    categoryList.postValue(it.categoryList)
//                    it.categoryList.firstOrNull()?.let { first ->
//                        selectCategory.postValue(first)
//                    }
//                }
            }
        })
    }

    class MinuteEntity(val minute: Int) : IOrderConfigEntity {
        override fun getTitle(code: String?): String =
            "${minute}${com.lsy.framelib.utils.StringUtils.getString(R.string.minute)}"

        override fun getTitleTxtColor(code: String?): Int =
            if (DeviceCategory.isDryerOrHair(code)) R.color.selector_black85_ff630e
            else R.color.selector_black85_04d1e5

        override fun getTitleBg(code: String?): Int =
            if (DeviceCategory.isDryerOrHair(code)) R.drawable.selector_device_model_item_dryer
            else R.drawable.selector_device_model_item1

        override fun defaultVal(): Boolean = false
    }
}
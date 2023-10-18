package com.yunshang.haile_life.business.vm

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.AppointCategory
import com.yunshang.haile_life.data.entities.AppointDevice
import com.yunshang.haile_life.data.entities.AppointSpec
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.rule.IOrderConfigEntity
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.string.StringUtils
import java.util.*

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
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)
    var shopId: Int = -1

    val autoRefund: MutableLiveData<Boolean> by lazy {
        MutableLiveData()
    }

    val categoryList: MutableLiveData<List<AppointCategory>> by lazy {
        MutableLiveData()
    }

    val selectCategory: MutableLiveData<AppointCategory> by lazy {
        MutableLiveData()
    }

    val isDryer: LiveData<Boolean> = selectCategory.map {
        DeviceCategory.isDryerOrHair(it.goodsCategoryCode)
    }

    val hint: LiveData<String> = selectCategory.map {
        when (it.goodsCategoryCode) {
            DeviceCategory.Washing -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_wash_hint)
            DeviceCategory.Dryer -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_dryer_hint)
            DeviceCategory.Shoes -> com.lsy.framelib.utils.StringUtils.getString(R.string.scan_order_shoes_hint)
            else -> ""
        }
    }

    val modelTitle: LiveData<String> = selectCategory.map {
        com.lsy.framelib.utils.StringUtils.getString(
            R.string.select_work_model,
            it.goodsCategoryName.replace("机", "")
        )
    }

    val timeTitle: LiveData<String> = selectCategory.map {
        com.lsy.framelib.utils.StringUtils.getString(
            R.string.select_work_time,
            it.goodsCategoryName.replace("机", "")
        )
    }

    val deviceTitle: LiveData<String> = selectCategory.map {
        com.lsy.framelib.utils.StringUtils.getString(
            R.string.select_work_device,
            it.goodsCategoryName.replace("机", "")
        )
    }

    val specList: MutableLiveData<List<AppointSpec>> by lazy {
        MutableLiveData()
    }

    val selectSpec: MutableLiveData<AppointSpec> by lazy {
        MutableLiveData()
    }

    val minuteList: LiveData<List<MinuteEntity>> = selectSpec.map {
        if (DeviceCategory.isDryerOrHair(selectCategory.value?.goodsCategoryCode)) {
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
    val selectDevice: MutableLiveData<AppointDevice> by lazy {
        MutableLiveData()
    }

    val selectDeviceVal: LiveData<SpannableString> = selectDevice.map {
        it?.let {
            val prefix = com.lsy.framelib.utils.StringUtils.getString(
                R.string.appoint_device_queue_use_time,
                it.goodsName,
                it.queueNum
            )

            val useDate = DateTimeUtils.formatDateFromString(it.appointmentTime)
            val useDateVal = if (DateTimeUtils.isSameDay(useDate, Date())) {
                "今天${DateTimeUtils.formatDateTime(useDate, "HH:mm")}"
            } else it.appointmentTime

            val content = "$prefix ${useDateVal}"
            StringUtils.formatMultiStyleStr(
                content,
                arrayOf(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            Constants.APP_CONTEXT, R.color.secondColorPrimary
                        )
                    )
                ), prefix.length, content.length
            )
        } ?: SpannableString("")
    }

    fun requestData() {
        if (-1 == shopId) return
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestAppointCategoryList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "shopId" to shopId
                        )
                    )
                )
            )?.let {
                autoRefund.postValue(1 == it.autoRefund)
                categoryList.postValue(it.categoryList)
                it.categoryList.firstOrNull()?.let { first ->
                    selectCategory.postValue(first)
                }
            }
        })
    }

    fun requestAppointSpecListAsync(context: Context) {
        if (-1 == shopId) return
        if (selectCategory.value == null) return SToast.showToast(context, "请先选择设备类型")

        launch({
            ApiRepository.dealApiResult(
                mShopRepo.requestAppointSpecList(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "shopId" to shopId,
                            "goodsCategoryId" to selectCategory.value?.goodsCategoryId,
                        )
                    )
                )
            )?.let {
                specList.postValue(it.specValueList)
                it.specValueList.firstOrNull()?.let { first ->
                    selectSpec.postValue(first)
                }
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
package com.yunshang.haile_life.business.vm

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.business.apiService.CommonService
import com.yunshang.haile_life.business.apiService.DeviceService
import com.yunshang.haile_life.data.entities.FaultCategoryEntity
import com.yunshang.haile_life.data.entities.GoodsScanEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.utils.string.StringUtils
import timber.log.Timber

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/16 10:56
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class FaultRepairsViewModel : BaseViewModel() {
    private val mCommonRepo = ApiRepository.apiClient(CommonService::class.java)
    private val mDeviceRepo = ApiRepository.apiClient(DeviceService::class.java)

    val scanDevice: MutableLiveData<GoodsScanEntity> by lazy {
        MutableLiveData()
    }

    val faultCategorys: MutableLiveData<MutableList<FaultCategoryEntity>> by lazy {
        MutableLiveData()
    }

    val faultType: MutableLiveData<FaultCategoryEntity> by lazy {
        MutableLiveData()
    }

    val faultDesc: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val faultPics: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())

    fun requestCategory(code: String) {
        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.requestGoodsScan(
                    imei = if (StringUtils.isImeiCode(code)) code else null,
                    n = if (StringUtils.isImeiCode(code)) null else code,
                    ifThrowException = false
                )
            )?.let {
                scanDevice.postValue(it)

                ApiRepository.dealApiResult(
                    mDeviceRepo.requestFaultTypes(it.categoryCode)
                )?.let { categoryList ->
                    faultCategorys.postValue(categoryList)
                }
            }
        })
    }

    /**
     * 上传头像
     */
    fun uploadHeadIcon(paths: List<String>) {
        launch({
            var list = mutableListOf<String>()
            paths.forEach { path ->
                ApiRepository.dealApiResult(
                    mCommonRepo.updateLoadFile(
                        ApiRepository.createFileUploadBody(path)
                    )
                )?.let {
                    Timber.i("图片上传成功：$it")
                    list.add(it)
                }
            }

            faultPics.value?.let {
                it.addAll(list)
                list = it
            }

            faultPics.postValue(list)
        })
    }

    fun submitFaultRepairs(context: Context) {
        if (null == scanDevice.value?.deviceId) {
            SToast.showToast(context, "请选择选择设备")
            return
        }
        if (null == faultType.value?.fixTypeCode) {
            SToast.showToast(context, "请选择故障类型")
            return
        }
        if (faultDesc.value.isNullOrEmpty()) {
            SToast.showToast(context, "请输入故障描述")
            return
        }

        launch({
            ApiRepository.dealApiResult(
                mDeviceRepo.submitFaultRepairs(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "goodsId" to scanDevice.value?.goodsId,
                            "deviceId" to scanDevice.value?.deviceId,
                            "fixSubTypeCode" to faultType.value?.fixTypeCode,
                            "description" to faultDesc.value,
                            "pics" to faultPics.value
                        )
                    )
                )
            )?.let {
                jump.postValue(1)
            }
        })
    }
}
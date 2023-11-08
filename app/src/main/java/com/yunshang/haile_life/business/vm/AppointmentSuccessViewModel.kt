package com.yunshang.haile_life.business.vm

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.entities.AppointmentStateListEntity
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/11/3 11:14
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AppointmentSuccessViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    val appointStateList: MutableLiveData<AppointmentStateListEntity> by lazy { MutableLiveData() }

    fun requestData(orderNo: String?) {
        if (orderNo.isNullOrEmpty()) return

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestAppointStateList(
                    ApiRepository.createRequestBody(
                        hashMapOf("orderNo" to orderNo)
                    )
                )
            )?.let {
                appointStateList.postValue(it)
            }
        })
    }
}
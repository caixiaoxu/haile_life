package com.yunshang.haile_life.business.vm

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.ShopService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.model.ApiRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/12 13:51
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class StarfishRefundViewModel : BaseViewModel() {
    private val mShopRepo = ApiRepository.apiClient(ShopService::class.java)

    var shopIdList: IntArray? = null

    var totalAmount: Double = 0.0

    val transAccount: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    val transRealName: MutableLiveData<String> by lazy {
        MutableLiveData()
    }

    fun submitRefundApply(v: View) {
        if (null == shopIdList || shopIdList!!.isEmpty()) return
        if (transAccount.value.isNullOrEmpty() || transRealName.value.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(
                mShopRepo.submitRefundApply(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                            "shopIdList" to shopIdList,
                            "transAccount" to transAccount.value,
                            "transAccountType" to 1,
                            "transRealName" to transRealName.value,
                        )
                    )
                )
            )
            LiveDataBus.post(BusEvents.STARFISH_REFUND_STATUS, true)
        })
    }
}
package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.network.exception.CommonCustomException
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.base.BaseViewModel
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.rule.IndicatorEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/5 19:45
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class OrderViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    var isAppoint: Boolean = false

    var curOrderStatus: MutableLiveData<Int?> = MutableLiveData()

    // 订单状态 ，100：待支付；500：进行中；1000：已完成；2000：退款中；2099：已退款
    val orderStatus: MutableLiveData<List<IndicatorEntity<Int?>>> = MutableLiveData(
        arrayListOf(
            IndicatorEntity("全部", 0, null),
            IndicatorEntity("未支付", 0, 100),
            IndicatorEntity("已支付", 0, 500),
            IndicatorEntity("已完成", 0, 1000),
//            IndicatorEntity("申请退款", 0, 2000),
            IndicatorEntity("已退款", 0, 2099),
        )
    )

    fun requestOrderList(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out OrderEntity>?) -> Unit
    ) {
        val params = hashMapOf(
            "page" to page,
            "pageSize" to pageSize,
            "orderState" to curOrderStatus.value
        )

        if (isAppoint) {
            params["orderType"] = 300
        }

        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.requestOrderList(
                    ApiRepository.createRequestBody(
                        params
                    )
                )
            )?.let {
                withContext(Dispatchers.Main) {
                    callBack.invoke(it)
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
                callBack.invoke(null)
            }
        }, showLoading = 1 == page)
    }
}
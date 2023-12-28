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

    var curOrderStatus: MutableLiveData<Int?> = MutableLiveData()

    //新订单状态<1待支付、2进行中、3已完成、4已退款>
    val orderStatus: MutableLiveData<List<IndicatorEntity<Int?>>> = MutableLiveData(
        arrayListOf(
            IndicatorEntity("全部", 0, null),
            IndicatorEntity("待支付", 0, 1),
            IndicatorEntity("进行中", 0, 2),
            IndicatorEntity("已完成", 0, 3),
//            IndicatorEntity("已退款", 0, 4),
        )
    )

    fun requestReplyNum() {
        launch({
            requestReplyNumAsync()
        })
    }

    private suspend fun requestReplyNumAsync() {
        ApiRepository.dealApiResult(
            mOrderRepo.requestReplayNum(
                ApiRepository.createRequestBody(hashMapOf())
            )
        )
    }

    fun requestOrderList(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out OrderEntity>?) -> Unit
    ) {
        val params = hashMapOf(
            "page" to page,
            "pageSize" to pageSize,
            "newOrderState" to curOrderStatus.value
        )

        launch({
            if (1 == page) {
                requestReplyNumAsync()
            }

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

    fun requestOrderDetail(orderNo: String?, callBack: (OrderEntity) -> Unit) {
        if (orderNo.isNullOrEmpty()) return
        launch({
            ApiRepository.dealApiResult(mOrderRepo.requestOrderDetail(orderNo))?.let {
                callBack(it)
            }
        })
    }
}
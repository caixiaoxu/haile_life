package com.yunshang.haile_life.business.vm

import androidx.lifecycle.MutableLiveData
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.ui.base.BaseViewModel
import com.yunshang.haile_life.business.apiService.OrderService
import com.yunshang.haile_life.data.entities.EvaluateReplyEntity
import com.yunshang.haile_life.data.model.ApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Title :
 * Author: Lsy
 * Date: 2024/1/3 10:24
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class EvaluateReplyListViewModel : BaseViewModel() {
    private val mOrderRepo = ApiRepository.apiClient(OrderService::class.java)

    val replayNum: MutableLiveData<Int> by lazy {
        MutableLiveData()
    }

    fun requestData(
        page: Int,
        pageSize: Int,
        callBack: (responseList: ResponseList<out EvaluateReplyEntity>?) -> Unit
    ) {
        launch({
            if (1 == page) {
                requestReplyNum()
            }
            val result = requestReplyList(page, pageSize)
            withContext(Dispatchers.Main) {
                callBack(result)
            }
        }, {
            withContext(Dispatchers.Main) {
                callBack(null)
            }
        })
    }

    fun requestReplyNumAsync() {
        launch({
            requestReplyNum()
        })
    }

    private suspend fun requestReplyNum() {
        ApiRepository.dealApiResult(
            mOrderRepo.requestReplayNum(
                ApiRepository.createRequestBody(hashMapOf())
            )
        )?.let {
            replayNum.postValue(it)
        }
    }

    private suspend fun requestReplyList(pageNum: Int, pageSize: Int = 20) =
        ApiRepository.dealApiResult(
            mOrderRepo.requestEvaluateReplyList(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "pageNum" to pageNum,
                        "pageSize" to pageSize,
                    )
                )
            )
        )

    fun submitEvaluateViewAllReply(callback: () -> Unit) {
        launch({
            ApiRepository.dealApiResult(
                mOrderRepo.submitEvaluateViewAllReply(
                    ApiRepository.createRequestBody(
                        hashMapOf(
                        )
                    )
                )
            )
            replayNum.postValue(0)
            withContext(Dispatchers.Main) {
                callback()
            }
        })
    }
}
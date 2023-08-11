package com.yunshang.haile_life.business.apiService

import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.network.response.ResponseWrapper
import com.yunshang.haile_life.data.entities.*
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/3 14:24
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface OrderService {
    @POST("/trade/preview")
    suspend fun requestTradePreview(@Body body: RequestBody): ResponseWrapper<TradePreviewEntity>

    @POST("/trade/create")
    suspend fun createTrade(@Body body: RequestBody): ResponseWrapper<CreateTradeEntity>

    @POST("/trade/payLaterCreate")
    suspend fun createLaterTrade(@Body body: RequestBody): ResponseWrapper<CreateTradeEntity>

    @POST("/pay/prePay")
    suspend fun prePay(@Body body: RequestBody): ResponseWrapper<PrePayEntity>

    @POST("/pay/pay")
    suspend fun balancePay(@Body body: RequestBody): ResponseWrapper<Any>

    @POST("/pay/asyncPay")
    suspend fun asyncPay(@Body body: RequestBody): ResponseWrapper<AsyncPayEntity>

    @POST("/trade/list")
    suspend fun requestOrderList(@Body body: RequestBody): ResponseWrapper<ResponseList<OrderEntity>>

    @GET("/trade/detail")
    suspend fun requestOrderDetail(@Query("orderNo") orderNo: String): ResponseWrapper<OrderEntity>

    @POST("/trade/cancel")
    suspend fun cancelOrder(@Body body: RequestBody): ResponseWrapper<Any>

    @POST("/appointment/cancel")
    suspend fun cancelAppointOrder(@Body body: RequestBody): ResponseWrapper<Any>

    @GET("/trade/getTradeOrderStateCountList")
    suspend fun requestOrderCountOfStatus(): ResponseWrapper<MutableList<OrderCountOfStatusEntity>>

    @POST("/appointment/using")
    suspend fun useAppointOrder(@Body body: RequestBody): ResponseWrapper<MutableList<Any>>

    @POST("/trade/queryUnPayOrder")
    suspend fun hasUnPayOrder(@Body body: RequestBody): ResponseWrapper<CreateTradeEntity>

}
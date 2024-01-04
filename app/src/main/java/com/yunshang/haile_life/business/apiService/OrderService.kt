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
    @GET("/trade/deviceError")
    suspend fun requestOrderError(@Query("orderNo") orderNo: String): ResponseWrapper<OrderEntity>

    @GET("/trade/detail/simple")
    suspend fun requestOrderDetailSimple(@Query("orderNo") orderNo: String): ResponseWrapper<OrderEntity>

    @POST("/trade/cancel")
    suspend fun cancelOrder(@Body body: RequestBody): ResponseWrapper<Any>

    @POST("/trade/hidden")
    suspend fun deleteOrder(@Body body: RequestBody): ResponseWrapper<Any>

    @POST("/appointment/cancel")
    suspend fun cancelAppointOrder(@Body body: RequestBody): ResponseWrapper<Any>

    @GET("/trade/getTradeOrderStateCountList")
    suspend fun requestOrderCountOfStatus(): ResponseWrapper<MutableList<OrderCountOfStatusEntity>>

    @POST("/appointment/using")
    suspend fun useAppointOrder(@Body body: RequestBody): ResponseWrapper<MutableList<Any>>

    @POST("/trade/queryUnPayOrder")
    suspend fun hasUnPayOrder(@Body body: RequestBody): ResponseWrapper<CreateTradeEntity>

    @POST("/trade/startByOrder")
    suspend fun startByOrder(@Body body: RequestBody): ResponseWrapper<Any>

    @POST("/trade/finishByOrder")
    suspend fun finishByOrder(@Body body: RequestBody): ResponseWrapper<Any>

    @POST("/trade/lockOrderCreate")
    suspend fun lockOrderCreate(@Body body: RequestBody): ResponseWrapper<OrderSubmitResultEntity>

    @POST("/trade/scanOrderCreate")
    suspend fun scanOrderCreate(@Body body: RequestBody): ResponseWrapper<OrderSubmitResultEntity>

    @POST("/trade/reserveCreate")
    suspend fun reserveCreate(@Body body: RequestBody): ResponseWrapper<OrderSubmitResultEntity>

    @POST("/appointment/order/stateList")
    suspend fun requestAppointStateList(@Body body: RequestBody): ResponseWrapper<AppointmentStateListEntity>

    @POST("/appointment/order/stateQuery")
    suspend fun requestAppointStateQuery(@Body body: RequestBody): ResponseWrapper<AppointmentStateEntity>

    @POST("/trade/underway/preview")
    suspend fun requestUnderWayOrderPreview(@Body body: RequestBody): ResponseWrapper<TradePreviewEntity>

    @POST("/trade/underway/create")
    suspend fun createUnderWayOrder(@Body body: RequestBody): ResponseWrapper<OrderSubmitResultEntity>

    @POST("/trade/fulfillment/start")
    suspend fun startAppointOrderDevice(@Body body: RequestBody): ResponseWrapper<Any>
    @POST("/trade/check/reSendCode")
    suspend fun sendDeviceVerifyCode(@Body body: RequestBody): ResponseWrapper<Any>
    @POST("/trade/check/verify")
    suspend fun verifyDeviceVerifyCode(@Body body: RequestBody): ResponseWrapper<Any>
    @POST("/trade/underway/stateList")
    suspend fun requestOrderStateList(): ResponseWrapper<OrderStateListEntity>
    @POST("/device/selfClean/refresh")
    suspend fun requestSelfCleanRefresh(@Body body: RequestBody): ResponseWrapper<SelfCleanRefreshEntity>
    @POST("/device/selfClean/start")
    suspend fun startSelfClean(@Body body: RequestBody): ResponseWrapper<Any>
}
package com.yunshang.haile_life.business.apiService

import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.network.response.ResponseWrapper
import com.yunshang.haile_life.data.entities.*
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Title :
 * Author: Lsy
 * Date: 2023/3/17 16:35
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface DeviceService {
    @GET("/goods/scan")
    suspend fun requestGoodsScan(
        @Query("imei") imei: String? = null,
        @Query("sn") sn: String? = null,
        @Query("n") n: String? = null
    ): ResponseWrapper<GoodsScanEntity>

    @GET("/goods/normal/items")
    suspend fun requestDeviceDetailItem(@Query("goodsId") goodsId: Int): ResponseWrapper<MutableList<DeviceDetailItemEntity>>

    @GET("/goods/normal/details")
    suspend fun requestDeviceDetail(@Query("goodsId") goodsId: Int): ResponseWrapper<DeviceDetailEntity>

    @POST("/goods/stateList")
    suspend fun requestDeviceStateList(@Body body: RequestBody): ResponseWrapper<DeviceStateListEntity>

    /**
     * 卡片列表接口
     */
    @POST("/card/getAll")
    suspend fun requestCardList(@Body body: RequestBody): ResponseWrapper<ResponseList<CardEntity>>

    /**
     * 绑卡接口
     */
    @POST("/card/bind/submit")
    suspend fun bindingCard(@Body body: RequestBody): ResponseWrapper<Any>

    /**
     * 解绑卡接口
     */
    @POST("/card/unbind")
    suspend fun unbindCard(@Body body: RequestBody): ResponseWrapper<Any>

    /**
     * 绑卡验证码接口
     */
    @POST("/card/bind/code")
    suspend fun sendBindCode(@Body body: RequestBody): ResponseWrapper<Any>

    /**
     * 下单前验证设备接口
     */
    @POST("/goods/verify")
    suspend fun verifyGoods(@Body body: RequestBody): ResponseWrapper<GoodsVerify>

    /**
     * 水控码生成带状态接口
     */
    @POST("/waterCode/WaterCodeGenerateWithState")
    suspend fun generateWaterCode(@Body body: RequestBody): ResponseWrapper<WaterControlCodeEntity>

    /**
     * 修改水控码状态接口
     */
    @POST("/waterCode/updateWaterCodeState")
    suspend fun updateWaterCode(@Body body: RequestBody): ResponseWrapper<Any>

    /**
     * 修改水控码状态接口
     */
    @GET("/waterCode/getWaterCodeByUserId")
    suspend fun requestWaterCode(): ResponseWrapper<WaterControlCodeEntity>

    /**
     * 错误列表接口
     */
    @POST("/deviceFix/getTypes/{goodsCategory}")
    suspend fun requestFaultTypes(@Path("goodsCategory") goodsCategory: String): ResponseWrapper<MutableList<FaultCategoryEntity>>

    /**
     * 提交错误接口
     */
    @POST("/deviceFix/create")
    suspend fun submitFaultRepairs(@Body body: RequestBody): ResponseWrapper<Any>

    /**
     * 请求报修记录列表接口
     */
    @POST("/deviceFix/list")
    suspend fun requestFaultRepairsRecordList(@Body body: RequestBody): ResponseWrapper<ResponseList<FaultRepairsRecordEntity>>

    /**
     * 请求报修记录详情接口
     */
    @POST("/deviceFix/detail/{id}")
    suspend fun requestFaultRepairsRecordDetails(@Path("id") id: Int): ResponseWrapper<FaultRepairsRecordEntity>

}
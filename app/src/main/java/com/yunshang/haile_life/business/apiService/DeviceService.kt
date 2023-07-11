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
}
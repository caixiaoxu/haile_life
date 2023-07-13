package com.yunshang.haile_life.business.apiService

import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.network.response.ResponseWrapper
import com.yunshang.haile_life.data.entities.ADEntity
import com.yunshang.haile_life.data.entities.DiscountCouponEntity
import com.yunshang.haile_life.data.entities.ShopUmpListEntity
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
interface MarketingService {

    @POST("/slot/get")
    suspend fun requestAD(@Body body: RequestBody): ResponseWrapper<ADEntity>

    @POST("/shopUmp/list")
    suspend fun requestShopUmpList(@Body body: RequestBody): ResponseWrapper<ShopUmpListEntity>

    @POST("/coupon/myCoupon")
    suspend fun requestDiscountCouponList(@Body body: RequestBody): ResponseWrapper<ResponseList<DiscountCouponEntity>>

    @GET("/coupon/myCouponCount")
    suspend fun requestDiscountCouponNum(): ResponseWrapper<Any>
}
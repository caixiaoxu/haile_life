package com.yunshang.haile_life.business.apiService

import com.lsy.framelib.network.response.ResponseWrapper
import com.yunshang.haile_life.data.entities.BalanceEntity
import com.yunshang.haile_life.data.entities.ShopStarfishTotalEntity
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Title : 资金接口
 * Author: Lsy
 * Date: 2023/3/17 16:35
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface CapitalService {

    /**
     * 在当前店铺内的海星数量接口
     */
    @POST("/tokenCoin/user/shopDetail")
    suspend fun requestShopStarfishTotal(@Body body: RequestBody): ResponseWrapper<ShopStarfishTotalEntity>

    /**
     * 余额接口
     */
    @POST("/balance/unWithdraw/info")
    suspend fun requestBalance(@Body body: RequestBody): ResponseWrapper<BalanceEntity>

    /**
     * 我的海星余额接口
     */
    @GET("/tokenCoin/user/amountTotal")
    suspend fun requestStarfishTotal(): ResponseWrapper<String>

    /**
     * 我的优惠券数量接口
     */
    @GET("/coupon/myCouponCount")
    suspend fun requestCouponTotal(): ResponseWrapper<String>
}
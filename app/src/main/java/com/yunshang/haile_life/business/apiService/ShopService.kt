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
interface ShopService {

    @POST("/shop/nearShop")
    suspend fun requestNearStores(@Body body: RequestBody): ResponseWrapper<ResponseList<NearStoreEntity>>

    @POST("/position/nearPosition")
    suspend fun requestNearStorePositions(@Body body: RequestBody): ResponseWrapper<ResponseList<NearStorePositionEntity>>

    @GET("/shop/shopDevice")
    suspend fun requestShopDevice(@Query("id") id: Int): ResponseWrapper<MutableList<StoreDeviceEntity>>

    @GET("/position/positionDevice")
    suspend fun requestShopPositionDevice(@Query("id") id: Int): ResponseWrapper<MutableList<StoreDeviceEntity>>

    @GET("/shop/shopDetail")
    suspend fun requestShopDetail(@Query("id") id: Int): ResponseWrapper<ShopDetailEntity>

    @GET("/position/positionDetail")
    suspend fun requestShopPositionDetail(@QueryMap params: HashMap<String, Any>): ResponseWrapper<ShopPositionDetailEntity>

    @POST("/tokenCoin/shop/goods")
    suspend fun requestShopRechargeList(@Body body: RequestBody): ResponseWrapper<ShopStarfishListEntity>

    @POST("/tokenCoin/user/list")
    suspend fun requestRechargeShopList(@Body body: RequestBody): ResponseWrapper<RechargeShopListEntity>

    @POST("/tokenCoin/user/logList")
    suspend fun requestRechargeDetailList(@Body body: RequestBody): ResponseWrapper<ResponseList<RechargeStarfishDetailEntity>>

    @POST("/appointment/goodsCategory/list")
    suspend fun requestAppointCategoryList(@Body body: RequestBody): ResponseWrapper<AppointCategoryListEntity>

    @POST("/appointment/spec/list")
    suspend fun requestAppointSpecList(@Body body: RequestBody): ResponseWrapper<AppointSpecListEntity>

    @POST("/appointment/item/list")
    suspend fun requestAppointDeviceList(@Body body: RequestBody): ResponseWrapper<AppointDeviceListEntity>

    @POST("/starfish/refundList")
    suspend fun requestStarfishRefundList(@Body body: RequestBody): ResponseWrapper<ResponseList<StarfishRefundRecordEntity>>

    @POST("/starfish/applyRefundList")
    suspend fun requestStarfishRefundApplyList(@Body body: RequestBody): ResponseWrapper<MutableList<StarfishRefundApplyEntity>>

    @POST("/starfish/applyRefund")
    suspend fun submitRefundApply(@Body body: RequestBody): ResponseWrapper<Any>

    @GET("/starfish/refundDetail")
    suspend fun requestRefundDetail(@Query("id") id: Int): ResponseWrapper<StarfishRefundDetailEntity>

    @POST("/shopConfig/list")
    suspend fun requestShopConfigList(@Body body: RequestBody): ResponseWrapper<MutableList<ShopConfigEntity>>

    @POST("/notice/getNoticeByShopId")
    suspend fun requestShopNotice(@Body body: RequestBody): ResponseWrapper<MutableList<ShopNoticeEntity>>

    @POST("/position/deviceDetailPage")
    suspend fun requestPositionDeviceList(@Body body: RequestBody): ResponseWrapper<ResponseList<ShopPositionDeviceEntity>>

    @GET("/position/floorCodeList")
    suspend fun requestPositionDeviceFloorList(@Query("positionId") positionId: Int): ResponseWrapper<MutableList<String>>

    @POST("/activity/queryAndExecute")
    suspend fun requestShopActivity(@Body body: RequestBody): ResponseWrapper<ShopActivityEntity>

    @POST("/activity/queryAndExecute")
    suspend fun executeShopActivity(@Body body: RequestBody): ResponseWrapper<Any>
}
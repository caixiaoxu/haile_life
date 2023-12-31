package com.yunshang.haile_life.business.apiService

import com.lsy.framelib.network.response.ResponseWrapper
import com.yunshang.haile_life.data.entities.AppVersionEntity
import okhttp3.MultipartBody
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
interface CommonService {
    @Multipart
    @POST("/common/upload")
    suspend fun updateLoadFile(@Part file: MultipartBody.Part): ResponseWrapper<String>


    @GET("/common/appVersion")
    suspend fun appVersion(
        @Query("currentVersion") currentVersion: String,
        @Query("appType") appType: Int = 2
    ): ResponseWrapper<AppVersionEntity>
}
package com.yunshang.haile_life.business.apiService

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

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
interface DownloadService {

    @Streaming
    @GET
    suspend fun downLoadFile(@Url fileUrl: String): ResponseBody
}
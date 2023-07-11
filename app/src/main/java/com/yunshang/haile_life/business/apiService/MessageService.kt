package com.yunshang.haile_life.business.apiService

import com.lsy.framelib.network.response.ResponseWrapper
import com.yunshang.haile_life.data.entities.MessageEntity
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
interface MessageService {

    @POST("/message/index")
    suspend fun requestHomeMessage(@Body body: RequestBody): ResponseWrapper<MutableList<MessageEntity>>
}
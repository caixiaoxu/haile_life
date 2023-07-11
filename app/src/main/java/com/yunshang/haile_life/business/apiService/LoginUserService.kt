package com.yunshang.haile_life.business.apiService

import com.lsy.framelib.network.response.ResponseWrapper
import com.yunshang.haile_life.data.entities.BindPhoneEntity
import com.yunshang.haile_life.data.entities.LoginEntity
import com.yunshang.haile_life.data.entities.ThirdLoginEntity
import com.yunshang.haile_life.data.entities.UserInfoEntity
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Title : 登录和用户信息接口
 * Author: Lsy
 * Date: 2023/3/17 16:35
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
interface LoginUserService {

    /**
     * 登录接口
     */
    @POST("/login/login")
    suspend fun login(@Body body: RequestBody): ResponseWrapper<LoginEntity>

    /**
     * 验证码接口
     */
    @POST("/login/getCode")
    suspend fun sendCode(@Body body: RequestBody): ResponseWrapper<String>

    /**
     * 忘记密码接口
     */
    @POST("/login/updatePasswordByCode")
    suspend fun forgetPassword(@Body body: RequestBody): ResponseWrapper<Boolean>

    /**
     * 修改密码接口
     */
    @POST("/user/updatePassword")
    suspend fun updatePassword(@Body body: RequestBody): ResponseWrapper<Boolean>

    /**
     * 用户信息接口
     */
    @GET("/account/getAccountInfo")
    suspend fun userInfo(): ResponseWrapper<UserInfoEntity>

    /**
     * 检验Token是否有效接口
     */
    @POST("/login/checkToken")
    suspend fun checkToken(@Body body: RequestBody): ResponseWrapper<LoginEntity>

    /**
     * 登出接口
     */
    @POST("/login/logout")
    suspend fun logout(@Body body: RequestBody): ResponseWrapper<Any>

    /**
     * 获取支付宝登录参数接口
     */
    @GET("/login/authorizationUrl")
    suspend fun requestAlipayAuthParams(): ResponseWrapper<String>

    /**
     * 绑定手机号接口
     */
    @POST("/login/bindAccountForApp")
    suspend fun bindAccountForPhone(@Body body: RequestBody): ResponseWrapper<BindPhoneEntity>

    /**
     * 三方登录接口
     */
    @POST("/login/authorizationLogin")
    suspend fun thirdLogin(@Body body: RequestBody): ResponseWrapper<ThirdLoginEntity>
}
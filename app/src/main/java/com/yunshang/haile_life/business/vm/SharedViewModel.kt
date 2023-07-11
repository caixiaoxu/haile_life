package com.yunshang.haile_life.business.vm

import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.network.exception.CommonCustomException
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.yunshang.haile_life.business.apiService.LoginUserService
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.data.entities.LoginEntity
import com.yunshang.haile_life.data.entities.UserInfoEntity
import com.yunshang.haile_life.data.model.ApiRepository
import com.yunshang.haile_life.data.model.SPRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Title :
 * Author: Lsy
 * Date: 2023/3/16 15:40
 * Version:
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class SharedViewModel : ViewModel() {
    private val mRepo = ApiRepository.apiClient(LoginUserService::class.java)

    val loginInfo: MutableLiveData<LoginEntity> = MutableLiveData()
    val userInfo: MutableLiveData<UserInfoEntity?> = MutableLiveData(SPRepository.userInfo)

    /**
     * 密码登录
     */
    suspend fun login(
        phone: String?,
        code: String?,
        loginType: Int
    ) {
        val params = mutableMapOf<String, Any>()
        if (!phone.isNullOrEmpty()) {
            params["account"] = phone
        }
        if (!code.isNullOrEmpty()) {
            params["verificationCode"] = code
        }

        params["loginType"] = loginType
        loginRequest(params)
    }

    /**
     * 登录请求
     */
    suspend fun loginRequest(
        params: MutableMap<String, Any>,
        isCheckToken: Boolean = false,
    ) {
        // 公共参数
        params["authorizationClientType"] = 4
        //区分是否是检验token接口
        val loginData = if (isCheckToken) {
            ApiRepository.dealApiResult(mRepo.checkToken(ApiRepository.createRequestBody(params)))
        } else {
            ApiRepository.dealApiResult(mRepo.login(ApiRepository.createRequestBody(params)))
        }
        Timber.d("登录接口请求成功$loginData")
        loginData?.let {
            SPRepository.loginInfo = it
            loginInfo.postValue(it)
        } ?: throw CommonCustomException(-1, "返回数据为空")
        requestUserInfo()
    }

    /**
     * 三方登录
     */
    suspend fun thirdLogin(type: Int, authCode: String?, callBack: (code: String) -> Unit) {
        ApiRepository.dealApiResult(
            mRepo.thirdLogin(
                ApiRepository.createRequestBody(
                    hashMapOf(
                        "authorizationLoginType" to type,
                        "authorizationCode" to authCode,
                        "authorizationClientType" to 9,
                    )
                )
            )
        )?.let {
            if (it.code.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    callBack(it.code)
                }
            } else if (it.token.isNotEmpty()) {
                initLoginInfo(it.organizationId, it.organizationType, it.token, it.userId)
                SPRepository.userInfo = it.accountInfoDto
                userInfo.postValue(it.accountInfoDto)
                LiveDataBus.post(BusEvents.LOGIN_STATUS, true)
            }
        } ?: throw CommonCustomException(-1, "返回数据为空")
    }

    fun initLoginInfo(organizationId: Int, organizationType: Int, token: String, userId: Int) {
        LoginEntity(organizationId, organizationType, token, userId).run {
            SPRepository.loginInfo = this
            loginInfo.postValue(this)
        }
    }

    /**
     * 切换角色
     */
    fun swapUserInfo() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                requestUserInfo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 请求用户信息
     */
    fun requestUserInfoAsync() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                requestUserInfo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 请求用户信息
     */
    suspend fun requestUserInfo() {
        val userInfoData = ApiRepository.dealApiResult(mRepo.userInfo())
        Timber.d("用户信息请求成功$userInfoData")
        userInfoData?.let {
            SPRepository.userInfo = it
            userInfo.postValue(it)
        } ?: throw CommonCustomException(-1, "返回数据为空")
    }

    val mSharedLocation: MutableLiveData<Location> by lazy {
        MutableLiveData()
    }

    fun requestLocationInfo(context: Context) {
        TencentLocationManager.getInstance(context)
            .requestSingleFreshLocation(null, object : TencentLocationListener {
                override fun onLocationChanged(
                    location: TencentLocation?,
                    code: Int,
                    message: String?
                ) {
                    Timber.i("定位信息:$location,错误码:$code,错误描述:$message")
                    if (0 == code && null != location) {
                        mSharedLocation.value = Location("").apply {
                            latitude = location.latitude
                            longitude = location.longitude
                        }
                    }
                }

                override fun onStatusUpdate(p0: String?, p1: Int, p2: String?) {
                }

            }, Looper.getMainLooper())
    }
}
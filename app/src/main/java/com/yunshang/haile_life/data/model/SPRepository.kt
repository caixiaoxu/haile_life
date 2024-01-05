package com.yunshang.haile_life.data.model

import com.lsy.framelib.utils.SPUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.data.entities.LoginEntity
import com.yunshang.haile_life.data.entities.UserInfoEntity

/**
 * Title :
 * Author: Lsy
 * Date: 2023/3/31 16:42
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object SPRepository {
    private const val SP_NAME: String = "default_name"

    private const val SP_SELECT_ENV: String = "select_env"

    private const val SP_SCAN_ORDER_CONFIG_NO_PROMPT = "sp_scan_order_config_no_prompt"
//    private const val SP_SCAN_APPOINT_ORDER_CONFIG_NO_PROMPT = "sp_scan_appoint_order_config_no_prompt"

    private const val SP_AGREE_AGREEMENT = "sp_agree_agreement"

    private const val SP_LOGIN_TOKEN = "sp_login_token"

    private const val SP_USER_TOKEN = "sp_user_token"

    private const val SP_CHECK_UPDATE_TIME = "sp_check_update_time"

    private val sp: SPUtils by lazy { SPUtils.getInstance(SP_NAME) }

    /**
     * 选择的环境
     */
    var selectEnv: String?
        get() = sp.getString(SP_SELECT_ENV)
        set(value) = sp.put(SP_SELECT_ENV, value)

    /**
     * 判断是否同意了隐私协议
     */
    var isAgreeAgreement: Boolean
        get() = sp.getBoolean(SP_AGREE_AGREEMENT, false)
        set(value) = sp.put(SP_AGREE_AGREEMENT, value)

    /**
     * 判断是否不再显示下单温馨提示
     */
    var isNoPrompt: Boolean
        get() = sp.getBoolean(SP_SCAN_ORDER_CONFIG_NO_PROMPT, false)
        set(value) = sp.put(SP_SCAN_ORDER_CONFIG_NO_PROMPT, value)
//
//    /**
//     * 判断是否不再显示预约下单温馨提示
//     */
//    var isNoAppointPrompt: Boolean
//        get() = sp.getBoolean(SP_SCAN_APPOINT_ORDER_CONFIG_NO_PROMPT, false)
//        set(value) = sp.put(SP_SCAN_APPOINT_ORDER_CONFIG_NO_PROMPT, value)

    /**
     * 判断检测更新时间
     */
    var checkUpdateTime: Long
        get() = sp.getLong(SP_CHECK_UPDATE_TIME, 0L)
        set(value) = sp.put(SP_CHECK_UPDATE_TIME, value)

    /**
     * 判断是否已登录
     */
    fun isLogin(): Boolean = null != loginInfo

    /**
     * 获取登录Token
     */
    var loginInfo: LoginEntity? = null
        get() {
            return field ?: GsonUtils.json2Class(
                sp.getString(SP_LOGIN_TOKEN),
                LoginEntity::class.java
            )
        }
        set(value) {
            if (null == value) {//清空缓存
                sp.put(SP_LOGIN_TOKEN, "")
            } else {//重新设置缓存
                sp.put(SP_LOGIN_TOKEN, GsonUtils.any2Json(value))
            }
            //清空用户信息
            userInfo = null
            field = value
        }

    /**
     * 获取用户Token
     */
    var userInfo: UserInfoEntity? = null
        get() {
            return field ?: GsonUtils.json2Class(
                sp.getString(SP_USER_TOKEN),
                UserInfoEntity::class.java
            )
        }
        set(value) {
            if (null == value) {//清空缓存
                sp.put(SP_USER_TOKEN, "")
            } else {//重新设置缓存
                sp.put(SP_USER_TOKEN, GsonUtils.any2Json(value))
            }
            field = value
        }

    /**
     * 清空登录和用户信息
     */
    fun cleaLoginUserInfo() {
        loginInfo = null
    }
}
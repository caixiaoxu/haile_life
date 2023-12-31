package com.yunshang.haile_life.utils.thrid

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.alipay.sdk.app.AuthTask
import com.alipay.sdk.app.PayTask
import com.yunshang.haile_life.data.entities.AliPayAuthResultEntity
import com.yunshang.haile_life.data.entities.AliPayResultEntity
import timber.log.Timber


/**
 * Title : 支付宝操作
 * Author: Lsy
 * Date: 2023/7/5 14:51
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object AlipayHelper {

    /**
     * 检测是否安装了支付宝
     */
    fun isAlipayInstalled(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("alipays://platformapi/startApp"))
        return intent.resolveActivity(context.packageManager) != null
    }

    /**
     * 请求登录
     */
    fun requestAuth(activity: Activity, authInfo: String): AliPayAuthResultEntity {
        val result = AuthTask(activity).authV2(authInfo, true)
        Timber.i(result.toString())
        return AliPayAuthResultEntity(result)
    }

    /**
     * 请求支付
     */
    fun requestPay(
        activity: Activity,
        params: String
    ): AliPayResultEntity {
        val result = PayTask(activity).payV2(params, true)
        Timber.i(result.toString())
        return AliPayResultEntity(result)
    }
}
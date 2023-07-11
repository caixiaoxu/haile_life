package com.yunshang.haile_life.utils

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.yunshang.haile_life.data.model.SPRepository

/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/12 16:09
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object ActivityManagerUtils {

    /**
     * 获取activity回调发射器
     */
    fun getActivityResultLauncher(
        activity: ComponentActivity,
        callback: (rCode: Int, rData: Intent?) -> Unit
    ) = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        callback(it.resultCode, it.data)
    }


    /**
     * 清空登录信息，跳转到登录界面
     */
    fun clearLoginInfoGoLogin() {
        // 清空登录信息
        SPRepository.cleaLoginUserInfo()
        // TODO 跳转登录界面
//        Constants.APP_CONTEXT.startActivity(
//            Intent(
//                Constants.APP_CONTEXT,
//                LoginActivity::class.java
//            ).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }
//        )
    }
}
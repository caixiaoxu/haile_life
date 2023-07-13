package com.yunshang.haile_life.utils.scheme

import android.os.Bundle
import com.yunshang.haile_life.data.agruments.IntentParams

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/13 17:54
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class NearByShopParser : ISchemeURLParser {

    override fun parsePath(): String = "com.yunshang.haile_life.ui.activity.shop.NearByShopActivity"
    override fun parseParams(params: String): Bundle {
        val bundle = Bundle()
        // 解析IsRechargeShop参数并
        SchemeURLHelper.parseParam(params, IntentParams.NearByShopParams.IsRechargeShop)?.let {
            try {
                bundle.putAll(IntentParams.NearByShopParams.pack(it.toBoolean()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return bundle
    }
}
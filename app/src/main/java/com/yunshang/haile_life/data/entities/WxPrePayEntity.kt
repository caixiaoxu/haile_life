package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/2 13:44
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
</desc></version></time></author> */
data class WxPrePayEntity(
    val timeStamp: String,
    val parentId: String,
    val prepayId: String,
    val paySign: String,
    val appId: String,
    val signType: String,
    val nonceStr: String,
)
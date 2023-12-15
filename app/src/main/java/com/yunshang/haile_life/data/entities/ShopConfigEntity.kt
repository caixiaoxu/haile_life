package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/8/8 14:33
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopConfigEntity(
    val configType: Int, //类型, 1--强制使用海星, 2--强制免密
    val result: Boolean, //命中结果。false未命中（可以继续），true命中（不能继续执行
    val closable: Boolean,//是否可关闭, true--可关闭, false--不可
    val tokenCoinForceUse: Int //类型, 1--开启 0--关闭
)
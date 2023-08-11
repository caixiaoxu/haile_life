package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/9 14:34
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class DeviceDetailEntity(
    val amount: Int,
    val brandId: Int,
    val categoryCode: String,
    val categoryId: Int,
    val chargeUnit: Int,
    val code: String,
    val createTime: String,
    val creatorId: Int,
    val creatorName: String,
    val deleteFlag: Int,
    val deviceErrorCode: Int,
    val deviceErrorMsg: String,
    val extAttr: String,
    val feature: String,
    val id: Int,
    val inventoryType: Int,
    val items: List<DeviceDetailItemEntity>,
    val lastEditor: Int,
    val mainPic: String,
    val mainVideo: String,
    val name: String,
    val payType: String,
    val price: String,
    val shopCategoryId: Int,
    val shopClosed: Boolean,
    val shopId: Int,
    val soldState: Int,
    val soldStateOp: Int,
    val spuId: Int,
    val tags: String,
    val type: Int,
    val updateTime: String,
    val version: Int,
    val hasAttachGoods:Boolean,
    val attachGoodsId:Int,
    val attachItems:List<DeviceDetailItemEntity>,
    val isShowDispenser:Boolean,
    val hideDispenserTips:String,
) {
    val drinkingOverTime: String
        get() = items.firstOrNull()?.getDrinkingExtAttr()?.overTime ?: ""
    val drinkingPauseTime: String
        get() = items.firstOrNull()?.getDrinkingExtAttr()?.pauseTime ?: ""
}




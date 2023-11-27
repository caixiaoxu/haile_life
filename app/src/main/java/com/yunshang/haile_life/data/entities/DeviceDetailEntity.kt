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
    val categoryName: String,
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
    val shopName: String,
    val imei: String,
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
//    val hasAttachGoods: Boolean,
    val attachGoodsId: Int,
//    val attachItems: List<DeviceDetailItemEntity>?,
//    val isShowDispenser: Boolean,
//    val hideDispenserTips: String,
    val advancedSettingVOS: List<AdvancedSettingVOS>,
    val positionId: Int? = null,
    val positionName: String? = null,
    val spuCode: String,
    val reserveState: Int? = null,//设备状态, 0--不可预约, 1--可预约
    val deviceState: Int? = null,// 设备状态 1:空闲；2占用, 3故障
    val enableReserve: Boolean? = null,//是否支持预约, false--不支持, true--支持
    val reserveMethod: Int? = null, //预约方式, 1--后付费预约, 1--先付费预约
    val attachValueMap: Map<String, AttachValueMapValue>? = null
) {
    companion object {
        val Dispenser: String = "Dispenser"
        val SelfClean: String = "SelfClean"
    }

    val shopPositionName: String
        get() = shopName + if (positionName.isNullOrEmpty()) "" else ("-$positionName")

    fun drinkingOverTime() =
        advancedSettingVOS.find { item -> item.modelFunctionCode == "030D" }?.settingValue?.firstOrNull()

    fun showDrinkingOverTime() = !drinkingOverTime().isNullOrEmpty()
    fun drinkingPauseTime() =
        advancedSettingVOS.find { item -> item.modelFunctionCode == "0303" }?.settingValue?.firstOrNull()

    fun showDrinkingPauseTime() = !drinkingPauseTime().isNullOrEmpty()

    val dispenserValue: AttachValueMapValue?
        get() = attachValueMap?.get(Dispenser)

    val hasAttachGoods: Boolean
        get() = dispenserValue?.isOn ?: false

    val attachItems: List<DeviceDetailItemEntity>?
        get() = dispenserValue?.items

    val selfCleanValue: AttachValueMapValue?
        get() = attachValueMap?.get(SelfClean)

    fun hasSelfClean() = selfCleanValue?.isOn ?: false

}

data class AttachValueMapValue(
    val isOn: Boolean? = null,
    val tipMessage: String? = null,
    val selfCleanGoodsId: Int? = null,
    val selfCleanItemId: Int? = null,
    val price: String? = null,
    val items: List<DeviceDetailItemEntity>? = null,
)

data class AdvancedSettingVOS(
    val modelFunctionCode: String,
    val settingValue: List<String>
)




package com.yunshang.haile_life.data.entities

/**
 * Title :
 * Author: Lsy
 * Date: 2024/1/18 14:55
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
data class ShopActivityEntity(
    val activityExecuteNodeId: Int? = 0,
    val activityId: Int? = 0,
    val activitySubTypeId: Int? = 0,
    val couponDTOS: List<CouponDTOS>? = listOf(),
    val status: Int? = 0,
    val structureId: Int? = 0,
    val subStructureId: Int? = 0
)

data class CouponDTOS(
    val assetIds: List<Int>? = listOf(),
    val count: Int? = 0,
    val couponDTO: CouponDTO? = CouponDTO(),
    val promotionId: Int? = 0,
    val promotionProductId: Int? = 0,
    val promotionProductSubId: Int? = 0
)

data class CouponDTO(
    val id: Int? = 0,
    val productId: Int? = 0,
    val productSubId: Int? = 0,
    val title: String? = "",
    val createTime: String? = "",
    val activation: Activation? = Activation(),
    val couponType: Int? = 0,
    val deleteFlag: Int? = 0,
    val feeShareBenefit: FeeShareBenefit? = FeeShareBenefit(),
    val hourMinute: HourMinute? = HourMinute(),
    val limitedChannelConditionDTO: LimitedChannelConditionDTO? = LimitedChannelConditionDTO(),
    val machineParentType: MachineParentType? = MachineParentType(),
    val operatorCondition: OperatorCondition? = OperatorCondition(),
    val orderFree: OrderFree? = OrderFree(),
    val orderItemFree: OrderItemFree? = OrderItemFree(),
    val orderItemReduce: OrderItemReduce? = OrderItemReduce(),
    val orderPercentageDiscount: OrderPercentageDiscount? = OrderPercentageDiscount(),
    val orderReachPrice: OrderReachPrice? = OrderReachPrice(),
    val orderReduce: OrderReduce? = OrderReduce(),
    val orderSpecifiedPriceBenefitDTO: OrderSpecifiedPriceBenefitDTO? = OrderSpecifiedPriceBenefitDTO(),
    val organization: Organization? = Organization(),
    val organizationBlock: OrganizationBlock? = OrganizationBlock(),
    val organizationTypeCondition: OrganizationTypeCondition? = OrganizationTypeCondition(),
    val validityDate: ValidityDate? = ValidityDate(),
    val validityDayOfWeek: ValidityDayOfWeek? = ValidityDayOfWeek()
)

data class Activation(
    val activateNum: Int? = null,
    val activateStartDay: Int? = null,
    val activateTime: Int? = null,
    val activateTimeType: Int? = null,
    val asset: Asset? = null,
    val isCalculateTimeFromActivation: Boolean? = null,
    val stock: Int? = null,
    val unlimitedStock: Boolean? = null,
    val unlimitedUserActivation: Boolean? = null,
    val userActivationLimit: Int? = null
)

data class FeeShareBenefit(
    val feeShareDetails: List<FeeShareDetail?>? = null,
    val feeShareType: Int? = null
)

data class HourMinute(
    val hourMinutes: List<HourMinuteX>? = listOf()
)

data class LimitedChannelConditionDTO(
    val limitedChannels: List<Any?>? = null
)

data class MachineParentType(
    val machineParentTypeIds: List<Any?>? = null
)

data class OperatorCondition(
    val operatorIds: List<Any?>? = null
)

data class OrderFree(
    val isFree: Boolean? = null,
    val needPay: Int? = null
)

data class OrderItemFree(
    val isFree: Boolean? = null,
    val needPay: Int? = null
)

data class OrderItemReduce(
    val reduce: Int? = null
)

data class OrderPercentageDiscount(
    val maxDiscountPrice: Int? = null,
    val percentage: Int? = null
)

data class OrderReachPrice(
    val orderReachPrice: Int? = null
)

data class OrderReduce(
    val reduce: Int? = null
)

data class OrderSpecifiedPriceBenefitDTO(
    val specifiedPrice: Int? = null
)

data class Organization(
    val orgIds: List<Any?>? = null
)

data class OrganizationBlock(
    val orgIds: List<Any?>? = null
)

data class OrganizationTypeCondition(
    val organizationType: Int? = null
)

data class ValidityDate(
    val endAt: String? = null,
    val startAt: String? = null
)

data class ValidityDayOfWeek(
    val dayOfWeek: List<Any?>? = null
)

data class Asset(
    val dailyUsageLimit: Int? = null,
    val hasDailyUsageLimit: Boolean? = null,
    val hasPerWeekUsageLimit: Boolean? = null,
    val perWeekUsageLimit: Int? = null,
    val stock: Int? = null,
    val unlimitedStock: Boolean? = null
)

data class HourMinuteX(
    val endTime: Int? = null,
    val startTime: Int? = null
)
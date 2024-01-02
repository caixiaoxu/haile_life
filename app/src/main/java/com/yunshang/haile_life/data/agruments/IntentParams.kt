package com.yunshang.haile_life.data.agruments

import android.content.Intent
import android.os.Bundle
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.data.entities.*

/**
 * Title :
 * Author: Lsy
 * Date: 2023/6/9 11:31
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
object IntentParams {

    object DefaultPageParams {
        private const val DefaultPage = "defaultPage"

        /**
         * 包装参数
         */
        fun pack(defaultPage: Int): Bundle =
            Bundle().apply {
                putInt(DefaultPage, defaultPage)
            }

        fun parseDefaultPage(intent: Intent): Int = intent.getIntExtra(DefaultPage, 0)
    }

    object IdParams {
        private const val Id = "id"

        /**
         * 包装参数
         */
        fun pack(id: Int): Bundle = Bundle().apply {
            putInt(Id, id)
        }

        /**
         * 解析id
         */
        fun parseId(intent: Intent): Int = intent.getIntExtra(Id, -1)
    }

    object PicParams {
        private const val Url = "url"

        /**
         * 包装参数
         */
        fun pack(url: String): Bundle = Bundle().apply {
            putString(Url, url)
        }

        /**
         * 解析Url
         */
        fun parseUrl(intent: Intent): String? = intent.getStringExtra(Url)
    }

    object BindPhoneParams {
        private const val Code = "code"
        private const val LoginType = "loginType"

        /**
         * 包装参数
         */
        fun pack(code: String, loginType: Int): Bundle = Bundle().apply {
            putString(Code, code)
            putInt(LoginType, loginType)
        }

        /**
         * 解析code
         */
        fun parseCode(intent: Intent): String? = intent.getStringExtra(Code)

        /**
         * 解析code
         */
        fun parseLoginType(intent: Intent): Int = intent.getIntExtra(LoginType, -1)
    }

    object ScanOrderParams {
        private const val CODE = "Code"
        private const val GoodsScan = "GoodsScan"
        private const val DeviceDetail = "DeviceDetail"

        /**
         * 包装参数
         */
        fun pack(
            payCode: String,
            goodsScan: GoodsScanEntity? = null,
            deviceDetail: DeviceDetailEntity? = null
        ): Bundle = Bundle().apply {
            putString(CODE, payCode)
            goodsScan?.let {
                putString(GoodsScan, GsonUtils.any2Json(goodsScan))
            }
            deviceDetail?.let {
                putString(DeviceDetail, GsonUtils.any2Json(deviceDetail))
            }
        }

        /**
         * 解析code
         */
        fun parseCode(intent: Intent): String? = intent.getStringExtra(CODE)

        fun parseGoodsScan(intent: Intent): GoodsScanEntity? =
            GsonUtils.json2Class(intent.getStringExtra(GoodsScan), GoodsScanEntity::class.java)

        fun parseDeviceDetail(intent: Intent): DeviceDetailEntity? =
            GsonUtils.json2Class(
                intent.getStringExtra(DeviceDetail),
                DeviceDetailEntity::class.java
            )
    }

    object OrderSubmitParams {
        private const val Goods = "goods"
        private const val ReserveTime = "reserveTime"
        private const val DeviceName = "deviceName"
        private const val ShopAddress = "shopAddress"
        private const val IsAppoint = "isAppoint"

        /**
         * 包装参数
         */
        fun pack(
            goods: List<OrderSubmitGood>,
            reserveTime: String? = null,
            deviceName: String? = null,
            shopAddress: String? = null,
            isAppoint: Boolean? = null,
        ): Bundle =
            Bundle().apply {
                putString(Goods, GsonUtils.any2Json(goods))
                reserveTime?.let {
                    putString(ReserveTime, reserveTime)
                }
                deviceName?.let {
                    putString(DeviceName, deviceName)
                }
                shopAddress?.let {
                    putString(ShopAddress, shopAddress)
                }
                isAppoint?.let {
                    putBoolean(IsAppoint, isAppoint)
                }
            }

        /**
         * 解析code
         */
        fun parseGoods(intent: Intent): MutableList<OrderSubmitGood>? =
            GsonUtils.json2List(intent.getStringExtra(Goods), OrderSubmitGood::class.java)

        fun parseReserveTime(intent: Intent): String? = intent.getStringExtra(ReserveTime)
        fun parseDeviceName(intent: Intent): String? = intent.getStringExtra(DeviceName)
        fun parseShopAddress(intent: Intent): String? = intent.getStringExtra(ShopAddress)
        fun parseIsAppoint(intent: Intent): Boolean = intent.getBooleanExtra(IsAppoint, false)

        data class OrderSubmitGood(
            val categoryCode: String,
            val goodId: Int,
            val goodItmId: Int,
            val num: String,
        )
    }

    object ShopParams {
        private const val ShopId = "shopId"

        /**
         * 包装参数
         */
        fun pack(shopId: Int): Bundle =
            Bundle().apply {
                putInt(ShopId, shopId)
            }

        /**
         * 解析ShopId
         */
        fun parseShopId(intent: Intent): Int = intent.getIntExtra(ShopId, -1)
    }

    object ShopWorkTimeParams {
        private const val WorkTimeArr = "workTimeArr"

        /**
         * 包装参数
         */
        fun pack(workTimeArr: MutableList<BusinessHourEntity>): Bundle =
            Bundle().apply {
                putString(WorkTimeArr, GsonUtils.any2Json(workTimeArr))
            }

        /**
         * 解析WorkTimeArr
         */
        fun parseWorkTimeArr(intent: Intent): List<BusinessHourEntity>? =
            GsonUtils.json2List(intent.getStringExtra(WorkTimeArr), BusinessHourEntity::class.java)
    }

    object SelectAppointDeviceParams {
        private const val ShopId = "shopId"
        private const val SpecValueId = "specValueId"
        private const val Unit = "unit"
        private const val CategoryName = "categoryName"

        private const val SelectDevice = "selectDevice"

        /**
         * 包装参数
         */
        fun pack(shopId: Int, specValueId: Int, unit: Int?, categoryName: String?): Bundle =
            Bundle().apply {
                putInt(ShopId, shopId)
                putInt(SpecValueId, specValueId)
                unit?.let {
                    putInt(Unit, unit)
                }
                putString(CategoryName, categoryName)
            }

        /**
         * 解析ShopId
         */
        fun parseShopId(intent: Intent): Int = intent.getIntExtra(ShopId, -1)

        /**
         * 解析SpecValueId
         */
        fun parseSpecValueId(intent: Intent): Int = intent.getIntExtra(SpecValueId, -1)

        /**
         * 解析时长
         */
        fun parseUnit(intent: Intent): Int? {
            val unit = intent.getIntExtra(Unit, 0)
            return if (0 == unit) null else unit
        }

        /**
         * 解析分类名称
         */
        fun parseCategoryName(intent: Intent): String? = intent.getStringExtra(CategoryName)

        fun packResult(selectDevice: AppointDevice): Bundle =
            Bundle().apply {
                putString(SelectDevice, GsonUtils.any2Json(selectDevice))
            }

        fun parseSelectDevice(intent: Intent): AppointDevice? = GsonUtils.json2Class(
            intent.getStringExtra(
                SelectDevice
            ), AppointDevice::class.java
        )
    }

    object NearByShopParams {
        const val IsRechargeShop = "isRechargeShop"

        /**
         * 包装参数
         */
        fun pack(isRechargeShop: Boolean = false): Bundle =
            Bundle().apply {
                putBoolean(IsRechargeShop, isRechargeShop)
            }

        fun parseIsRechargeShop(intent: Intent): Boolean =
            intent.getBooleanExtra(IsRechargeShop, false)
    }

    object OrderListParams {
        private const val IsMain = "isMain"
        private const val Status = "status"

        /**
         * 包装参数
         */
        fun pack(isMain: Boolean = false, status: Int? = null): Bundle =
            Bundle().apply {
                putBoolean(IsMain, isMain)
                status?.let {
                    putInt(Status, status)
                }
            }

        fun parseIsMain(bundle: Bundle?): Boolean = bundle?.getBoolean(IsMain, false) ?: false
        fun parseStatus(bundle: Bundle?): Int? = bundle?.getInt(Status, -1).let {
            if (-1 == it) null else it
        }
    }

    object DeviceParams {
        private const val DeviceId = "deviceId"
        const val CategoryCode = "categoryCode"

        /**
         * 包装参数
         */
        fun pack(categoryCode: String? = null, deviceId: Int? = null): Bundle =
            Bundle().apply {
                categoryCode?.let {
                    putString(CategoryCode, categoryCode)
                }
                deviceId?.let {
                    putInt(DeviceId, deviceId)
                }
            }

        fun parseCategoryCode(intent: Intent): String? = intent.getStringExtra(CategoryCode)

        fun parseDeviceId(intent: Intent): Int = intent.getIntExtra(DeviceId, -1)
    }

    object DiscountCouponSelectorParams {
        private const val SelectCoupon = "SelectCoupon"
        private const val PromotionProduct = "promotionProduct"
        private const val OtherSelectCoupon = "OtherSelectCoupon"

        /**
         * 包装参数
         */
        fun pack(
            select: List<TradePreviewParticipate>? = null,
            promotionProduct: Int,
            otherSelect: List<TradePreviewParticipate>? = null,
        ): Bundle =
            Bundle().apply {
                select?.let {
                    putString(SelectCoupon, GsonUtils.any2Json(select))
                }
                putInt(PromotionProduct, promotionProduct)
                otherSelect?.let {
                    putString(OtherSelectCoupon, GsonUtils.any2Json(otherSelect))
                }
            }

        fun parseSelectCoupon(intent: Intent): MutableList<TradePreviewParticipate>? =
            GsonUtils.json2List(
                intent.getStringExtra(SelectCoupon),
                TradePreviewParticipate::class.java
            )

        fun parsePromotionProduct(intent: Intent): Int = intent.getIntExtra(PromotionProduct, -1)


        fun parseOtherSelectCoupon(intent: Intent): MutableList<TradePreviewParticipate>? =
            GsonUtils.json2List(
                intent.getStringExtra(OtherSelectCoupon),
                TradePreviewParticipate::class.java
            )
    }

    object RechargeStarfishParams {
        private const val ShopId = "shopId"

        /**
         * 包装参数
         */
        fun pack(shopId: Int?): Bundle =
            Bundle().apply {
                shopId?.let {
                    putInt(ShopId, shopId)
                }
            }

        fun parseShopId(intent: Intent): Int = intent.getIntExtra(ShopId, -1)
    }

    object StarfishRefundParams {
        private const val ShopIdList = "shopIdList"
        private const val TotalAmount = "totalAmount"

        /**
         * 包装参数
         */
        fun pack(shopIdList: IntArray, totalAmount: Double): Bundle =
            Bundle().apply {
                putIntArray(ShopIdList, shopIdList)
                putDouble(TotalAmount, totalAmount)
            }

        fun parseShopIdList(intent: Intent): IntArray? = intent.getIntArrayExtra(ShopIdList)
        fun parseTotalAmount(intent: Intent): Double = intent.getDoubleExtra(TotalAmount, 0.0)
    }

    object StarfishRefundDetailParams {
        private const val RefundId = "refundId"

        /**
         * 包装参数
         */
        fun pack(refundId: Int): Bundle =
            Bundle().apply {
                putInt(RefundId, refundId)
            }

        fun parseRefundId(intent: Intent): Int = intent.getIntExtra(RefundId, -1)
    }

    object WalletBalanceParams {
        private const val Balance = "balance"

        /**
         * 包装参数
         */
        fun pack(balance: String): Bundle =
            Bundle().apply {
                putString(Balance, balance)
            }

        fun parseBalance(intent: Intent): String? = intent.getStringExtra(Balance)
    }

    object RechargeStarfishDetailListParams {
        private const val ShopId = "shopId"

        /**
         * 包装参数
         */
        fun pack(shopId: Int): Bundle =
            Bundle().apply {
                putInt(ShopId, shopId)
            }

        fun parseShopId(intent: Intent): Int = intent.getIntExtra(ShopId, -1)
    }

    object FaultRepairsParams {
        private const val GoodsInfo = "goodsInfo"

        /**
         * 包装参数
         */
        fun pack(goodsInfo: GoodsScanEntity? = null): Bundle =
            Bundle().apply {
                goodsInfo?.let {
                    putString(GoodsInfo, GsonUtils.any2Json(it))
                }
            }

        fun parseGoodsInfo(intent: Intent): GoodsScanEntity? =
            GsonUtils.json2Class(intent.getStringExtra(GoodsInfo), GoodsScanEntity::class.java)
    }

    object OrderParams {
        private const val OrderNo = "orderNo"
        private const val IsAppoint = "isAppoint"
        private const val FormScan = "formScan"

        /**
         * 包装参数
         */
        fun pack(orderNo: String, isAppoint: Boolean = false, formScan: Int = 0): Bundle =
            Bundle().apply {
                putString(OrderNo, orderNo)
                putBoolean(IsAppoint, isAppoint)
                putInt(FormScan, formScan)
            }

        fun parseOrderNo(intent: Intent): String? = intent.getStringExtra(OrderNo)

        fun parseIsAppoint(intent: Intent): Boolean = intent.getBooleanExtra(IsAppoint, false)
        fun parseFormScan(intent: Intent): Int = intent.getIntExtra(FormScan, 0)
    }

    object OrderPayParams {
        private const val TimeRemaining = "timeRemaining"
        private const val OrderItems = "orderItems"
        private const val Price = "price"

        /**
         * 包装参数
         */
        fun pack(
            orderNo: String,
            orderType: String,
            orderSubType: Int,
            timeRemaining: String,
            price: String,
            orderItems: List<OrderItem>? = null
        ): Bundle =
            Bundle().apply {
                putAll(
                    OrderParams.pack(
                        orderNo,
                        "300" == orderType || 106 == orderSubType
                    )
                )
                putString(TimeRemaining, timeRemaining)
                putString(Price, price)
                orderItems?.let {
                    putString(OrderItems, GsonUtils.any2Json(orderItems))
                }
            }

        fun parseOrderNo(intent: Intent): String? = OrderParams.parseOrderNo(intent)
        fun parseTimeRemaining(intent: Intent): String? = intent.getStringExtra(TimeRemaining)
        fun parsePrice(intent: Intent): String? = intent.getStringExtra(Price)
        fun parseOrderItems(intent: Intent): MutableList<OrderItem>? = GsonUtils.json2List(
            intent.getStringExtra(OrderItems), OrderItem::class.java
        )
    }

    object WebViewParams {
        private const val Url = "url"
        private const val NeedFilter = "needFilter"
        private const val Title = "title"
        private const val AutoWebTitle = "autoWebTitle"
        private const val NoCache = "noCache"

        /**
         * 包装参数
         */
        fun pack(
            url: String,
            needFilter: Boolean = false,
            title: String? = null,
            autoWebTitle: Boolean = true,
            noCache: Boolean = false,
        ): Bundle = Bundle().apply {
            putString(Url, url)
            putBoolean(NeedFilter, needFilter)
            title?.let {
                putString(Title, title)
            }
            putBoolean(AutoWebTitle, autoWebTitle)
            putBoolean(NoCache, noCache)
        }

        fun parseUrl(intent: Intent): String? = intent.getStringExtra(Url)
        fun parseNeedFilter(intent: Intent): Boolean = intent.getBooleanExtra(NeedFilter, false)
        fun parseTitle(intent: Intent): String? = intent.getStringExtra(Title)
        fun parseAutoWebTitle(intent: Intent): Boolean = intent.getBooleanExtra(AutoWebTitle, true)
        fun parseNoCache(intent: Intent): Boolean = intent.getBooleanExtra(NoCache, false)
    }


    object OrderIssueEvaluateParams {
        private const val OrderId = "orderId"
        private const val GoodId = "goodId"
        private const val BuyerId = "buyerId"
        private const val SellerId = "sellerId"
        private const val OrderNo = "orderNo"
        private const val OrderShopPhone = "orderShopPhone"

        /**
         * 包装参数
         */
        fun pack(
            orderId: Int?,
            orderNo: String?,
            goodId: Int?,
            buyerId: Int?,
            sellerId: Int?,
            orderShopPhone: String?
        ): Bundle =
            Bundle().apply {
                orderId?.let {
                    putInt(OrderId, orderId)
                }
                goodId?.let {
                    putInt(GoodId, goodId)
                }
                buyerId?.let {
                    putInt(BuyerId, buyerId)
                }
                sellerId?.let {
                    putInt(SellerId, sellerId)
                }
                orderNo?.let {
                    putString(OrderNo, orderNo)
                }
                orderShopPhone?.let {
                    putString(OrderShopPhone, orderNo)
                }
            }

        fun parseOrderId(intent: Intent): Int = intent.getIntExtra(OrderId, -1)
        fun parseGoodId(intent: Intent): Int = intent.getIntExtra(GoodId, -1)
        fun parseBuyerId(intent: Intent): Int = intent.getIntExtra(BuyerId, -1)
        fun parseSellerId(intent: Intent): Int = intent.getIntExtra(SellerId, -1)
        fun parseOrderNo(intent: Intent): String? = intent.getStringExtra(OrderNo)
        fun parseOrderShopPhone(intent: Intent): String? = intent.getStringExtra(OrderShopPhone)
    }

    object OrderEvaluateDetailsParams {
        private const val OrderId = "orderId"

        /**
         * 包装参数
         */
        fun pack(
            orderId: Int?,
        ): Bundle =
            Bundle().apply {
                orderId?.let {
                    putInt(OrderId, orderId)
                }
            }

        fun parseOrderId(intent: Intent): Int = intent.getIntExtra(OrderId, -1)
    }
}
package com.yunshang.haile_life.data.agruments

import android.content.Intent
import android.os.Bundle
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.data.entities.AppointDevice
import com.yunshang.haile_life.data.entities.TradePreviewParticipate

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

        /**
         * 包装参数
         */
        fun pack(payCode: String): Bundle = Bundle().apply {
            putString(CODE, payCode)
        }

        /**
         * 解析code
         */
        fun parseCode(intent: Intent): String? = intent.getStringExtra(CODE)
    }

    object OrderSubmitParams {
        private const val Goods = "goods"
        private const val ReserveTime = "reserveTime"
        private const val DeviceName = "deviceName"
        private const val ShopAddress = "shopAddress"

        /**
         * 包装参数
         */
        fun pack(
            goods: List<OrderSubmitGood>,
            reserveTime: String? = null,
            deviceName: String? = null,
            shopAddress: String? = null
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
            }

        /**
         * 解析code
         */
        fun parseGoods(intent: Intent): MutableList<OrderSubmitGood>? =
            GsonUtils.json2List(intent.getStringExtra(Goods), OrderSubmitGood::class.java)

        fun parseReserveTime(intent: Intent): String? = intent.getStringExtra(ReserveTime)
        fun parseDeviceName(intent: Intent): String? = intent.getStringExtra(DeviceName)
        fun parseShopAddress(intent: Intent): String? = intent.getStringExtra(ShopAddress)

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
        private const val IsRechargeShop = "isRechargeShop"

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

        fun parseIsMain(bundle: Bundle): Boolean = bundle.getBoolean(IsMain, false)
        fun parseStatus(bundle: Bundle): Int? = bundle.getInt(Status, -1).let {
            if (-1 == it) null else it
        }
    }

    object DiscountCouponSelectorParams {
        private const val SelectCoupon = "SelectCoupon"

        /**
         * 包装参数
         */
        fun pack(
            select: List<TradePreviewParticipate>? = null,
        ): Bundle =
            Bundle().apply {
                select?.let {
                    putString(SelectCoupon, GsonUtils.any2Json(select))
                }
            }

        fun parseSelectCoupon(intent: Intent): MutableList<TradePreviewParticipate>? =
            GsonUtils.json2List(
                intent.getStringExtra(SelectCoupon),
                TradePreviewParticipate::class.java
            )
    }

    object RechargeStarfishParams {
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

    object OrderParams {
        private const val OrderNo = "orderNo"

        /**
         * 包装参数
         */
        fun pack(orderNo: String): Bundle =
            Bundle().apply {
                putString(OrderNo, orderNo)
            }

        fun parseOrderNo(intent: Intent): String? = intent.getStringExtra(OrderNo)
    }
}
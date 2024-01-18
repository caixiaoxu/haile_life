package com.yunshang.haile_life.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StatusBarUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderPaySubmitViewModel
import com.yunshang.haile_life.business.vm.OrderStatusViewModel
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.TradePreviewGoodItem
import com.yunshang.haile_life.data.entities.TradePreviewParticipate
import com.yunshang.haile_life.databinding.FragmentOrderPaySubmitBinding
import com.yunshang.haile_life.databinding.ItemOrderSubmitGoodBinding
import com.yunshang.haile_life.databinding.ItemOrderSubmitGoodDispenserBinding
import com.yunshang.haile_life.databinding.ItemOrderSubmitGoodItemBinding
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.ui.activity.marketing.DiscountCouponSelectorActivity
import com.yunshang.haile_life.ui.activity.shop.RechargeStarfishActivity
import com.yunshang.haile_life.ui.view.dialog.BalancePaySureDialog
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.ScanOrderConfirmDialog
import com.yunshang.haile_life.ui.view.dialog.ShopActivitiesDialog
import com.yunshang.haile_life.utils.thrid.WeChatHelper

class OrderPaySubmitFragment :
    BaseBusinessFragment<FragmentOrderPaySubmitBinding, OrderPaySubmitViewModel>(
        OrderPaySubmitViewModel::class.java, BR.vm
    ) {

    val mActivityViewModel by lazy {
        getActivityViewModelProvider(requireActivity())[OrderStatusViewModel::class.java]
    }

    // 优惠券选择界面
    private val startDiscountCouponSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.let { intent ->
                    mActivityViewModel.selectParticipate?.removeAll { item ->
                        IntentParams.DiscountCouponSelectorParams.parsePromotionProduct(
                            intent
                        ) == item.promotionProduct
                    }
                    IntentParams.DiscountCouponSelectorParams.parseSelectCoupon(
                        intent
                    )?.let { list ->
                        mActivityViewModel.selectParticipate?.addAll(list)
                    }
                }
                mActivityViewModel.requestPreview()
            }
        }

    override fun layoutId(): Int = R.layout.fragment_order_pay_submit

    override fun backBtn(): View = mBinding.barOrderPaySubmitTitle.getBackBtn()

    override fun onBackListener() {
        requireActivity().finish()
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            putExtras(IntentParams.DefaultPageParams.pack(3))
        })
    }

    override fun initEvent() {
        super.initEvent()

        mActivityViewModel.orderDetails.observe(this) { detail ->
            detail?.let {
                // 失效时间
                mViewModel.validTime = detail.invalidTimeStamp
                mViewModel.checkValidTime()

                mViewModel.isDryer.value =
                    DeviceCategory.isDryer(detail.orderItemList.firstOrNull()?.categoryCode)
            }
        }

        mActivityViewModel.tradePreview.observe(this) {
            it?.let { trade ->
                // 已选优惠
                mActivityViewModel.selectParticipate =
                    mutableListOf<TradePreviewParticipate>().apply {
                        for (promotion in trade.promotionList) {
                            if (promotion.participateList.isNotEmpty()) {
                                addAll(promotion.participateList)
                            }
                        }
                    }

                val count = mBinding.includeOrderPaySubmitSpecs.llOrderSubmitGood.childCount
                if (count > 2) {
                    mBinding.includeOrderPaySubmitSpecs.llOrderSubmitGood.removeViews(0, count - 2)
                }
                val inflater = LayoutInflater.from(requireContext())
                if (trade.itemList.isNotEmpty()) {
                    for (good in trade.itemList.filter { item -> !DeviceCategory.isDispenser(item.goodsCategoryCode) }) {
                        val childGoodBinding = DataBindingUtil.inflate<ItemOrderSubmitGoodBinding>(
                            inflater,
                            R.layout.item_order_submit_good,
                            null,
                            false
                        )
                        childGoodBinding.item = good
                        childGoodBinding.showDiscount = trade.showDiscount()
                        mBinding.includeOrderPaySubmitSpecs.llOrderSubmitGood.addView(
                            childGoodBinding.root,
                            (mBinding.includeOrderPaySubmitSpecs.llOrderSubmitGood.childCount - 2),
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
                    }

                    // 投放器的数据
                    val dispenserList =
                        trade.itemList.filter { item -> DeviceCategory.isDispenser(item.goodsCategoryCode) }
                    if (dispenserList.isNotEmpty()) {
                        val childDispenserGoodBinding =
                            DataBindingUtil.inflate<ItemOrderSubmitGoodDispenserBinding>(
                                inflater,
                                R.layout.item_order_submit_good_dispenser,
                                null,
                                false
                            )
                        childDispenserGoodBinding.showDiscount = trade.showDiscount()
                        childDispenserGoodBinding.llOrderSubmitGoodDispenserItem.buildChild<ItemOrderSubmitGoodItemBinding, TradePreviewGoodItem>(
                            dispenserList
                        ) { _, childBinding, data ->
                            childBinding.title =
                                data.goodsItemName + "${data.num}ml"
                            childBinding.type = 0
                            childBinding.value = data.getOriginAmountStr()
                        }
                        try {
                            var discountVal = 0.0
                            var totalPriceVal = 0.0
                            dispenserList.forEach { item ->
                                discountVal += item.discountAmount.toDouble()
                                totalPriceVal += item.realAmount.toDouble()
                            }
                            childDispenserGoodBinding.discount =
                                com.yunshang.haile_life.utils.string.StringUtils.formatAmountStr(
                                    -discountVal
                                )
                            childDispenserGoodBinding.price =
                                com.yunshang.haile_life.utils.string.StringUtils.formatAmountStr(
                                    totalPriceVal
                                )
                            childDispenserGoodBinding.includeOrderSubmitGoodDiscount.root.visibility =
                                View.VISIBLE
                        } catch (e: Exception) {
                            e.printStackTrace()
                            childDispenserGoodBinding.includeOrderSubmitGoodDiscount.root.visibility =
                                View.GONE
                        }
                        mBinding.includeOrderPaySubmitSpecs.llOrderSubmitGood.addView(
                            childDispenserGoodBinding.root,
                            (mBinding.includeOrderPaySubmitSpecs.llOrderSubmitGood.childCount - 2),
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
                    }
                }

                if (trade.promotionList.isNotEmpty()) {
                    if (mBinding.includeOrderPaySubmitSpecs.llOrderGoodDiscounts.childCount > 0) {
                        mBinding.includeOrderPaySubmitSpecs.llOrderGoodDiscounts.removeAllViews()
                    }
                    for (promotion in trade.promotionList.filter { item -> item.available && item.used }) {
                        val childBinding = DataBindingUtil.inflate<ItemOrderSubmitGoodItemBinding>(
                            inflater,
                            R.layout.item_order_submit_good_item,
                            null,
                            false
                        )

                        childBinding.type = 4
                        childBinding.icon = promotion.getDiscountIcon()
                        childBinding.title = promotion.getDiscountTitle()
                        childBinding.value =
                            "-" + com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(
                                promotion.discountPrice
                            )

                        if ((2 == promotion.promotionProduct || 4 == promotion.promotionProduct)) {
                            if (!promotion.used) {
                                childBinding.noSelect = true
                                childBinding.endDraw = 0
                                val pH = DimensionUtils.dip2px(requireContext(), 8f)
                                childBinding.tvOrderSubmitGoodValue.setPadding(pH, 0, pH, 0)
                                (childBinding.tvOrderSubmitGoodValue.layoutParams as LinearLayout.LayoutParams).run {
                                    height = DimensionUtils.dip2px(requireContext(), 25f)
                                }
                                childBinding.value = StringUtils.getString(
                                    R.string.available_coupon_num,
                                    promotion.options?.filter { item -> item.available }?.size ?: 0
                                )
                            } else {
                                childBinding.endDraw = R.mipmap.icon_small_arrow_right
                            }
                            childBinding.tvOrderSubmitGoodValue.setOnClickListener {
                                if (false == mViewModel.inValidOrder.value) {
                                    startDiscountCouponSelect.launch(
                                        Intent(
                                            requireContext(),
                                            DiscountCouponSelectorActivity::class.java
                                        ).apply {
                                            putExtras(
                                                IntentParams.OrderSubmitParams.pack(
                                                    trade.itemList.map { item ->
                                                        IntentParams.OrderSubmitParams.OrderSubmitGood(
                                                            item.goodsCategoryCode,
                                                            item.goodsId,
                                                            item.goodsItemId,
                                                            item.num
                                                        )
                                                    }.toList()
                                                )
                                            )
                                            putExtras(
                                                IntentParams.DiscountCouponSelectorParams.pack(
                                                    promotion.participateList,
                                                    promotion.promotionProduct,
                                                    trade.promotionList.filter { item -> promotion.promotionProduct != item.promotionProduct }
                                                        .flatMap { item ->
                                                            item.participateList
                                                        },
                                                    mActivityViewModel.orderNo
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        } else if (5 == promotion.promotionProduct) {
                            childBinding.endDraw =
                                if (promotion.used) {
                                    if (DeviceCategory.isDryer(trade.itemList.firstOrNull()?.goodsCategoryCode)) R.mipmap.icon_orange_checked
                                    else R.mipmap.icon_cyan_checked
                                } else R.mipmap.icon_uncheck
                            childBinding.tvOrderSubmitGoodValue.setOnClickListener {
                                if (promotion.used) {
                                    // 如果强制使用海星，不可取消
                                    if (promotion.forceUse) return@setOnClickListener

                                    mActivityViewModel.selectParticipate?.removeAll { item -> 5 == item.promotionProduct }
                                } else {
                                    if (!promotion.options.isNullOrEmpty()) {
                                        mActivityViewModel.selectParticipate?.addAll(promotion.options)
                                    }
                                }
                                mActivityViewModel.requestPreview()
                            }
                        } else {
                            childBinding.endDraw = 0
                        }

                        mBinding.includeOrderPaySubmitSpecs.llOrderGoodDiscounts.addView(
                            childBinding.root,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    mBinding.includeOrderPaySubmitSpecs.llOrderGoodDiscounts.visibility =
                        View.VISIBLE
                } else {
                    mBinding.includeOrderPaySubmitSpecs.llOrderGoodDiscounts.visibility = View.GONE
                }

                changePayWay()
            }
        }

        mActivityViewModel.shopConfig.observe(this) {
            it?.let { config ->
                if ((1 == config.configType && 1 == config.tokenCoinForceUse) && (config.result || !mActivityViewModel.tradePreview.value!!.isZero())) {
                    CommonDialog.Builder("海星余额不足，请先购买海星后再使用").apply {
                        title = StringUtils.getString(R.string.friendly_reminder)
                        isCancelable = config.closable
                        negativeTxt = StringUtils.getString(R.string.cancel)
                        setPositiveButton(StringUtils.getString(R.string.go_recharge)) {
                            goToRechargeStarfishPage()
                        }
                    }.build().show(childFragmentManager)
                }
            }
        }

        mActivityViewModel.balance.observe(this) {
            try {
                mActivityViewModel.tradePreview.value?.realPrice?.toDouble()?.let { price ->
                    if (it.amount.toDouble() < price) {
                        mBinding.includeOrderPaySubmitPayWay.rbOrderSubmitBalancePayWay.text =
                            com.yunshang.haile_life.utils.string.StringUtils.formatBalancePayStyleStr(
                                requireContext()
                            )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 充值后刷新店铺配置
        LiveDataBus.with(BusEvents.RECHARGE_SUCCESS_STATUS)?.observe(this) {
            mActivityViewModel.requestPreview()
        }

        mViewModel.jump.observe(this) {
            when (it) {
                1 -> mActivityViewModel.requestPreview()
                else -> mActivityViewModel.jump.postValue(1)
            }
        }
    }

    override fun initView() {
        mBinding.avm = mActivityViewModel
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

        mBinding.btnOrderPaySubmitCancel.setOnClickListener {
            CommonDialog.Builder(StringUtils.getString(R.string.cancel_appoint_order_prompt))
                .apply {
                    negativeTxt = StringUtils.getString(R.string.no)
                    setPositiveButton(StringUtils.getString(R.string.yes)) {
                        mActivityViewModel.cancelOrder()
                    }
                }.build().show(childFragmentManager)
        }

        mBinding.includeOrderPaySubmitPayWay.rgOrderSubmitPayWay.setOnCheckedChangeListener { _, _ ->
            changePayWay()
        }

        mBinding.btnOrderPayRecharge.setOnClickListener {
            goToRechargeStarfishPage()
        }

        // 筒自洁
        mBinding.btnOrderPaySubmitSelfClean.setOnClickListener {
            CommonDialog.Builder("该设备有筒自洁功能，去除残留污垢和细菌，耗时${mActivityViewModel.tradePreview.value?.selfCleanInfo?.remainMinutes}分钟，是否需要？\n\n点击需要，设备立即启动，请勿放入衣物")
                .apply {
                    title = "健康筒自洁"
                    negativeTxt = "不需要"
                    setPositiveButton("需要") {
                        mActivityViewModel.startSelfClean()
                    }
                }.build().show(childFragmentManager)
        }

        // 支付
        mBinding.btnOrderPaySubmitOnlyPay.setOnClickListener {
            payEvent()
        }
        // 支付
        mBinding.btnOrderPaySubmitPay.setOnClickListener {
            payEvent()
        }
    }

    private fun payEvent() {
        if (true == mActivityViewModel.shopConfig.value?.result) {
            mActivityViewModel.payMethod = 1001
        }

        if (-1 == mActivityViewModel.payMethod) {
            SToast.showToast(requireContext(), "请选择支付方式")
            return
        }

        val noMoney = 1001 == mActivityViewModel.payMethod && try {
            mActivityViewModel.balance.value!!.amount.toDouble() < mActivityViewModel.tradePreview.value!!.realPrice.toDouble()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        if (noMoney) {
            SToast.showToast(requireContext(), "余额不足，先选择其他方式支付")
            return
        }

        // 判断是否跳转验证
        payDialog()
    }

    /**
     * 跳转到海星充值页
     */
    private fun goToRechargeStarfishPage() {
        startActivity(
            Intent(
                requireContext(),
                RechargeStarfishActivity::class.java
            ).apply {
                putExtras(
                    IntentParams.RechargeStarfishParams.pack(
                        mActivityViewModel.tradePreview.value?.itemList?.firstOrNull()?.shopId
                    )
                )
            })
    }

    private fun payDialog() {
        mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.let { firstItem ->
            if (!DeviceCategory.isHair(firstItem.categoryCode)) {
                val hasClean =
                    true == mActivityViewModel.orderDetails.value?.orderItemList?.any { item -> item.selfClean }
                ScanOrderConfirmDialog.Builder(firstItem.categoryCode, hasClean) {
                    goPay()
                }.build().show(childFragmentManager)
            } else {
                goPay()
            }
        }
    }

    private fun goPay() {
        if (1001 == mActivityViewModel.payMethod && !(1 == mActivityViewModel.shopConfig.value?.configType
                    && 1 == mActivityViewModel.shopConfig.value?.tokenCoinForceUse)
        ) {
            if (null != mActivityViewModel.tradePreview.value && null != mActivityViewModel.balance.value) {
                BalancePaySureDialog(
                    mActivityViewModel.balance.value!!.amount,
                    mActivityViewModel.tradePreview.value!!.realPrice
                ) {
                    mActivityViewModel.requestPrePay(requireContext())
                }.show(childFragmentManager)
            }
        } else mActivityViewModel.requestPrePay(requireContext()) { orderNo, payMethod ->
            val page = "pages/pay/cashier?orderNo=$orderNo"
            if (103 == payMethod) {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("alipays://platformapi/startapp?appId=2021003183687122&page=$page")
                )
                startActivity(intent)
            } else {
                // pages/pay/cashier?orderNo=1020240109144516781487
                WeChatHelper.openWeChatMiniProgram(
                    page,
                    null,
                    "gh_102c08f8d7a4"
                )
            }
            orderNo?.let {
                mViewModel.eachRefreshPayStatus(it, true) {
                    mActivityViewModel.requestData(true)
                }
            }
        }
    }

    /**
     * 切换支付方式
     */
    private fun changePayWay() {
        mActivityViewModel.tradePreview.value?.let {
            mActivityViewModel.payMethod =
                if (it.isZero()) 1001 else when (mBinding.includeOrderPaySubmitPayWay.rgOrderSubmitPayWay.checkedRadioButtonId) {
                    R.id.rb_order_submit_balance_pay_way -> 1001
                    R.id.rb_order_submit_alipay_pay_way -> 103
                    R.id.rb_order_submit_wechat_pay_way -> 203
                    else -> -1
                }
        }
    }

    override fun initData() {
        mActivityViewModel.requestPreview()
    }
}
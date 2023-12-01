package com.yunshang.haile_life.ui.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StatusBarUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
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
import com.yunshang.haile_life.ui.view.dialog.BalancePaySureDialog
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.ScanOrderConfirmDialog

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
                    for (good in trade.itemList.filter { item -> !DeviceCategory.isDispenser(item.goodsCategoryCode) && !item.selfClean }) {
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

                    // 投放器和筒自洁的数据
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
                                data.goodsItemName + if (data.selfClean) "" else "${data.num}ml"
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

                    // 筒自洁
                    trade.itemList.find { item -> item.selfClean }?.let { good ->
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
                }

                if (trade.promotionList.isNotEmpty()) {
                    if (mBinding.includeOrderPaySubmitSpecs.llOrderGoodDiscounts.childCount > 0) {
                        mBinding.includeOrderPaySubmitSpecs.llOrderGoodDiscounts.removeAllViews()
                    }
                    for (promotion in trade.promotionList.filter { item -> item.available }) {
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
                                    promotion.options.filter { item -> item.available }.size
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
                                                        }
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
                                    if (promotion.options.isNotEmpty()) {
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

        mBinding.btnOrderPaySubmitPay.setOnClickListener {
            if (-1 == mActivityViewModel.payMethod) {
                SToast.showToast(requireContext(), "请选择支付方式")
                return@setOnClickListener
            }

            val noMoney = 1001 == mActivityViewModel.payMethod && try {
                mActivityViewModel.balance.value!!.amount.toDouble() < mActivityViewModel.tradePreview.value!!.realPrice.toDouble()
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
            if (noMoney) {
                SToast.showToast(requireContext(), "余额不足，先选择其他方式支付")
                return@setOnClickListener
            }

            // 判断是否跳转验证
            payDialog()
        }
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
        if (1001 == mActivityViewModel.payMethod) {
            if (null != mActivityViewModel.tradePreview.value && null != mActivityViewModel.balance.value) {
                BalancePaySureDialog(
                    mActivityViewModel.balance.value!!.amount,
                    mActivityViewModel.tradePreview.value!!.realPrice
                ) {
                    mActivityViewModel.requestPrePay(requireContext())
                }.show(childFragmentManager)
            }
        } else mActivityViewModel.requestPrePay(requireContext())
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

    override fun onStart() {
        super.onStart()

        Handler(Looper.getMainLooper()).postDelayed({
            // 如果是未支付完成，并且订单不为空
            if (2 == mActivityViewModel.isPayFinish && !mActivityViewModel.orderNo.isNullOrEmpty()) {
                mActivityViewModel.jump.postValue(1)
            }
        }, 300)
    }

    override fun initData() {
        mActivityViewModel.requestPreview()
    }
}
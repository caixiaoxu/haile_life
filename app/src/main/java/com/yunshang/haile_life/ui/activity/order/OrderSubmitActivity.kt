package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderSubmitViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.TradePreviewParticipate
import com.yunshang.haile_life.data.entities.WxPrePayEntity
import com.yunshang.haile_life.databinding.ActivityOrderSubmitBinding
import com.yunshang.haile_life.databinding.ItemOrderSubmitGoodBinding
import com.yunshang.haile_life.databinding.ItemOrderSubmitGoodItemBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.marketing.DiscountCouponSelectorActivity
import com.yunshang.haile_life.ui.view.dialog.BalancePaySureDialog
import com.yunshang.haile_life.utils.string.RoundBackgroundColorSpan
import com.yunshang.haile_life.utils.thrid.WeChatHelper

class OrderSubmitActivity : BaseBusinessActivity<ActivityOrderSubmitBinding, OrderSubmitViewModel>(
    OrderSubmitViewModel::class.java, BR.vm
) {

    // 优惠券选择界面
    private val startDiscountCouponSelect =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                mViewModel.selectParticipate?.removeAll { item -> 2 == item.promotionProduct || 4 == item.promotionProduct }
                it.data?.let { intent ->
                    IntentParams.DiscountCouponSelectorParams.parseSelectCoupon(
                        intent
                    )?.let { list ->
                        mViewModel.selectParticipate?.addAll(list)
                    }
                }
                mViewModel.requestData()
            }
        }

    override fun layoutId(): Int = R.layout.activity_order_submit

    override fun backBtn(): View = mBinding.barOrderSubmitTitle

    override fun initIntent() {
        super.initIntent()

        mViewModel.goods.value =
            IntentParams.OrderSubmitParams.parseGoods(intent) ?: mutableListOf()
        mViewModel.reserveTime.value = IntentParams.OrderSubmitParams.parseReserveTime(intent)
        mViewModel.deviceName.value = IntentParams.OrderSubmitParams.parseDeviceName(intent) ?: ""
        mViewModel.shopAddress.value = IntentParams.OrderSubmitParams.parseShopAddress(intent) ?: ""
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.tradePreview.observe(this) {
            it?.let { trade ->

                // 已选优惠
                mViewModel.selectParticipate = mutableListOf<TradePreviewParticipate>().apply {
                    for (promotion in trade.promotionList) {
                        if (promotion.participateList.isNotEmpty()) {
                            addAll(promotion.participateList)
                        }
                    }
                }

                val count = mBinding.llOrderSubmitGood.childCount
                if (count > 2) {
                    mBinding.llOrderSubmitGood.removeViews(0, count - 2)
                }
                val inflater = LayoutInflater.from(this@OrderSubmitActivity)
                if (trade.itemList.isNotEmpty()) {
                    for (good in trade.itemList) {
                        val childGoodBinding = DataBindingUtil.inflate<ItemOrderSubmitGoodBinding>(
                            inflater,
                            R.layout.item_order_submit_good,
                            null,
                            false
                        )
                        childGoodBinding.item = good
                        mBinding.llOrderSubmitGood.addView(
                            childGoodBinding.root,
                            (mBinding.llOrderSubmitGood.childCount - 2),
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
                    }
                }

                if (trade.promotionList.isNotEmpty()) {
                    if (mBinding.llOrderGoodDiscounts.childCount > 0) {
                        mBinding.llOrderGoodDiscounts.removeAllViews()
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
                        childBinding.canCancel = 0
                        childBinding.value =
                            com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(
                                promotion.discountPrice
                            )

                        if ((2 == promotion.promotionProduct || 4 == promotion.promotionProduct)) {
                            if (!promotion.used) {
                                childBinding.noSelect = true
                                val pH = DimensionUtils.dip2px(this@OrderSubmitActivity, 8f)
                                childBinding.tvOrderSubmitGoodValue.setPadding(pH, 0, pH, 0)
                                (childBinding.tvOrderSubmitGoodValue.layoutParams as ConstraintLayout.LayoutParams).run {
                                    height = DimensionUtils.dip2px(this@OrderSubmitActivity, 25f)
                                }
                                childBinding.value = StringUtils.getString(
                                    R.string.available_coupon_num,
                                    promotion.options.filter { item -> item.available }.size
                                )
                            }
                            childBinding.tvOrderSubmitGoodValue.setOnClickListener {
                                startDiscountCouponSelect.launch(
                                    Intent(
                                        this@OrderSubmitActivity,
                                        DiscountCouponSelectorActivity::class.java
                                    ).apply {
                                        putExtras(intent)
                                        putExtras(
                                            IntentParams.DiscountCouponSelectorParams.pack(
                                                promotion.participateList
                                            )
                                        )
                                    }
                                )
                            }
                        } else if (5 == promotion.promotionProduct) {
                            childBinding.isDryer = mViewModel.isDryer.value ?: false
                            childBinding.canCancel = if (promotion.used) 1 else -1
                            childBinding.tvOrderSubmitGoodValue.setOnClickListener {
                                if (promotion.used) {
                                    mViewModel.selectParticipate?.removeAll { item -> 5 == item.promotionProduct }
                                } else {
                                    if (promotion.options.isNotEmpty()) {
                                        mViewModel.selectParticipate?.addAll(promotion.options)
                                    }
                                }
                                mViewModel.requestData()
                            }
                        }

                        mBinding.llOrderGoodDiscounts.addView(
                            childBinding.root,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    }
                    mBinding.llOrderGoodDiscounts.visibility = View.VISIBLE
                } else {
                    mBinding.llOrderGoodDiscounts.visibility = View.GONE
                }
            }
        }

        mViewModel.balance.observe(this) {
            try {
                mViewModel.tradePreview.value?.realPrice?.toDouble()?.let { price ->
                    if (it.amount.toDouble() < price) {
                        mBinding.includeOrderSubmitPayWay.rbOrderSubmitBalancePayWay.text =
                            com.yunshang.haile_life.utils.string.StringUtils.formatBalancePayStyleStr(
                                this@OrderSubmitActivity
                            )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mViewModel.prepayParam.observe(this) {
            it?.let {
                if (103 == mViewModel.payMethod) {
                    mViewModel.alipay(this@OrderSubmitActivity, it)
                } else if (203 == mViewModel.payMethod) {
                    GsonUtils.json2Class(it, WxPrePayEntity::class.java)?.let { wxPrePayBean ->
                        WeChatHelper.openWeChatPay(
                            wxPrePayBean.appId,
                            wxPrePayBean.partnerId,
                            wxPrePayBean.prepayId,
                            wxPrePayBean.nonceStr,
                            wxPrePayBean.timeStamp,
                            wxPrePayBean.paySign

                        )
                    }
                }
            }
        }

        LiveDataBus.with(BusEvents.WXPAY_STATUS)?.observe(this) {
            mViewModel.requestAsyncPayAsync()
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS, Boolean::class.java)?.observe(this) {
            if (it) {
                startActivity(
                    Intent(
                        this@OrderSubmitActivity,
                        OrderPaySuccessActivity::class.java
                    ).apply {
                        if (mViewModel.orderNo.isNotEmpty()) {
                            putExtras(IntentParams.OrderParams.pack(mViewModel.orderNo))
                        }
                    })
                finish()
            }
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.includeOrderSubmitPayWay.rgOrderSubmitPayWay.setOnCheckedChangeListener { _, checkedId ->
            mViewModel.tradePreview.value?.let {
                mViewModel.payMethod = if (it.isZero()) 1001 else when (checkedId) {
                    R.id.rb_order_submit_balance_pay_way -> 1001
                    R.id.rb_order_submit_alipay_pay_way -> 103
                    R.id.rb_order_submit_wechat_pay_way -> 203
                    else -> -1
                }
            }
        }
        mBinding.btnOrderSubmitPay.setOnClickListener {
            if (-1 == mViewModel.payMethod) {
                SToast.showToast(this@OrderSubmitActivity, "请选择支付方式")
                return@setOnClickListener
            }

            if (1001 == mViewModel.payMethod) {
                if (null != mViewModel.tradePreview.value && null != mViewModel.balance.value) {
                    BalancePaySureDialog(
                        mViewModel.balance.value!!.amount,
                        mViewModel.tradePreview.value!!.realPrice
                    ) {
                        mViewModel.requestPrePay()
                    }.show(supportFragmentManager)
                }
            } else mViewModel.requestPrePay()
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
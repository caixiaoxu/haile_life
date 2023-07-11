package com.yunshang.haile_life.ui.activity.shop

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.RechargeStarfishViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.WxPrePayEntity
import com.yunshang.haile_life.databinding.ActivityRechargeStarfishBinding
import com.yunshang.haile_life.databinding.ItemRechargeStarfishListBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.order.OrderPaySuccessActivity
import com.yunshang.haile_life.utils.string.RoundBackgroundColorSpan
import com.yunshang.haile_life.utils.string.StringUtils
import com.yunshang.haile_life.utils.thrid.WeChatHelper

class RechargeStarfishActivity :
    BaseBusinessActivity<ActivityRechargeStarfishBinding, RechargeStarfishViewModel>(
        RechargeStarfishViewModel::class.java, BR.vm
    ) {
    override fun layoutId(): Int = R.layout.activity_recharge_starfish

    override fun backBtn(): View = mBinding.barRechargeStarfishTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.shopId = IntentParams.RechargeStarfishParams.parseShopId(intent)
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.shopStarfishList.observe(this) {
            it?.let {

                mBinding.glRechargeStarfishList.let { grid ->
                    if (it.rewardList.isEmpty()) {
                        grid.visibility = View.GONE
                    } else {
                        if (grid.childCount > 0) {
                            grid.removeAllViews()
                        }
                        val column = 3
                        val space = DimensionUtils.dip2px(this@RechargeStarfishActivity, 5f)
                        val width = (grid.measuredWidth - space * (column - 1)) / column
                        val inflater = LayoutInflater.from(this@RechargeStarfishActivity)
                        it.rewardList.forEachIndexed { index, reward ->
                            val itemBinding =
                                DataBindingUtil.inflate<ItemRechargeStarfishListBinding>(
                                    inflater,
                                    R.layout.item_recharge_starfish_list,
                                    null,
                                    false
                                )
                            itemBinding.item = reward
                            itemBinding.ivRechargeStarfishRecommend.visibility =
                                if (0 == index) View.VISIBLE else View.GONE
                            mViewModel.selectGoodsItem.observe(this@RechargeStarfishActivity) { item ->
                                itemBinding.llRechargeStarfishItem.setBackgroundResource(
                                    if (reward.goodsItemId == item.goodsItemId) R.drawable.shape_s04d1e5_04d7e5_r8
                                    else R.drawable.shape_sf5f5f5_r8
                                )
                            }
                            itemBinding.root.setOnClickListener {
                                mViewModel.selectGoodsItem.value = reward
                            }
                            grid.addView(
                                itemBinding.root, GridLayout.LayoutParams(
                                    ViewGroup.LayoutParams(
                                        width,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                ).apply {
                                    marginStart = if (0 == index) 0 else space
                                }
                            )
                        }
                        grid.visibility = View.VISIBLE
                    }
                }
            }
        }

        mViewModel.prepayParam.observe(this) {
            it?.let {
                if (103 == mViewModel.payMethod) {
                    mViewModel.alipay(this@RechargeStarfishActivity, it)
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

        mViewModel.asyncPay.observe(this) {
            if (it) {
                startActivity(
                    Intent(
                        this@RechargeStarfishActivity,
                        OrderPaySuccessActivity::class.java
                    ).apply {
                        if (mViewModel.orderNo.isNotEmpty()) {
                            putExtras(IntentParams.OrderParams.pack(mViewModel.orderNo))
                        }
                    })
                finish()
            }
        }

        mViewModel.balance.observe(this) {
            try {
                mViewModel.selectGoodsItem.value?.price?.toDouble()?.let { price ->
                    if (it.amount.toDouble() < price) {
                        mBinding.includeRechargeStarfishPayWay.rbOrderSubmitBalancePayWay.text =
                            "余额  余额不足".let { content ->
                                StringUtils.formatMultiStyleStr(
                                    content,
                                    arrayOf(
                                        AbsoluteSizeSpan(
                                            DimensionUtils.sp2px(
                                                10f,
                                                this@RechargeStarfishActivity
                                            )
                                        ),
                                        StyleSpan(Typeface.NORMAL),
                                        RoundBackgroundColorSpan(
                                            ContextCompat.getColor(
                                                this@RechargeStarfishActivity,
                                                R.color.color_appoint_bg
                                            ),
                                            ContextCompat.getColor(
                                                this@RechargeStarfishActivity,
                                                R.color.color_ff630e
                                            ),
                                            DimensionUtils.dip2px(this@RechargeStarfishActivity, 4f)
                                                .toFloat()
                                        )
                                    ),
                                    3, content.length
                                )
                            }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.includeRechargeStarfishMine.tvItemTitle.run {
            textSize = 16f
            setTextColor(
                ContextCompat.getColor(
                    this@RechargeStarfishActivity,
                    R.color.color_black_85
                )
            )
        }

        mBinding.includeRechargeStarfishMine.tvItemValue.run {
            textSize = 22f
            setTextColor(
                ContextCompat.getColor(
                    this@RechargeStarfishActivity,
                    R.color.color_black_85
                )
            )
            ResourcesCompat.getFont(context, R.font.money)?.let {
                typeface = it
            }
        }

        mBinding.includeRechargeStarfishPayWay.rgOrderSubmitPayWay.setOnCheckedChangeListener { _, checkedId ->
            mViewModel.payMethod = when (checkedId) {
                R.id.rb_order_submit_balance_pay_way -> 1001
                R.id.rb_order_submit_alipay_pay_way -> 103
                R.id.rb_order_submit_wechat_pay_way -> 203
                else -> -1
            }
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
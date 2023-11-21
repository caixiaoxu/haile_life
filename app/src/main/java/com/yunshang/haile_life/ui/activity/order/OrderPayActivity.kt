package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.text.style.AbsoluteSizeSpan
import android.view.View
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderPayViewModel
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.WxPrePayEntity
import com.yunshang.haile_life.databinding.ActivityOrderPayBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.dialog.BalancePaySureDialog
import com.yunshang.haile_life.utils.string.StringUtils
import com.yunshang.haile_life.utils.thrid.WeChatHelper

class OrderPayActivity : BaseBusinessActivity<ActivityOrderPayBinding, OrderPayViewModel>(
    OrderPayViewModel::class.java, BR.vm
) {
    override fun layoutId(): Int = R.layout.activity_order_pay

    override fun backBtn(): View = mBinding.barOrderPayTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.orderNo = IntentParams.OrderPayParams.parseOrderNo(intent)
        mViewModel.isAppoint = IntentParams.OrderParams.parseIsAppoint(intent)
        mViewModel.timeRemaining = IntentParams.OrderPayParams.parseTimeRemaining(intent)
        mViewModel.price = IntentParams.OrderPayParams.parsePrice(intent) ?: ""
        mViewModel.orderItems = IntentParams.OrderPayParams.parseOrderItems(intent)
        mViewModel.isDrinking =
            DeviceCategory.isDrinking(mViewModel.orderItems?.firstOrNull()?.categoryCode)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.balance.observe(this) {
            try {
                if (it.amount.toDouble() < mViewModel.price.toDouble()) {
                    mBinding.rbOrderBalancePayWay.text =
                        StringUtils.formatBalancePayStyleStr(this@OrderPayActivity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mViewModel.prepayParam.observe(this) {
            it?.let {
                if (103 == mViewModel.payMethod) {
                    mViewModel.alipay(this@OrderPayActivity, it)
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
        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS)?.observe(this) {
            SToast.showToast(this@OrderPayActivity, R.string.pay_success)

            if (mViewModel.isAppoint) {
                mViewModel.orderNo?.let {orderNo->
                    startActivity(
                        Intent(
                            this@OrderPayActivity,
                            OrderStatusActivity::class.java
                        ).apply {
                            putExtras(IntentParams.OrderParams.pack(orderNo))
                        })
                }
            } else {
                finish()
            }
        }
    }

    override fun initView() {
        mBinding.tvOrderPayPrice.text = StringUtils.formatMultiStyleStr(
            com.lsy.framelib.utils.StringUtils.getString(R.string.unit_money) + " " + mViewModel.price,
            arrayOf(
                AbsoluteSizeSpan(DimensionUtils.sp2px(24f, this@OrderPayActivity))
            ), 0, 2
        )
        mBinding.rgOrderPayWay.setOnCheckedChangeListener { _, _ ->
            changePayWay()
        }
        mBinding.btnOrderPay.setOnClickListener {
            if (0L == mViewModel.remaining.value) {
                SToast.showToast(this@OrderPayActivity, "支付已超时")
                return@setOnClickListener
            }
            if (-1 == mViewModel.payMethod) {
                SToast.showToast(this@OrderPayActivity, "请选择支付方式")
                return@setOnClickListener
            }
            if (1001 == mViewModel.payMethod) {
                val noMoney = try {
                    mViewModel.balance.value!!.amount.toDouble() < mViewModel.price.toDouble()
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
                if (noMoney) {
                    SToast.showToast(this@OrderPayActivity, "余额不足，先选择其他方式支付")
                    return@setOnClickListener
                }


                if (null != mViewModel.balance.value) {
                    BalancePaySureDialog(
                        mViewModel.balance.value!!.amount,
                        mViewModel.price
                    ) {
                        mViewModel.requestPrePay(this)
                    }.show(supportFragmentManager)
                }
            } else mViewModel.requestPrePay(this)
        }
    }


    /**
     * 切换支付方式
     */
    private fun changePayWay() {
        try {
            mViewModel.payMethod =
                if (mViewModel.price.toDouble() == 0.0) 1001 else when (mBinding.rgOrderPayWay.checkedRadioButtonId) {
                    R.id.rb_order_balance_pay_way -> 1001
                    R.id.rb_order_alipay_pay_way -> 103
                    R.id.rb_order_wechat_pay_way -> 203
                    else -> -1
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun initData() {
        changePayWay()
        mViewModel.requestData()
        mViewModel.countDownTimer()
    }

    override fun onDestroy() {
        mViewModel.timer?.cancel()
        super.onDestroy()
    }
}
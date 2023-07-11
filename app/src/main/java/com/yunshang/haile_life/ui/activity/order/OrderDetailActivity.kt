package com.yunshang.haile_life.ui.activity.order

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderDetailViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.PromotionParticipation
import com.yunshang.haile_life.data.entities.WxPrePayEntity
import com.yunshang.haile_life.databinding.ActivityOrderDetailBinding
import com.yunshang.haile_life.databinding.ItemTitleValueLrBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.OrderPayDialog
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.string.StringUtils
import com.yunshang.haile_life.utils.thrid.WeChatHelper


class OrderDetailActivity :
    BaseBusinessActivity<ActivityOrderDetailBinding, OrderDetailViewModel>(
        OrderDetailViewModel::class.java,
        BR.vm
    ) {

    // 拨打电话权限
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result) {
                // 授权权限成功
                mViewModel.orderDetail.value?.buyerPhone?.let {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$it")))
                }
            } else {
                // 授权失败
                SToast.showToast(this, R.string.empty_permission)
            }
        }

    override fun layoutId(): Int = R.layout.activity_order_detail

    override fun backBtn(): View = mBinding.barOrderDetailTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.orderNo = IntentParams.OrderParams.parseOrderNo(intent)
        mViewModel.isAppoint = IntentParams.OrderParams.parseIsAppoint(intent)
        mViewModel.formScan = IntentParams.OrderParams.parseFormScan(intent)
    }

    override fun initEvent() {
        super.initEvent()

        if (!mViewModel.formScan) {
            mViewModel.orderDetail.observe(this) {
                it?.let { detail ->
                    mBinding.llOrderDetailDiscount.buildChild<ItemTitleValueLrBinding, PromotionParticipation>(
                        detail.promotionParticipationList
                    ) { _, childBinding, data ->
                        childBinding.title = data.promotionProductName
                        childBinding.value =
                            "-${StringUtils.formatAmountStrOfStr(data.discountPrice)}"
                    }
                }
            }
        }

        mViewModel.prepayParam.observe(this) {
            it?.let {
                if (103 == mViewModel.payMethod) {
                    mViewModel.alipay(this@OrderDetailActivity, it)
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
        LiveDataBus.with(BusEvents.APPOINT_ORDER_USE_STATUS)?.observe(this) {
            finish()
        }
    }

    override fun initView() {
        mBinding.tvOrderDetailContact.setOnClickListener {
            requestPermission.launch(Manifest.permission.CALL_PHONE)
        }
        mBinding.tvOrderDetailPay.setOnClickListener {
            mViewModel.orderDetail.value?.let { detail ->
                OrderPayDialog.Builder(
                    detail.orderItemList.firstOrNull()?.categoryCode ?: "",
                    try {
                        0.0 == detail.realPrice.toDouble()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                ) { type ->
                    mViewModel.payMethod = when (type) {
                        0 -> 1001
                        1 -> 103
                        2 -> 203
                        else -> -1
                    }
                    mViewModel.requestPrePay()
                }.build().show(supportFragmentManager)
            }
        }

        mBinding.tvOrderDetailCancel.setOnClickListener {
            mViewModel.orderDetail.value?.let { detail ->
                val overTime =
                    DateTimeUtils.formatDateFromString(detail.appointmentUsageTime)?.let {
                        (System.currentTimeMillis() - it.time) / 1000 / 60
                    } ?: -1

                CommonDialog.Builder(
                    if (mViewModel.isAppoint && overTime > 0)
                        com.lsy.framelib.utils.StringUtils.getString(
                            R.string.over_time_prompt, overTime
                        )
                    else com.lsy.framelib.utils.StringUtils.getString(R.string.cancel_order_prompt)
                ).apply {
                    title = "提示"
                    setNegativeButton("确认取消") {
                        mViewModel.cancelOrder()
                    }
                    positiveTxt = "再想想"
                }.build().show(supportFragmentManager)
            }
        }

        mBinding.tvOrderDetailAppointNoUse.setOnClickListener {
            finish()
        }
        mBinding.tvOrderDetailAppointUse.setOnClickListener {
            mViewModel.changeUseModel.value = true
        }
    }

    override fun initData() {
        mViewModel.requestOrderDetailAsync()
    }
}
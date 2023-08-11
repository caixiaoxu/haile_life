package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderDetailViewModel
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.OrderItem
import com.yunshang.haile_life.data.entities.PromotionParticipation
import com.yunshang.haile_life.databinding.ActivityOrderDetailBinding
import com.yunshang.haile_life.databinding.ItemOrderDetailSkuBinding
import com.yunshang.haile_life.databinding.ItemTitleValueLrBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.string.StringUtils


class OrderDetailActivity :
    BaseBusinessActivity<ActivityOrderDetailBinding, OrderDetailViewModel>(
        OrderDetailViewModel::class.java,
        BR.vm
    ) {

    // 拨打电话权限
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                // 授权权限成功
                mViewModel.orderDetail.value?.serviceTelephone?.let {
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
        mViewModel.isAppoint.value = IntentParams.OrderParams.parseIsAppoint(intent)
        mViewModel.formScan.value = 1 == IntentParams.OrderParams.parseFormScan(intent)
    }

    override fun initEvent() {
        super.initEvent()

        if (true != mViewModel.formScan.value) {
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

        mViewModel.remaining.observe(this) {
            mViewModel.orderStatusDesc.value = it?.let {
                val minute = (it / 60000)
                val second = (it % 60000) / 1000
                val content = com.lsy.framelib.utils.StringUtils.getString(
                    R.string.order_status_desc, String.format(
                        "%02d分%02d秒",
                        minute,
                        second
                    )
                )
                StringUtils.formatMultiStyleStr(
                    content, arrayOf(
                        ForegroundColorSpan(
                            ContextCompat.getColor(Constants.APP_CONTEXT, R.color.color_ff630e)
                        )
                    ), 0, content.length
                )
            } ?: SpannableString("订单待付款")
        }

        mViewModel.orderDetail.observe(this) {
            it?.let {
                mViewModel.getOrderStatusVal(it)

                mBinding.llOrderDetailSkus.buildChild<ItemOrderDetailSkuBinding, OrderItem>(it.orderItemList.filter { item ->
                    try {
                        item.unit.toDouble() > 0.0
                    } catch (e: Exception) {
                        e.printStackTrace()
                        true
                    }
                }) { _, childBinding, data ->
                    childBinding.item = data
                    childBinding.state = it.state
                }
            }
        }

        LiveDataBus.with(BusEvents.PAY_OVERTIME_STATUS)?.observe(this) {
            mViewModel.requestOrderDetailAsync()
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS)?.observe(this) {
            mViewModel.requestOrderDetailAsync()
        }
    }

    override fun initView() {
        mBinding.tvOrderDetailContact.setOnClickListener {
            requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
        }
        mBinding.tvOrderDetailPay.setOnClickListener {
            mViewModel.orderDetail.value?.let { detail ->
                startActivity(Intent(this@OrderDetailActivity, OrderPayActivity::class.java).apply {
                    putExtras(
                        IntentParams.OrderPayParams.pack(
                            detail.orderNo,
                            if (DeviceCategory.isDrinking(detail.orderItemList.firstOrNull()?.categoryCode)) "" else detail.invalidTime,
                            detail.realPrice,
                            detail.orderItemList.firstOrNull()?.categoryCode
                        )
                    )
                })
            }
        }

        mBinding.tvOrderDetailCancel.setOnClickListener {
            mViewModel.orderDetail.value?.let { detail ->
                val overTime =
                    DateTimeUtils.formatDateFromString(detail.appointmentUsageTime)?.let {
                        (it.time - System.currentTimeMillis()) / 1000 / 60
                    } ?: -1

                CommonDialog.Builder(
                    if (true == mViewModel.isAppoint.value)
                        if (overTime > 10)
                            com.lsy.framelib.utils.StringUtils.getString(
                                R.string.over_time_prompt2
                            )
                        else if (overTime > 0)
                            com.lsy.framelib.utils.StringUtils.getString(
                                R.string.over_time_prompt1
                            )
                        else com.lsy.framelib.utils.StringUtils.getString(
                            R.string.over_time_prompt3
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
            startActivity(
                Intent(
                    this@OrderDetailActivity,
                    ScanOrderActivity::class.java
                ).apply {
                    putExtras(intent)
                })
            finish()
        }
        mBinding.tvOrderDetailAppointUse.setOnClickListener {
            mViewModel.changeUseModel.value = true
        }
    }

    override fun initData() {
        mViewModel.requestOrderDetailAsync()
    }

    override fun onDestroy() {
        mViewModel.timer?.cancel()
        super.onDestroy()
    }
}
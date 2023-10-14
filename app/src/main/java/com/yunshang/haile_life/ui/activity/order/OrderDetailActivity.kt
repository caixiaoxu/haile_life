package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.data.constants.Constants
import com.lsy.framelib.utils.DimensionUtils
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
import com.yunshang.haile_life.databinding.*
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.TranslucencePopupWindow
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

    private var popupWindow: TranslucencePopupWindow? = null

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
//                mViewModel.getOrderStatusVal(it)
                // 是否只有一个sku
                val isSingle = 1 == it.orderItemList.size
                // sku 列表
                val items = it.orderItemList.filter { item ->
                    !DeviceCategory.isDispenser(item.categoryCode)
                }
                mBinding.llOrderDetailSkus.buildChild<ItemOrderDetailSkuBinding, OrderItem>(items) { _, childBinding, data ->
                    childBinding.item = data
                    childBinding.isSingle = isSingle
                    childBinding.state = it.state
                }
                // 投放器的sku
                val dispenserList =
                    it.orderItemList.filter { item -> DeviceCategory.isDispenser(item.categoryCode) }
                if (dispenserList.isNotEmpty()) {
                    val dispenserBinding =
                        DataBindingUtil.inflate<ItemOrderDetailSkuDispenserBinding>(
                            LayoutInflater.from(this@OrderDetailActivity),
                            R.layout.item_order_detail_sku_dispenser,
                            null,
                            false
                        )
                    dispenserBinding.llItemOrderSkuDispenser.buildChild<ItemOrderDetailSkuGoodBinding, OrderItem>(
                        dispenserList
                    ) { _, childBinding, data ->
                        childBinding.type = 1
                        childBinding.title = data.goodsItemName
                        childBinding.num = data.num + "ml"
                        childBinding.price = StringUtils.formatAmountStrOfStr(data.originPrice)
                    }
                    try {
                        var discountVal = 0.0
                        dispenserList.forEach { item ->
                            discountVal += item.discountPrice.toDouble()
                        }
                        dispenserBinding.discount = StringUtils.formatAmountStr(-discountVal)
                        dispenserBinding.includeItemOrderDetailSkuGood.root.visibility =
                            View.VISIBLE
                    } catch (e: Exception) {
                        e.printStackTrace()
                        dispenserBinding.includeItemOrderDetailSkuGood.root.visibility = View.GONE
                    }
                    mBinding.llOrderDetailSkus.addView(
                        dispenserBinding.root,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
            }
        }

        LiveDataBus.with(BusEvents.PAY_OVERTIME_STATUS)?.observe(this) {
            mViewModel.requestOrderDetailAsync()
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS)?.observe(this) {
            mViewModel.requestOrderDetailAsync()
        }

        LiveDataBus.with(BusEvents.PROMPT_POPUP, Boolean::class.java)?.observe(this) {
            if (it) {
                val mPopupBinding =
                    PopupPromptBinding.inflate(LayoutInflater.from(this))
                popupWindow = TranslucencePopupWindow(
                    mPopupBinding.root, window,
                    DimensionUtils.dip2px(this@OrderDetailActivity, 210f),
                    alpha = 1f
                )
                mPopupBinding.llPopupPrompt.setContextBg(Color.parseColor("#45484A"))

                mPopupBinding.tvPromptContext.text =
                    if (DeviceCategory.isShoes(mViewModel.orderDetail.value?.orderItemList?.firstOrNull()?.categoryCode)
                    ) "订单结束后，请尽快取走鞋子\n鞋子面料、鞋子量等会影响运行时长，预计剩余时间仅供参考，请以实际时间为准！"
                    else "订单结束后，请尽快取走衣物\n衣物面料、衣物量等会影响运行时长，预计剩余时间仅供参考，请以实际时间为准！"
                popupWindow?.showAsDropDown(
                    mBinding.tvOrderDetailDesc,
                    mBinding.tvOrderDetailDesc.width - DimensionUtils.dip2px(
                        this@OrderDetailActivity,
                        34f
                    ), 0
                )
            } else {
                popupWindow?.dismiss()
            }
        }
    }

    override fun initView() {
        mBinding.tvOrderDetailDesc.movementMethod = LinkMovementMethod.getInstance()

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
                            detail.orderItemList,
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

        mBinding.tvOrderDetailDelete.setOnClickListener {
            mViewModel.deleteOrder() {
                finish()
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
        mViewModel.orderStateTimer?.cancel()
        super.onDestroy()
    }
}
package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.yunshang.haile_life.data.agruments.SearchSelectParam
import com.yunshang.haile_life.data.entities.GoodsScanEntity
import com.yunshang.haile_life.data.entities.OrderItem
import com.yunshang.haile_life.data.entities.PromotionParticipation
import com.yunshang.haile_life.data.extend.toRemove0Str
import com.yunshang.haile_life.databinding.ActivityOrderDetailBinding
import com.yunshang.haile_life.databinding.IncludeOrderInfoItemBinding
import com.yunshang.haile_life.databinding.PopupPromptBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.personal.FaultRepairsActivity
import com.yunshang.haile_life.ui.view.TranslucencePopupWindow
import com.yunshang.haile_life.ui.view.dialog.CommonBottomSheetDialog
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.string.StringUtils


class OrderDetailActivity :
    BaseBusinessActivity<ActivityOrderDetailBinding, OrderDetailViewModel>(
        OrderDetailViewModel::class.java,
        BR.vm
    ) {

    private var callPhone: String? = ""

    // 拨打电话权限
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                // 授权权限成功
                if (!callPhone.isNullOrEmpty()) {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$callPhone")))
                }
            } else {
                // 授权失败
                SToast.showToast(this@OrderDetailActivity, R.string.empty_permission)
            }
        }

    private var popupWindow: TranslucencePopupWindow? = null

    override fun layoutId(): Int = R.layout.activity_order_detail

    override fun backBtn(): View = mBinding.barOrderDetailTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.orderNo = IntentParams.OrderParams.parseOrderNo(intent)
        mViewModel.formScan.value = 1 == IntentParams.OrderParams.parseFormScan(intent)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.orderDetail.observe(this) { detail ->
            detail?.let {
                mBinding.includeOrderDetailInfo.llOrderInfoItems.buildChild<IncludeOrderInfoItemBinding, OrderItem>(
                    detail.orderItemList
                ) { index, childBinding, data ->
                    childBinding.title =
                        if (0 == index) com.lsy.framelib.utils.StringUtils.getString(R.string.service) + "：" else ""
                    childBinding.content =
                        "${data.goodsItemName} ${data.unit.toRemove0Str()}${data.unitValue}"
                    childBinding.tail =
                        StringUtils.formatAmountStrOfStr(data.originPrice)
                }
                mBinding.includeOrderDetailInfo.llOrderInfoPromotions.buildChild<IncludeOrderInfoItemBinding, PromotionParticipation>(
                    detail.promotionParticipationList
                ) { index, childBinding, data ->
                    childBinding.title =
                        if (0 == index) com.lsy.framelib.utils.StringUtils.getString(R.string.discounts) + "：" else ""
                    childBinding.content = data.promotionProductName
                    childBinding.tvOrderInfoItemTail.setTextColor(
                        ContextCompat.getColor(
                            this@OrderDetailActivity,
                            R.color.color_black_85
                        )
                    )
                    childBinding.tail = data.getOrderDeviceDiscountPrice()
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

        LiveDataBus.with(BusEvents.PAY_OVERTIME_STATUS)?.observe(this) {
            mViewModel.requestOrderDetailAsync()
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS)?.observe(this) {
            if ("300" == mViewModel.orderDetail.value?.orderType || 106 == mViewModel.orderDetail.value?.orderSubType) {
                finish()
            } else {
                mViewModel.requestOrderDetailAsync()
            }
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

        mBinding.tvOrderDetailRepairs.setOnClickListener {
            startActivity(Intent(this, FaultRepairsActivity::class.java).apply {
                putExtras(
                    IntentParams.FaultRepairsParams.pack(
                        GoodsScanEntity(
                            mViewModel.orderDetail.value?.orderItemList?.firstOrNull()?.goodsId,
                            mViewModel.orderDetail.value?.orderItemList?.firstOrNull()?.goodsName,
                            mViewModel.orderDetail.value?.orderItemList?.firstOrNull()?.categoryCode,
                            DeviceCategory.categoryName(mViewModel.orderDetail.value?.orderItemList?.firstOrNull()?.categoryCode),
                        )
                    )
                )
            })
        }

        mBinding.tvOrderDetailContact.setOnClickListener {
            mViewModel.orderDetail.value?.serviceTelephone?.let {
                val phoneList =
                    it.split(",").mapIndexed { index, phone -> SearchSelectParam(index, phone) }
                CommonBottomSheetDialog.Builder("", phoneList).apply {
                    selectModel = 1
                    onValueSureListener =
                        object : CommonBottomSheetDialog.OnValueSureListener<SearchSelectParam> {
                            override fun onValue(data: SearchSelectParam?) {
                                CommonDialog.Builder("是否拨打电话").apply {
                                    title =
                                        com.lsy.framelib.utils.StringUtils.getString(R.string.tip)
                                    negativeTxt =
                                        com.lsy.framelib.utils.StringUtils.getString(R.string.cancel)
                                    setPositiveButton(com.lsy.framelib.utils.StringUtils.getString(R.string.sure)) {
                                        callPhone = data?.name
                                        requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
                                    }
                                }.build().show(supportFragmentManager)
                            }
                        }
                }.build().show(supportFragmentManager)
            }
        }
        mBinding.tvOrderDetailPay.setOnClickListener {
            mViewModel.orderDetail.value?.let { detail ->
                startActivity(Intent(this@OrderDetailActivity, OrderPayActivity::class.java).apply {
                    putExtras(
                        IntentParams.OrderPayParams.pack(
                            detail.orderNo,
                            detail.orderType,
                            detail.orderSubType,
                            if (DeviceCategory.isDrinking(detail.orderItemList.firstOrNull()?.categoryCode)) "" else detail.invalidTime,
                            detail.realPrice,
                            detail.orderItemList,
                        )
                    )
                })
            }
        }

        mBinding.tvOrderDetailEvaluateGo.setOnClickListener {
            goEvaluate()
        }

        mBinding.tvOrderDetailEvaluate.setOnClickListener {
            if (true == mViewModel.evaluateStatus.value?.canFeedback) {
                goEvaluate()
            } else {
                startActivity(
                    Intent(
                        this@OrderDetailActivity,
                        EvaluateDetailsActivity::class.java
                    ).apply {
                        mViewModel.orderDetail.value?.let { details ->
                            putExtras(
                                IntentParams.OrderEvaluateDetailsParams.pack(
                                    details.id,
                                )
                            )
                        }
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
                    if (true == mViewModel.isAppoint.value && 1 != detail.timesOfRestart)
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
    }

    private fun goEvaluate() {
        startActivity(
            Intent(
                this@OrderDetailActivity,
                IssueEvaluateActivity::class.java
            ).apply {
                mViewModel.orderDetail.value?.let { details ->
                    putExtras(
                        IntentParams.OrderIssueEvaluateParams.pack(
                            details.id,
                            details.orderNo,
                            details.orderItemList.filter { item -> !DeviceCategory.isDispenser(item.categoryCode) }
                                .firstOrNull()?.goodsId,
                            details.buyerId,
                            details.sellerId,
                            mViewModel.orderDetail.value?.serviceTelephone?.split(",")?.first()
                        )
                    )
                }
            })
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
package com.yunshang.haile_life.ui.fragment

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StatusBarUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.AppointmentOrderVerifyViewModel
import com.yunshang.haile_life.business.vm.AppointmentOrderViewModel
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.OrderItem
import com.yunshang.haile_life.data.entities.PromotionParticipation
import com.yunshang.haile_life.data.entities.TradePreviewGoodItem
import com.yunshang.haile_life.databinding.*
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import com.yunshang.haile_life.ui.view.dialog.CommonDialog

class AppointmentOrderVerifyFragment :
    BaseBusinessFragment<FragmentAppointmentOrderVerifyBinding, AppointmentOrderVerifyViewModel>(
        AppointmentOrderVerifyViewModel::class.java, BR.vm
    ) {

    val mActivityViewModel by lazy {
        getActivityViewModelProvider(requireActivity())[AppointmentOrderViewModel::class.java]
    }

    override fun layoutId(): Int = R.layout.fragment_appointment_order_verify

    override fun backBtn(): View = mBinding.barAppointmentOrderVerifyTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()

        mActivityViewModel.orderDetails.observe(this) { detail ->
            detail?.let {
                detail.checkInfo?.invalidTimeStamp?.let { time ->
                    // 失效时间
                    mViewModel.validTime = time
                    mViewModel.checkValidTime()
                }

                val count = mBinding.includeOrderSpecs.llOrderSubmitGood.childCount
                if (count > 2) {
                    mBinding.includeOrderSpecs.llOrderSubmitGood.removeViews(0, count - 2)
                }
                val inflater = LayoutInflater.from(requireContext())
                if (detail.orderItemList.isNotEmpty()) {
                    val isSingle = 1 == detail.orderItemList.size
                    for (good in detail.orderItemList.filter { item ->
                        !DeviceCategory.isDispenser(
                            item.categoryCode
                        )
                    }.map { item ->
                        TradePreviewGoodItem(
                            item.discountPrice,
                            item.categoryCode,
                            item.goodsId,
                            item.goodsItemId,
                            item.goodsItemName,
                            item.goodsName,
                            item.num,
                            item.originPrice,
                            item.originUnitPrice,
                            item.realPrice,
                            item.realUnitPrice,
                        )
                    }) {
                        val childGoodBinding = DataBindingUtil.inflate<ItemOrderSubmitGoodBinding>(
                            inflater,
                            R.layout.item_order_submit_good,
                            null,
                            false
                        )
                        childGoodBinding.item = good
                        childGoodBinding.isSingle = isSingle
                        mBinding.includeOrderSpecs.llOrderSubmitGood.addView(
                            childGoodBinding.root,
                            (mBinding.includeOrderSpecs.llOrderSubmitGood.childCount - 2),
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
                    }

                    // 投放器数据
                    val dispenserList =
                        detail.orderItemList.filter { item -> DeviceCategory.isDispenser(item.categoryCode) }
                            .map { item ->
                                TradePreviewGoodItem(
                                    item.discountPrice,
                                    item.categoryCode,
                                    item.goodsId,
                                    item.goodsItemId,
                                    item.goodsItemName,
                                    item.goodsName,
                                    item.num,
                                    item.originPrice,
                                    item.originUnitPrice,
                                    item.realPrice,
                                    item.realUnitPrice,
                                )
                            }
                    if (dispenserList.isNotEmpty()) {
                        val childDispenserGoodBinding =
                            DataBindingUtil.inflate<ItemOrderSubmitGoodDispenserBinding>(
                                inflater,
                                R.layout.item_order_submit_good_dispenser,
                                null,
                                false
                            )
                        childDispenserGoodBinding.llOrderSubmitGoodDispenserItem.buildChild<ItemOrderSubmitGoodItemBinding, TradePreviewGoodItem>(
                            dispenserList
                        ) { _, childBinding, data ->
                            childBinding.title = data.goodsItemName + "${data.num}ml"
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
                        mBinding.includeOrderSpecs.llOrderSubmitGood.addView(
                            childDispenserGoodBinding.root,
                            (mBinding.includeOrderSpecs.llOrderSubmitGood.childCount - 2),
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        )
                    }
                }

                mBinding.includeOrderSpecs.llOrderGoodDiscounts.visibility(false)

                mBinding.tvAppointmentOrderVerifyDevice.text =
                    "验证码已发送至设备${detail.orderItemList.firstOrNull()?.goodsName ?: ""}"

                mBinding.includeOrderInfo.llOrderInfoItems.buildChild<IncludeOrderInfoItemBinding, OrderItem>(
                    detail.orderItemList
                ) { index, childBinding, data ->
                    childBinding.title =
                        if (0 == index) StringUtils.getString(R.string.service) + "：" else ""
                    childBinding.content = "${data.goodsItemName} ${data.unit}${data.unitValue}"
                    childBinding.tail =
                        com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(data.originPrice)
                }
                mBinding.includeOrderInfo.llOrderInfoPromotions.buildChild<IncludeOrderInfoItemBinding, PromotionParticipation>(
                    detail.promotionParticipationList
                ) { index, childBinding, data ->
                    childBinding.title =
                        if (0 == index) StringUtils.getString(R.string.discounts) + "：" else ""
                    childBinding.content = data.promotionProductName
                    childBinding.tail = data.getOrderDeviceDiscountPrice()
                }
            }
        }
    }

    override fun initView() {
        mBinding.avm = mActivityViewModel
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

        mBinding.tvAppointmentOrderVerifyPrompt.movementMethod = LinkMovementMethod.getInstance()
        mBinding.tvAppointmentOrderVerifyPrompt.highlightColor = Color.TRANSPARENT
        val promptPrefix = StringUtils.getString(R.string.verify_order_prompt)
        val content = "$promptPrefix 提醒取衣"
        mBinding.tvAppointmentOrderVerifyPrompt.text =
            com.yunshang.haile_life.utils.string.StringUtils.formatMultiStyleStr(
                content,
                arrayOf(
                    ForegroundColorSpan(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.color_00cee5
                        )
                    ),
                    object : ClickableSpan() {
                        override fun onClick(v: View) {
                            mViewModel.sendTakeSms(mActivityViewModel.orderNo) {
                                SToast.showToast(requireContext(), "已发送信息，提醒用户取衣")
                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            //去掉下划线
                            ds.isUnderlineText = false
                        }
                    }
                ), promptPrefix.length, content.length
            )

        mBinding.etAppointmentOrderVerify.setOnFocusChangeListener { _, hasFocus ->
            if (mBinding.etAppointmentOrderVerify.text.toString().trim().isEmpty()) {
                mBinding.etAppointmentOrderVerify.hint =
                    if (hasFocus) "" else StringUtils.getString(R.string.appointment_order_verify_code_prompt)
            }
        }

        mBinding.btnAppointmentOrderVerifyCancel.setOnClickListener {
            CommonDialog.Builder("是否结束订单？").apply {
                negativeTxt = StringUtils.getString(R.string.no)
                setPositiveButton(StringUtils.getString(R.string.yes)) {
                    mActivityViewModel.cancelOrder()
                }
            }.build().show(childFragmentManager)
        }

        mBinding.btnAppointmentOrderVerifyResend.setOnClickListener {
            mViewModel.sendVerifyCode(mActivityViewModel.orderNo) {
                SToast.showToast(requireContext(), "已重新下发验证码")
            }
        }
        mBinding.btnAppointmentOrderVerify.setOnClickListener {
            mViewModel.verifyDeviceCode(mActivityViewModel.orderNo) {
                SToast.showToast(requireContext(), "验证成功")
                Handler(Looper.getMainLooper()).postDelayed({
                    mActivityViewModel.requestData()
                }, 3000)
            }
        }
    }

    override fun initData() {
    }
}
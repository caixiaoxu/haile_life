package com.yunshang.haile_life.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StatusBarUtils
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.OrderExecuteViewModel
import com.yunshang.haile_life.business.vm.OrderStatusViewModel
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.agruments.SearchSelectParam
import com.yunshang.haile_life.data.entities.GoodsScanEntity
import com.yunshang.haile_life.data.entities.OrderItem
import com.yunshang.haile_life.data.entities.PromotionParticipation
import com.yunshang.haile_life.data.extend.toRemove0Str
import com.yunshang.haile_life.databinding.FragmentOrderExecuteBinding
import com.yunshang.haile_life.databinding.IncludeOrderInfoItemBinding
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.ui.activity.personal.FaultRepairsActivity
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import com.yunshang.haile_life.ui.view.dialog.CommonBottomSheetDialog
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.Hint3SecondDialog
import com.yunshang.haile_life.utils.DateTimeUtils

class OrderExecuteFragment :
    BaseBusinessFragment<FragmentOrderExecuteBinding, OrderExecuteViewModel>(
        OrderExecuteViewModel::class.java, BR.vm
    ) {

    private var callPhone: String? = ""

    val mActivityViewModel by lazy {
        getActivityViewModelProvider(requireActivity())[OrderStatusViewModel::class.java]
    }

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
                SToast.showToast(requireContext(), R.string.empty_permission)
            }
        }

    override fun layoutId(): Int = R.layout.fragment_order_execute

    override fun backBtn(): View = mBinding.barOrderExecuteTitle.getBackBtn()

    override fun onBackListener() {
        requireActivity().finish()
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            putExtras(IntentParams.DefaultPageParams.pack(3))
        })
    }

    override fun initEvent() {
        super.initEvent()

        mActivityViewModel.orderDetails.observe(this) { detail ->
            mBinding.refreshView.finishRefresh()
            detail?.let {
                mBinding.includeOrderInfo.llOrderInfoItems.buildChild<IncludeOrderInfoItemBinding, OrderItem>(
                    detail.orderItemList
                ) { index, childBinding, data ->
                    childBinding.title =
                        if (0 == index) StringUtils.getString(R.string.service) + "：" else ""
                    childBinding.content =
                        "${data.goodsItemName} ${data.unit.toRemove0Str()}${data.unitValue}"
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

                mBinding.tvOrderExecutePrompt.visibility(!DeviceCategory.isHair(detail.orderItemList.firstOrNull()?.categoryCode))
            }
        }

        mViewModel.remainingTime.observe(this) {
            if (it > 0) {
                mBinding.cdOrderExecuteTime.setData(mViewModel.totalTime, if (it > 0) it else 1)
            } else {
                mViewModel.checkOrderFinish(mActivityViewModel.orderNo)
            }
        }

        mViewModel.jump.observe(this) {
            mActivityViewModel.jump.value = 1
        }
    }

    override fun initView() {
        mBinding.avm = mActivityViewModel
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

        mBinding.refreshView.requestData = {
            mActivityViewModel.requestData()
        }

        mBinding.tvOrderExecuteCoerceDevice.setOnClickListener {
            CommonDialog.Builder("您确定要强制启动该设备？").apply {
                dialogBgResource = R.drawable.shape_dialog_bg
                negativeTxt = StringUtils.getString(R.string.cancel)
                setPositiveButton(StringUtils.getString(R.string.sure)) {
                    mViewModel.coerceDevice(
                        requireContext(),
                        mActivityViewModel.orderNo,
                        mActivityViewModel.orderDetails.value?.fulfillInfo?.fulfillingItem?.fulfillId
                    )
                }
            }.build().show(childFragmentManager)
        }

        mBinding.tvOrderExecuteDeviceRepairs.setOnClickListener {
            startActivity(Intent(requireContext(), FaultRepairsActivity::class.java).apply {
                putExtras(
                    IntentParams.FaultRepairsParams.pack(
                        GoodsScanEntity(
                            mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.goodsId,
                            mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.goodsName,
                            mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.categoryCode,
                            DeviceCategory.categoryName(mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.categoryCode),
                        )
                    )
                )
            })
        }

        mBinding.tvOrderExecuteContactShop.setOnClickListener {
            mActivityViewModel.orderDetails.value?.serviceTelephone?.let {
                val phoneList =
                    it.split(",").mapIndexed { index, phone -> SearchSelectParam(index, phone) }
                CommonBottomSheetDialog.Builder("", phoneList).apply {
                    selectModel = 1
                    onValueSureListener =
                        object : CommonBottomSheetDialog.OnValueSureListener<SearchSelectParam> {
                            override fun onValue(data: SearchSelectParam?) {
                                CommonDialog.Builder("是否拨打电话").apply {
                                    title = StringUtils.getString(R.string.tip)
                                    negativeTxt = StringUtils.getString(R.string.cancel)
                                    setPositiveButton(StringUtils.getString(R.string.sure)) {
                                        callPhone = data?.name
                                        requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
                                    }
                                }.build().show(childFragmentManager)
                            }
                        }
                }.build().show(childFragmentManager)
            }
        }

        mViewModel.totalTime = try {
            mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.unit?.toDouble()
                ?.toInt()
                ?.let {
                    it * 60
                } ?: 0
        } catch (e: Exception) {
            0
        }
        // 没有启动设备就启动
//        if (0 == mActivityViewModel.orderDetails.value?.fulfillInfo?.fulfill) {
//            mViewModel.remainingTime.value = mViewModel.totalTime
//            mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.let { firstItem ->
//                val isSpecialDevice = firstItem.spuCode == "04001030"
//                if ((!SPRepository.isNoAppointPrompt && !DeviceCategory.isHair(firstItem.categoryCode))
//                    || isSpecialDevice
//                ) {
//                    ScanOrderConfirmDialog.Builder(firstItem.categoryCode, isSpecialDevice, true) {
//                        startOrderDevice()
//                    }.build().show(childFragmentManager)
//                } else {
//                    startOrderDevice()
//                }
//            }
//        } else {
        DateTimeUtils.formatDateFromString(mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.finishTime)
            ?.let {
                val diff = ((it.time - System.currentTimeMillis()) / 1000).toInt()
                mViewModel.remainingTime.value = if (diff < 0) 0 else diff
                mViewModel.checkValidTime()
            }
//        }

        mBinding.includeOrderInfo.btnOrderInfoCancel.text =
            StringUtils.getString(R.string.finish_order)
        mBinding.includeOrderInfo.btnOrderInfoCancel.setOnClickListener {
            CommonDialog.Builder("是否结束订单？").apply {
                negativeTxt = StringUtils.getString(R.string.no)
                setPositiveButton(StringUtils.getString(R.string.yes)) {
                    mViewModel.finishOrder(mActivityViewModel.orderNo) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            mActivityViewModel.jump.postValue(1)
                        }, 1000)
                    }
                }
            }.build().show(childFragmentManager)
        }
    }

    private fun startOrderDevice() {
        mViewModel.startOrderDevice(mActivityViewModel.orderNo) {
            Hint3SecondDialog.Builder("设备已启动").apply {
                dialogBgResource = R.drawable.shape_dialog_bg
            }.build().show(childFragmentManager)

            Handler(Looper.getMainLooper()).postDelayed({
                mActivityViewModel.requestData()
            }, 2000)
        }
    }

    override fun initData() {
        if (!mActivityViewModel.isRefreshError) {
            mActivityViewModel.timerRefreshOrderDetails()
        }
    }
}
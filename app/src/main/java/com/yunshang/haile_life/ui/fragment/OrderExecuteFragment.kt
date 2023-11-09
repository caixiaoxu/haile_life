package com.yunshang.haile_life.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.lsy.framelib.utils.*
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.AppointmentOrderViewModel
import com.yunshang.haile_life.business.vm.OrderExecuteViewModel
import com.yunshang.haile_life.data.ActivityTag
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.FragmentOrderExecuteBinding
import com.yunshang.haile_life.ui.activity.order.OrderDetailActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.ScanOrderConfirmDialog
import com.yunshang.haile_life.utils.DateTimeUtils

class OrderExecuteFragment :
    BaseBusinessFragment<FragmentOrderExecuteBinding, OrderExecuteViewModel>(
        OrderExecuteViewModel::class.java, BR.vm
    ) {

    val mActivityViewModel by lazy {
        getActivityViewModelProvider(requireActivity())[AppointmentOrderViewModel::class.java]
    }

    // 拨打电话权限
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                // 授权权限成功
                mActivityViewModel.orderDetails.value?.serviceTelephone?.let {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$it")))
                }
            } else {
                // 授权失败
                SToast.showToast(requireContext(), R.string.empty_permission)
            }
        }

    override fun layoutId(): Int = R.layout.fragment_order_execute

    override fun backBtn(): View = mBinding.barOrderExecuteTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()

        mViewModel.remainingTime.observe(this) {
            mBinding.cdOrderExecuteTime.setData(mViewModel.totalTime, it)
        }

        mViewModel.jump.observe(this) {
            when (it) {
                1 -> {
                    startActivity(
                        Intent(
                            requireContext(),
                            OrderDetailActivity::class.java
                        ).apply {
                            mActivityViewModel.orderNo?.let { orderNo ->
                                putExtras(IntentParams.OrderParams.pack(orderNo))
                            }
                        })
                    AppManager.finishAllActivityForTag(ActivityTag.TAG_ORDER_PAY)
                }
            }
        }
    }

    override fun initView() {
        mBinding.avm = mActivityViewModel
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

        mBinding.tvOrderExecuteCoerceDevice.setOnClickListener {
            mViewModel.coerceDevice(requireContext(), mActivityViewModel.orderNo)
        }

        mBinding.tvOrderExecuteContactShop.setOnClickListener {
            requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
        }

        mBinding.tvOrderExecuteFinishOrder.setOnClickListener {
            CommonDialog.Builder("是否结束订单？").apply {
                negativeTxt = StringUtils.getString(R.string.no)
                setPositiveButton(StringUtils.getString(R.string.yes)) {
                    mViewModel.finishOrder(mActivityViewModel.orderNo)
                }
            }.build().show(childFragmentManager)
        }

        // 没有启动设备就启动
        if (0 == mActivityViewModel.orderDetails.value?.fulfillInfo?.fulfill) {
            mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.let { firstItem ->
                val isSpecialDevice = firstItem.spuCode == "04001030"
                if ((!SPRepository.isNoPrompt && !DeviceCategory.isHair(firstItem.categoryCode))
                    || isSpecialDevice
                ) {
                    ScanOrderConfirmDialog.Builder(firstItem.categoryCode, isSpecialDevice, true) {
                        startOrderDevice()
                    }.build().show(childFragmentManager)
                } else {
                    startOrderDevice()
                }
            }
        } else {
            DateTimeUtils.formatDateFromString(mActivityViewModel.orderDetails.value?.orderItemList?.firstOrNull()?.finishTime)
                ?.let {
                    mViewModel.totalTime = ((it.time - System.currentTimeMillis()) / 1000).toInt()
                    mViewModel.remainingTime.value = mViewModel.totalTime
                    mViewModel.checkValidTime()
                }
        }
    }

    private fun startOrderDevice() {
        mViewModel.startOrderDevice(mActivityViewModel.orderNo) {
            Handler(Looper.getMainLooper()).postDelayed({
                mActivityViewModel.requestData()
            }, 2000)
        }
    }

    override fun initData() {
    }
}
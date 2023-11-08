package com.yunshang.haile_life.ui.fragment

import android.content.Intent
import android.view.View
import com.lsy.framelib.utils.StatusBarUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.AppointmentOrderVerifyViewModel
import com.yunshang.haile_life.business.vm.AppointmentOrderViewModel
import com.yunshang.haile_life.databinding.FragmentAppointmentOrderVerifyBinding
import com.yunshang.haile_life.ui.activity.order.OrderExecuteActivity
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
    }

    override fun initView() {
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

        mBinding.btnAppointmentOrderVerifyCancel.setOnClickListener {
            CommonDialog.Builder("是否结束订单？").apply {
                negativeTxt = StringUtils.getString(R.string.no)
                setPositiveButton(StringUtils.getString(R.string.yes)) {
                    mActivityViewModel.cancelOrder()
                }
            }.build().show(childFragmentManager)
        }

        mBinding.btnAppointmentOrderVerify.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    OrderExecuteActivity::class.java
                )
            )
        }
    }

    override fun initData() {
    }
}
package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.AppointmentSuccessViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityAppointmentSuccessBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class AppointmentSuccessActivity :
    BaseBusinessActivity<ActivityAppointmentSuccessBinding, AppointmentSuccessViewModel>(
        AppointmentSuccessViewModel::class.java, BR.vm
    ) {

    override fun isFullScreen(): Boolean = true

    override fun layoutId(): Int = R.layout.activity_appointment_success

    override fun backBtn(): View = mBinding.barAppointmentSuccessTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.orderNo = IntentParams.OrderParams.parseOrderNo(intent)
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.jump.observe(this) {
            when (it) {
                1 -> {
                    startActivity(
                        Intent(
                            this@AppointmentSuccessActivity,
                            OrderDetailActivity::class.java
                        )
                    )
                    finish()
                }
            }
        }
        mViewModel.orderDetails.observe(this) {

        }
    }

    override fun initView() {
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)
        mBinding.includeAppointmentDeviceStatus.root.setBackgroundResource(R.drawable.shape_sffffff_r8)
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
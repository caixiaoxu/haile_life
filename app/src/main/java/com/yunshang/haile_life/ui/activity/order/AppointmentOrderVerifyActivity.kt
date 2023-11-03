package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.view.View
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.AppointmentOrderVerifyViewModel
import com.yunshang.haile_life.databinding.ActivityAppointmentOrderVerifyBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class AppointmentOrderVerifyActivity :
    BaseBusinessActivity<ActivityAppointmentOrderVerifyBinding, AppointmentOrderVerifyViewModel>(
        AppointmentOrderVerifyViewModel::class.java, BR.vm
    ) {

    override fun isFullScreen(): Boolean = true

    override fun layoutId(): Int = R.layout.activity_appointment_order_verify

    override fun backBtn(): View = mBinding.barAppointmentOrderVerifyTitle.getBackBtn()

    override fun initView() {
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

        mBinding.btnAppointmentOrderVerify.setOnClickListener {
            startActivity(Intent(this@AppointmentOrderVerifyActivity,OrderExecuteActivity::class.java))
        }
    }

    override fun initData() {
    }
}
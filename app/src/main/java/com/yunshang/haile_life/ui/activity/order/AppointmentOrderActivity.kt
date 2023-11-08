package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import androidx.fragment.app.Fragment
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.AppointmentOrderViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityAppointmentOrderBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.fragment.AppointmentOrderSubmitFragment
import com.yunshang.haile_life.ui.fragment.AppointmentOrderVerifyFragment
import com.yunshang.haile_life.ui.fragment.AppointmentSuccessFragment

class AppointmentOrderActivity :
    BaseBusinessActivity<ActivityAppointmentOrderBinding, AppointmentOrderViewModel>(
        AppointmentOrderViewModel::class.java
    ) {

    override fun isFullScreen(): Boolean = true

    override fun layoutId(): Int = R.layout.activity_appointment_order

    override fun initIntent() {
        super.initIntent()
        mViewModel.orderNo = IntentParams.OrderParams.parseOrderNo(intent)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.orderDetails.observe(this) { detail ->
            detail?.let {
                when (detail.orderSubType) {
                    106 -> {
                        if (50 == detail.state) {
                            when (detail.checkInfo?.checkState) {
                                1 -> {
                                    // 待验证
                                    showAppointmentPage(AppointmentOrderVerifyFragment())
                                }
                                2 -> {
                                    // 支付
                                    showAppointmentPage(AppointmentOrderSubmitFragment())
                                }
                                else -> goToNormalOrderPage(detail.orderNo)
                            }
                        } else goToNormalOrderPage(detail.orderNo)
                    }
                    301 -> {
                        if (500 == detail.state) {
                            if (1 == detail.appointmentState) {
                                // 预约成功
                                showAppointmentPage(AppointmentSuccessFragment())
                            } else if (5 == detail.appointmentState && 1 == detail.checkInfo?.checkState) {
                                // 待验证
                                showAppointmentPage(AppointmentOrderVerifyFragment())
                            } else goToNormalOrderPage(detail.orderNo)
                        } else if (50 == detail.state) {
                            // 待支付
                            showAppointmentPage(AppointmentOrderSubmitFragment())
                        } else goToNormalOrderPage(detail.orderNo)
                    }
                    303 -> {
                        if (50 == detail.state) {
                            // 待支付
                            when (detail.appointmentState) {
                                1 -> {
                                    // 预约成功
                                    showAppointmentPage(AppointmentSuccessFragment())
                                }
                                5 -> {
                                    when (detail.checkInfo?.checkState) {
                                        1 -> {
                                            // 待验证
                                            showAppointmentPage(AppointmentOrderVerifyFragment())
                                        }
                                        2 -> {
                                            // 待支付
                                            showAppointmentPage(AppointmentOrderSubmitFragment())
                                        }
                                        else -> goToNormalOrderPage(detail.orderNo)
                                    }
                                }
                                else -> goToNormalOrderPage(detail.orderNo)
                            }
                        } else goToNormalOrderPage(detail.orderNo)
                    }
                    else -> {
                        goToNormalOrderPage(detail.orderNo)
                    }
                }
            }
        }

        mViewModel.jump.observe(this) {
            when (it) {
                1 -> mViewModel.orderNo?.let { orderNo -> goToNormalOrderPage(orderNo) }
            }
        }
    }

    private fun goToNormalOrderPage(orderNo: String) {
        startActivity(
            Intent(
                this@AppointmentOrderActivity,
                OrderDetailActivity::class.java
            ).apply {
                putExtras(
                    IntentParams.OrderParams.pack(
                        orderNo,
                    )
                )
            })
        finish()
    }

    private fun showAppointmentPage(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_appointment_order_control, fragment)
            .commit()
    }

    override fun initView() {
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
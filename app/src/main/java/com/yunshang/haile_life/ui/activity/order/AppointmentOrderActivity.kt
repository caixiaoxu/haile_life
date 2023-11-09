package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import androidx.fragment.app.Fragment
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.AppManager
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.AppointmentOrderViewModel
import com.yunshang.haile_life.data.ActivityTag
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.WxPrePayEntity
import com.yunshang.haile_life.databinding.ActivityAppointmentOrderBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.fragment.AppointmentOrderSubmitFragment
import com.yunshang.haile_life.ui.fragment.AppointmentOrderVerifyFragment
import com.yunshang.haile_life.ui.fragment.AppointmentSuccessFragment
import com.yunshang.haile_life.ui.fragment.OrderExecuteFragment
import com.yunshang.haile_life.utils.thrid.WeChatHelper

class AppointmentOrderActivity :
    BaseBusinessActivity<ActivityAppointmentOrderBinding, AppointmentOrderViewModel>(
        AppointmentOrderViewModel::class.java
    ) {

    override fun activityTag(): String = ActivityTag.TAG_ORDER_PAY

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
                if (true == detail.redirectWorking) {
                    // 待执行
                    showAppointmentPage(OrderExecuteFragment())
                } else if ((301 == detail.orderSubType && 500 == detail.state && 1 == detail.appointmentState)// 后付费
                    || (303 == detail.orderSubType && 50 == detail.state && 1 == detail.appointmentState)// 先付费
                ) {
                    // 预约成功
                    showAppointmentPage(AppointmentSuccessFragment())
                } else if ((106 == detail.orderSubType && 50 == detail.state && 1 == detail.checkInfo?.checkState)// 预定订单
                    || (301 == detail.orderSubType && 500 == detail.state && 5 == detail.appointmentState && 1 == detail.checkInfo?.checkState) // 后付费
                    || (303 == detail.orderSubType && 50 == detail.state && 5 == detail.appointmentState && 1 == detail.checkInfo?.checkState) // 先付费
                ) {
                    // 待验证
                    showAppointmentPage(AppointmentOrderVerifyFragment())
                } else if ((106 == detail.orderSubType && 50 == detail.state && 2 == detail.checkInfo?.checkState) // 预定订单
                    || (301 == detail.orderSubType && 50 == detail.state)//后付费
                    || (303 == detail.orderSubType && 50 == detail.state && 5 == detail.appointmentState && 2 == detail.checkInfo?.checkState) // 先付费
                ) {
                    // 待支付
                    showAppointmentPage(AppointmentOrderSubmitFragment())
                } else goToNormalOrderPage(detail.orderNo)
            }
        }

        mViewModel.prepayParam.observe(this) {
            it?.let {
                if (103 == mViewModel.payMethod) {
                    mViewModel.alipay(this, it)
                } else if (203 == mViewModel.payMethod) {
                    GsonUtils.json2Class(it, WxPrePayEntity::class.java)?.let { wxPrePayBean ->
                        WeChatHelper.openWeChatPay(
                            wxPrePayBean.appId,
                            wxPrePayBean.partnerId,
                            wxPrePayBean.prepayId,
                            wxPrePayBean.nonceStr,
                            wxPrePayBean.timeStamp,
                            wxPrePayBean.paySign

                        )
                    }
                }
            }
        }

        LiveDataBus.with(BusEvents.WXPAY_STATUS)?.observe(this) {
            mViewModel.requestAsyncPayAsync()
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS, Boolean::class.java)?.observe(this) {
            if (it) {
                mViewModel.requestData()
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
        AppManager.finishAllActivityForTag(ActivityTag.TAG_ORDER_PAY)
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
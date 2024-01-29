package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.AppManager
import com.lsy.framelib.utils.gson.GsonUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderStatusViewModel
import com.yunshang.haile_life.data.ActivityTag
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.WxPrePayEntity
import com.yunshang.haile_life.databinding.ActivityOrderStatusBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.ui.fragment.*
import com.yunshang.haile_life.ui.view.dialog.OfficialAccountsDialog
import com.yunshang.haile_life.ui.view.dialog.ShopActivitiesDialog
import com.yunshang.haile_life.utils.thrid.WeChatHelper

class OrderStatusActivity :
    BaseBusinessActivity<ActivityOrderStatusBinding, OrderStatusViewModel>(
        OrderStatusViewModel::class.java
    ) {

    override fun activityTag(): String = ActivityTag.TAG_ORDER_PAY

    override fun isFullScreen(): Boolean = true

    override fun layoutId(): Int = R.layout.activity_order_status

    override fun onBackListener() {
        super.onBackListener()
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtras(IntentParams.DefaultPageParams.pack(3))
        })
    }

    override fun initIntent() {
        super.initIntent()
        mViewModel.orderNo = IntentParams.OrderParams.parseOrderNo(intent)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.orderDetails.observe(this) { detail ->
            detail?.let {
                if (true == detail.redirectWorking) {
                    if (0 == detail.fulfillInfo?.fulfill || 1 == detail.fulfillInfo?.fulfill) {
                        // 桶自洁
                        showAppointmentPage(DeviceSelfCleaningFragment())
                    } else {
                        // 待执行
                        showAppointmentPage(OrderExecuteFragment())
                        if (1 != mViewModel.statusType) {
                            requestOfficialAccounts()
                            requestShopActivity(300)
                        }
                        mViewModel.statusType = 1
                    }
                } else if ((301 == detail.orderSubType && 500 == detail.state && 1 == detail.appointmentState)// 先付费
                    || (303 == detail.orderSubType && 50 == detail.state && 1 == detail.appointmentState)// 后付费
                ) {
                    // 预约成功
                    showAppointmentPage(AppointmentSuccessFragment())
                    if (2 != mViewModel.statusType) {
                        requestOfficialAccounts()
                    }
                    mViewModel.statusType = 2
                } else if (((101 == detail.orderSubType || 106 == detail.orderSubType) && 50 == detail.state && 1 == detail.checkInfo?.checkState)// 预定订单
                    || (301 == detail.orderSubType && 500 == detail.state && 5 == detail.appointmentState && 1 == detail.checkInfo?.checkState) // 先付费
                    || (303 == detail.orderSubType && 50 == detail.state && 5 == detail.appointmentState && 1 == detail.checkInfo?.checkState) // 后付费
                ) {
                    // 待验证
                    showAppointmentPage(AppointmentOrderVerifyFragment())
                    requestOfficialAccounts()
                    if (!(2 == mViewModel.statusType || 3 == mViewModel.statusType)) {
                        requestOfficialAccounts()
                    }
                    mViewModel.statusType = 3
                } else if (((101 == detail.orderSubType || 106 == detail.orderSubType) && 50 == detail.state && 2 == detail.checkInfo?.checkState) // 预定订单
                    || (301 == detail.orderSubType && 50 == detail.state)// 先付费
                    || (303 == detail.orderSubType && 50 == detail.state && 5 == detail.appointmentState && 2 == detail.checkInfo?.checkState) //后付费
                ) {
                    // 待支付
                    showAppointmentPage(OrderPaySubmitFragment())
                    if (4 != mViewModel.statusType) {
                        requestShopActivity(200)
                    }
                    mViewModel.statusType = 4
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
                            wxPrePayBean.parentId,
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
                Handler(Looper.getMainLooper()).postDelayed({
                    mViewModel.requestData()
                }, 1000)
            }
        }

        mViewModel.jump.observe(this) {
            when (it) {
                1 -> mViewModel.orderNo?.let { orderNo -> goToNormalOrderPage(orderNo) }
            }
        }
    }

    private fun requestOfficialAccounts() {
        mViewModel.requestOfficialAccounts() {
            if (!it.flag) {
                OfficialAccountsDialog(it).show(supportFragmentManager, "OfficialAccounts")
            }
        }
    }

    private fun requestShopActivity(activityExecuteNodeId: Int) {
        // 是否有活动
        mViewModel.requestShopActivity(activityExecuteNodeId) {
            it?.let {
                ShopActivitiesDialog.Builder(it, 200, orderNo = mViewModel.orderNo) {
                    if (200 == activityExecuteNodeId) {
                        mViewModel.requestPreviewASync()
                    }
                }.build()
                    .show(supportFragmentManager)
            }
        }
    }

    private fun goToNormalOrderPage(orderNo: String) {
        startActivity(
            Intent(
                this@OrderStatusActivity,
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
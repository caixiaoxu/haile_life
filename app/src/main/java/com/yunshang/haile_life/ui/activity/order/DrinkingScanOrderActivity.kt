package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.ScreenUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.DrinkingScanOrderViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.DeviceDetailItemEntity
import com.yunshang.haile_life.databinding.ActivityDrinkingScanOrderBinding
import com.yunshang.haile_life.databinding.ItemDrinkingScanOrderBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.ShopActivitiesDialog
import com.yunshang.haile_life.ui.view.dialog.ShopNoticeDialog

class DrinkingScanOrderActivity :
    BaseBusinessActivity<ActivityDrinkingScanOrderBinding, DrinkingScanOrderViewModel>(
        DrinkingScanOrderViewModel::class.java, BR.vm
    ) {

    override fun layoutId(): Int = R.layout.activity_drinking_scan_order

    override fun backBtn(): View = mBinding.barDrinkingScanOrderTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()

        IntentParams.ScanOrderParams.parseGoodsScan(intent)?.let {
            mViewModel.goodsScan.value = it
        }

        IntentParams.ScanOrderParams.parseDeviceDetail(intent)?.let {
            mViewModel.deviceDetail.value = it
        }
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.unPayOrderNo.observe(this) { orderNo ->
            if (!orderNo.isNullOrEmpty())
                CommonDialog.Builder("您有未支付订单，请支付后再使用").apply {
                    title = "支付提醒"
                    setNegativeButton(StringUtils.getString(R.string.cancel)) {
                        finish()
                    }
                    setPositiveButton(StringUtils.getString(R.string.go_pay)) {
                        goOrderDetail(orderNo)
                    }
                }.build().show(supportFragmentManager)
        }

        mViewModel.deviceDetail.observe(this) {
            it?.let {
                val list = it.items.filter { item -> 1 == item.soldState }
                val itemCount = list.size

                mBinding.llDrinkingScanOrderConfigure.buildChild<ItemDrinkingScanOrderBinding, DeviceDetailItemEntity>(
                    list,
                    if (1 == itemCount) LinearLayoutCompat.LayoutParams(
                        ScreenUtils.screenWidth - 2 * DimensionUtils.dip2px(
                            this@DrinkingScanOrderActivity,
                            12f
                        ),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ) else LinearLayoutCompat.LayoutParams(
                        ScreenUtils.screenWidth / 2 - DimensionUtils.dip2px(
                            this@DrinkingScanOrderActivity,
                            12f
                        ),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                ) { _, childBinding, data ->
                    childBinding.item = data
                    childBinding.isShower = false == mViewModel.isDrinking.value
                }
            }
        }


        // 店铺公告
        mViewModel.shopNotice.observe(this) {
            if (!it.isNullOrEmpty()) {
                ShopNoticeDialog(it).show(supportFragmentManager)
            }
        }

        // 是否有活动
        mViewModel.shopActivity.observe(this) {
            it?.let {
                ShopActivitiesDialog.Builder(it, 100, mViewModel.goodsScan.value?.activityHashKey)
                    .build().show(supportFragmentManager)
            }
        }
    }

    override fun initView() {
        mBinding.btnDrinkingScanOrderUse.setOnClickListener {
            mViewModel.createOrder { orderNo ->
                goOrderDetail(orderNo)
            }
        }
    }

    /**
     * 跳转订单详情
     */
    private fun goOrderDetail(orderNo: String) {
        startActivity(
            Intent(
                this@DrinkingScanOrderActivity,
                OrderDetailActivity::class.java
            ).apply {
                putExtras(IntentParams.OrderParams.pack(orderNo, false))
            })
        finish()
    }

    override fun initData() {
        mViewModel.requestData(IntentParams.ScanOrderParams.parseCode(intent))
    }
}
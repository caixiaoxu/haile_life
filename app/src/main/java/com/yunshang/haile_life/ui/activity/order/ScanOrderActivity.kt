package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.ViewUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.ScanOrderViewModel
import com.yunshang.haile_life.data.ActivityTag
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.ExtAttrBean
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityScanOrderBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelItemBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.shop.RechargeStarfishActivity
import com.yunshang.haile_life.ui.view.ClickRadioButton
import com.yunshang.haile_life.ui.view.dialog.ScanOrderConfirmDialog

class ScanOrderActivity : BaseBusinessActivity<ActivityScanOrderBinding, ScanOrderViewModel>(
    ScanOrderViewModel::class.java, BR.vm
) {
    override fun layoutId(): Int = R.layout.activity_scan_order

    override fun activityTag(): String = ActivityTag.TAG_ORDER_PAY

    override fun backBtn(): View = mBinding.barScanOrderTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()

        IntentParams.ScanOrderParams.parseGoodsScan(intent)?.let {
            mViewModel.goodsScan.value = it
        }
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.deviceConfigs.observe(this) { configs ->
            if (configs.isNotEmpty()) {
                mBinding.includeScanOrderConfig.clScanOrderConfig.let { cl ->
                    if (cl.childCount > 3) {
                        cl.removeViews(3, cl.childCount - 3)
                    }
                    val inflater = LayoutInflater.from(this@ScanOrderActivity)
                    configs.forEachIndexed { index, item ->
                        DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                            inflater, R.layout.item_scan_order_model_item, null, false
                        )?.let { itemBinding ->
                            mViewModel.goodsScan.value?.categoryCode?.let {
                                itemBinding.code = it
                            }
                            itemBinding.item = item
                            (itemBinding.root as ClickRadioButton).let { rb ->
                                rb.id = index + 1

                                if (DeviceCategory.isHair(mViewModel.goodsScan.value?.categoryCode)
                                    && 0 == item.amount
                                ) {
                                    rb.setTextColor(
                                        ContextCompat.getColor(
                                            this@ScanOrderActivity,
                                            R.color.color_black_25
                                        )
                                    )
                                    rb.setOnRadioClickListener { true }
                                }

                                mViewModel.selectDeviceConfig.observe(this) {
                                    (item.id == it.id).let { isSame ->
                                        if (isSame != rb.isChecked) {
                                            rb.isChecked = isSame
                                        }
                                    }
                                }
                                rb.setOnClickListener {
                                    mViewModel.selectDeviceConfig.value = item
                                    mViewModel.goodsScan.value?.categoryCode?.let { code ->
                                        item.getExtAttrs(DeviceCategory.isDryerOrHair(code))
                                            .firstOrNull()?.let { firstAttr ->
                                                mViewModel.selectExtAttr.postValue(firstAttr)
                                            }
                                    }
                                }
                            }
                            cl.addView(
                                itemBinding.root,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }
                    // 设置id
                    val idList = IntArray(configs.size) { it + 1 }
                    mBinding.includeScanOrderConfig.flowScanOrderItem.referencedIds = idList
                }
            }
        }

        mViewModel.selectDeviceConfig.observe(this) {
            mViewModel.selectExtAttr.value = null
            mBinding.includeScanOrderTime.clScanOrderConfig.let { cl ->
                if (cl.childCount > 3) {
                    cl.removeViews(3, cl.childCount - 3)
                }

                val inflater = LayoutInflater.from(this@ScanOrderActivity)

                val list: List<ExtAttrBean> =
                    it.getExtAttrs(DeviceCategory.isDryerOrHair(mViewModel.goodsScan.value!!.categoryCode))
                if (list.isNotEmpty()) {
                    list.forEachIndexed { index, item ->
                        DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                            inflater, R.layout.item_scan_order_model_item, null, false
                        )?.let { itemBinding ->
                            mViewModel.goodsScan.value?.categoryCode?.let {
                                itemBinding.code = it
                            }
                            itemBinding.item = item
                            (itemBinding.root as AppCompatRadioButton).let { rb ->
                                rb.id = index + 1

                                mViewModel.selectExtAttr.observe(this) {
                                    (item.minutes == it?.minutes).let { isSame ->
                                        if (isSame != rb.isChecked) {
                                            rb.isChecked = isSame
                                        }
                                    }
                                }
                                rb.setOnClickListener {
                                    mViewModel.selectExtAttr.value = item
                                }
                            }
                            cl.addView(
                                itemBinding.root,
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }
                    // 设置id
                    val idList = IntArray(list.size) { it + 1 }
                    mBinding.includeScanOrderTime.flowScanOrderItem.referencedIds = idList
                }
            }
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS)?.observe(this) {
            finish()
        }

        LiveDataBus.with(BusEvents.APPOINT_ORDER_USE_STATUS)?.observe(this) {
            finish()
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.viewScanOrderSelected.setOnClickListener {
            if (!ViewUtils.isFastDoubleClick()) {
                if (null == mViewModel.selectDeviceConfig.value) {
                    SToast.showToast(this@ScanOrderActivity, "请先选择设备模式")
                    return@setOnClickListener
                }

                if (null == mViewModel.selectExtAttr.value) {
                    SToast.showToast(this@ScanOrderActivity, "请先选择运行配置")
                    return@setOnClickListener
                }

                if (!SPRepository.isNoPrompt && null != mViewModel.goodsScan.value
                    && !DeviceCategory.isHair(mViewModel.goodsScan.value!!.categoryCode)
                ) {
                    ScanOrderConfirmDialog.Builder(mViewModel.goodsScan.value!!.categoryCode) {
                        jumpOrderSubmit()
                    }.build().show(supportFragmentManager)
                } else {
                    jumpOrderSubmit()
                }
            }
        }

        mBinding.clScanRechargeStarfish.setOnClickListener {
            mViewModel.goodsScan.value?.let {
                startActivity(
                    Intent(
                        this@ScanOrderActivity,
                        RechargeStarfishActivity::class.java
                    ).apply {
                        putExtras(IntentParams.RechargeStarfishParams.pack(it.shopId))
                    })
            }
        }
    }

    /**
     * 跳转到订单提交页
     */
    private fun jumpOrderSubmit() {
        val categoryCode = mViewModel.goodsScan.value?.categoryCode ?: return
        val goodId = mViewModel.goodsScan.value?.goodsId
        if (null == goodId || goodId <= 0) {
            return
        }
        val goodItemId = mViewModel.selectDeviceConfig.value?.id
        if (null == goodItemId || goodItemId <= 0) {
            return
        }
        val num =
            (if (true == mViewModel.isDryer.value) mViewModel.selectExtAttr.value?.minutes else 1)
                ?: return
        startActivity(Intent(this, OrderSubmitActivity::class.java).apply {
            putExtras(
                IntentParams.OrderSubmitParams.pack(
                    listOf(
                        IntentParams.OrderSubmitParams.OrderSubmitGood(
                            categoryCode,
                            goodId,
                            goodItemId,
                            "$num"
                        )
                    )
                )
            )
        })
    }

    override fun initData() {
        mViewModel.requestData(IntentParams.ScanOrderParams.parseCode(intent))
    }
}
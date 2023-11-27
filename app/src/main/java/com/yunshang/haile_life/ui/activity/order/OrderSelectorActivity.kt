package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderSelectorViewModel
import com.yunshang.haile_life.data.ActivityTag
import com.yunshang.haile_life.data.Constants
import com.yunshang.haile_life.data.agruments.NewOrderParams
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.agruments.Purchase
import com.yunshang.haile_life.data.entities.DeviceDetailEntity
import com.yunshang.haile_life.data.entities.DeviceDetailItemEntity
import com.yunshang.haile_life.data.entities.DeviceStateEntity
import com.yunshang.haile_life.data.entities.ExtAttrDtoItem
import com.yunshang.haile_life.databinding.ActivityOrderSelectorBinding
import com.yunshang.haile_life.databinding.ItemDeviceStatusProgressBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelItemBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import com.yunshang.haile_life.web.WebViewActivity

class OrderSelectorActivity :
    BaseBusinessActivity<ActivityOrderSelectorBinding, OrderSelectorViewModel>(
        OrderSelectorViewModel::class.java, BR.vm
    ) {

    override fun activityTag(): String = ActivityTag.TAG_ORDER_PAY

    override fun layoutId(): Int = R.layout.activity_order_selector

    override fun backBtn(): View = mBinding.barSelectorTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.deviceId = IntentParams.DeviceParams.parseDeviceId(intent)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.isHideDeviceInfo.observe(this) {
            ContextCompat.getDrawable(
                this@OrderSelectorActivity,
                if (it) R.mipmap.icon_info_open else R.mipmap.icon_info_hide
            )?.let { draw ->
                mBinding.includeScanOrderDeviceInfo.ibScanOrderDeviceInfoToggle.setImageDrawable(
                    draw
                )
            }
        }

        // 进度状态
        mViewModel.stateList.observe(this) { stateList ->
            val size = stateList?.let { it.size - 1 } ?: 0
            mBinding.includeDeviceStatus.includeAppointmentDeviceStatusProgress.llDeviceStateProgress.buildChild<ItemDeviceStatusProgressBinding, DeviceStateEntity>(
                stateList,
                LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ).apply {
                    weight = 1f
                }
            ) { index, childBinding, data ->
                childBinding.isDryer =
                    false//DeviceCategory.isDryer(mViewModel.deviceDetail.value?.categoryCode)
                childBinding.isFirst = 0 == index
                childBinding.isLast = index == size
                childBinding.item = data
            }
        }

        val inflater = LayoutInflater.from(this@OrderSelectorActivity)

        // 模式和关联
        mViewModel.deviceDetail.observe(this) { detail ->
            mBinding.includeDeviceStatus.root.visibility(1 != detail.deviceState)
            detail.items.filter { item -> 1 == item.soldState }.let { configs ->
                if (configs.isNotEmpty()) {
                    mBinding.includeSelectorSpec.clScanOrderConfig.let { cl ->
                        if (cl.childCount > 3) {
                            cl.removeViews(3, cl.childCount - 3)
                        }
                        configs.forEachIndexed { index, item ->
                            DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                                inflater, R.layout.item_scan_order_model_item, null, false
                            )?.let { itemBinding ->
                                itemBinding.code = detail.categoryCode
                                itemBinding.item = item
                                itemBinding.root.id = index + 1
                                itemBinding.rbOrderModelItem.let { rb ->
                                    mViewModel.selectDeviceConfig.observe(this) {
                                        (item.id == it.id).let { isSame ->
                                            if (isSame != rb.isChecked) {
                                                rb.isChecked = isSame
                                            }
                                        }
                                    }
                                    rb.setOnClickListener {
                                        mViewModel.selectDeviceConfig.value = item
                                        mViewModel.changeDeviceConfig(item)
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
                        mBinding.includeSelectorSpec.flowScanOrderItem.referencedIds = idList
                        mBinding.includeSelectorSpec.flowScanOrderItem.visibility =
                            View.VISIBLE
                    }
                }
            }
            buildAttachSkuView(detail)

            // 下单按钮内容
            mBinding.tvOrderSelectSubmitSelected.text = StringUtils.getString(
                if (1 == detail.deviceState) R.string.order_now
                else if (true == detail.enableReserve && 1 == detail.reserveState) R.string.advance_reservation
                else R.string.can_not_appointment1
            )
        }

        // 时长
        mViewModel.selectDeviceConfig.observe(this) {
            mBinding.includeSelectorMinute.clScanOrderConfig.let { cl ->
                if (cl.childCount > 3) {
                    cl.removeViews(3, cl.childCount - 3)
                }

                val list: List<ExtAttrDtoItem> =
                    it.extAttrDto.items.filter { item -> item.isEnabled }
                if (list.isNotEmpty()) {
                    list.forEachIndexed { index, item ->
                        DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                            inflater, R.layout.item_scan_order_model_item, null, false
                        )?.let { itemBinding ->
                            itemBinding.code = mViewModel.deviceDetail.value?.categoryCode
                            itemBinding.item = item
                            itemBinding.root.id = index + 1
                            itemBinding.rbOrderModelItem.let { rb ->
                                mViewModel.selectExtAttr.observe(this) { select ->
                                    (item.unitAmount == select?.unitAmount).let { isSame ->
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
                    mBinding.includeSelectorMinute.flowScanOrderItem.referencedIds = idList
                    mBinding.includeSelectorMinute.flowScanOrderItem.visibility =
                        View.VISIBLE
                }
            }
        }

        mViewModel.selectExtAttr.observe(this) {
            mViewModel.totalPrice()
            mViewModel.attachConfigure()
        }

        mViewModel.selectSelfClean.observe(this){
            mViewModel.totalPrice()
            mViewModel.attachConfigure()
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS, Boolean::class.java)?.observe(this) {
            if (it) {
                finish()
            }
        }

        LiveDataBus.with(BusEvents.LOGIN_STATUS)?.observe(this) {
            mViewModel.requestData()
        }
    }


    /**
     * 构建关键sku的布局
     */
    private fun buildAttachSkuView(detail: DeviceDetailEntity) {
        if (detail.hasAttachGoods && !detail.attachItems.isNullOrEmpty()) {
            val attachList = detail.attachItems?.filter { item -> 1 == item.soldState }
            mBinding.llSelectorConfigsAttrSku.buildChild<ItemScanOrderModelBinding, DeviceDetailItemEntity>(
                if (detail.hasAttachGoods)
                    attachList
                else
                    detail.attachItems?.subList(0, 1)
            ) { _, childBinding, data ->
                if (detail.hasAttachGoods) {
                    childBinding.modelTitle = "自动投放${data.name}"
                    childBinding.clScanOrderConfig.let { cl ->
                        if (cl.childCount > 3) {
                            cl.removeViews(3, cl.childCount - 3)
                        }
                        val inflater = LayoutInflater.from(this)
                        val items = data.extAttrDto.items.filter { item -> item.isEnabled }
                        if (items.isEmpty()) {
                            cl.visibility = View.GONE
                        } else {
                            val list = arrayListOf<ExtAttrDtoItem>()
                            list.addAll(items)
                            list.add(
                                items.first()
                                    .copy(unitAmount = "", isDefault = false, unitPrice = "")
                            )
                            list.forEachIndexed { index, item ->
                                DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                                    inflater, R.layout.item_scan_order_model_item, null, false
                                )?.let { itemBinding ->
                                    itemBinding.code = detail.categoryCode
                                    itemBinding.item = item
                                    itemBinding.root.id = index + 1
                                    itemBinding.rbOrderModelItem.let { rb ->
                                        mViewModel.selectAttachSku[data.id]?.observe(this) {
                                            it?.let {
                                                (item.unitAmount == it.unitAmount).let { isSame ->
                                                    if (isSame != rb.isChecked) {
                                                        rb.isChecked = isSame
                                                    }
                                                }
                                            }
                                        }

                                        rb.setOnClickListener {
                                            mViewModel.selectAttachSku[data.id]?.value = item
                                            mViewModel.totalPrice()
                                            mViewModel.attachConfigure()
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
                            childBinding.flowScanOrderItem.referencedIds = idList
                            childBinding.flowScanOrderItem.visibility = View.VISIBLE
                            cl.visibility = View.VISIBLE
                        }
                    }
                } else {
                    childBinding.modelTitle = "自动投放" + attachList?.joinToString("/") { item ->
                        item.name
                    }
                    childBinding.desc = detail.dispenserValue?.tipMessage ?: ""
                }
            }
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.includeScanOrderDeviceInfo.ibScanOrderDeviceInfoToggle.setOnClickListener {
            mViewModel.isHideDeviceInfo.value = !mViewModel.isHideDeviceInfo.value!!
        }
        val dimens12 = DimensionUtils.dip2px(this@OrderSelectorActivity, 12f)
        mBinding.includeDeviceStatus.root.setBackgroundColor(Color.WHITE)
        mBinding.includeSelectorSpec.root.setBackgroundResource(R.drawable.shape_bottom_stroke_dividing_mlr16)
        mBinding.includeSelectorSpec.root.let { specView ->
            specView.setPadding(dimens12, specView.paddingTop, dimens12, specView.paddingBottom)
        }
        mBinding.includeSelectorMinute.root.setBackgroundColor(Color.WHITE)
        mBinding.includeSelectorMinute.root.let { minuteView ->
            minuteView.setPadding(
                dimens12,
                minuteView.paddingTop,
                dimens12,
                minuteView.paddingBottom
            )
        }

        mBinding.includeDeviceStatus.tvDeviceStatusRefresh.setOnClickListener {
            mViewModel.requestData()
        }

        mBinding.btnSelectorNotReason.setOnClickListener {
            startActivity(
                Intent(
                    this@OrderSelectorActivity,
                    WebViewActivity::class.java
                ).apply {
                    putExtras(
                        IntentParams.WebViewParams.pack(
                            Constants.notAppointReason,
                        )
                    )
                })
        }

        mBinding.viewSelectorSelected.setOnClickListener {
            if (null == mViewModel.selectDeviceConfig.value) {
                SToast.showToast(this, "请先选择设备模式")
                return@setOnClickListener
            }

            if (null == mViewModel.selectExtAttr.value) {
                SToast.showToast(this, "请先选择运行配置")
                return@setOnClickListener
            }

            val num =
                (if (DeviceCategory.isDryer(mViewModel.deviceDetail.value?.categoryCode)) mViewModel.selectExtAttr.value?.unitAmount else 1)
                    ?: return@setOnClickListener

            // 主商品
            val goods = mutableListOf(
                Purchase(
                    mViewModel.deviceDetail.value?.id,
                    mViewModel.selectDeviceConfig.value?.id,
                    "$num",
                    if (DeviceCategory.isDryerOrHairOrDispenser(mViewModel.deviceDetail.value?.categoryCode)) 2 else 1,
                )
            )

            // 关联sku
            goods.addAll(mViewModel.selectAttachSku.filter { item -> !item.value.value?.unitAmount.isNullOrEmpty() }
                .mapNotNull { item ->
                    item.value.value?.let { dtos ->
                        Purchase(mViewModel.deviceDetail.value?.id, item.key, dtos.unitAmount, 2)
                    }
                })

            //筒自洁
            if (true == mViewModel.selectSelfClean.value) {
                goods.add(
                    Purchase(
                        mViewModel.deviceDetail.value?.selfCleanValue?.selfCleanGoodsId,
                        mViewModel.deviceDetail.value?.selfCleanValue?.selfCleanItemId,
                        "1",
                        1
                    )
                )
            }

            // 提交
            mViewModel.submitOrder(NewOrderParams(goods).apply {
                if (2 == mViewModel.deviceDetail.value?.deviceState) {
                    reserveMethod = mViewModel.deviceDetail.value?.reserveMethod
                }
            }) { success,result ->
                if (success){
                    result?.orderNo?.let { orderNo ->
                        startActivity(
                            Intent(
                                this,
                                OrderStatusActivity::class.java
                            ).apply {
                                putExtras(IntentParams.OrderParams.pack(orderNo))
                            }
                        )
                        finish()
                    }
                } else {
                    mViewModel.requestData()
                }
            }
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
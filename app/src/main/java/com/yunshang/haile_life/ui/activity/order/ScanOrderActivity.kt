package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.ViewUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.ScanOrderViewModel
import com.yunshang.haile_life.data.ActivityTag
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityScanOrderBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelItemBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.shop.RechargeStarfishActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.ScanOrderConfirmDialog
import com.yunshang.haile_life.ui.view.dialog.ShopNoticeDialog

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
        IntentParams.ScanOrderParams.parseDeviceDetail(intent)?.let {
            mViewModel.deviceDetail.value = it
        }
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.deviceDetail.observe(this) { detail ->
            detail?.let {
                detail.items.filter { item -> 1 == item.soldState }.let { configs ->
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
                                    itemBinding.root.id = index + 1
                                    itemBinding.rbOrderModelItem.let { rb ->

                                        // 吹风机使用中不可点击
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
                            mBinding.includeScanOrderConfig.flowScanOrderItem.referencedIds = idList
                            mBinding.includeScanOrderConfig.flowScanOrderItem.visibility =
                                View.VISIBLE
                        }
                    }
                }
                buildAttachSkuView(detail)
            }
        }

        mViewModel.selectDeviceConfig.observe(this) {
            mViewModel.selectExtAttr.value = null
            mBinding.includeScanOrderTime.clScanOrderConfig.let { cl ->
                if (cl.childCount > 3) {
                    cl.removeViews(3, cl.childCount - 3)
                }

                val inflater = LayoutInflater.from(this@ScanOrderActivity)

                val list: List<ExtAttrDtoItem> =
                    it.extAttrDto.items.filter { item -> item.isEnabled }
                if (list.isNotEmpty()) {
                    list.forEachIndexed { index, item ->
                        DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                            inflater, R.layout.item_scan_order_model_item, null, false
                        )?.let { itemBinding ->
                            mViewModel.goodsScan.value?.categoryCode?.let {
                                itemBinding.code = it
                            }
                            itemBinding.item = item
                            itemBinding.root.id = index + 1
                            itemBinding.rbOrderModelItem.let { rb ->

                                mViewModel.selectExtAttr.observe(this) {
                                    (item.unitAmount == it?.unitAmount).let { isSame ->
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
                    mBinding.includeScanOrderTime.flowScanOrderItem.visibility = View.VISIBLE
                }
            }
        }

        mViewModel.selectExtAttr.observe(this) {
            mViewModel.totalPrice()
            mViewModel.attachConfigure()
        }

        mViewModel.shopNotice.observe(this) {
            if (!it.isNullOrEmpty()) {
                ShopNoticeDialog(it).show(supportFragmentManager)
            }
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS)?.observe(this) {
            finish()
        }

        // 充值后刷新店铺配置
        LiveDataBus.with(BusEvents.RECHARGE_SUCCESS_STATUS)?.observe(this) {
            mViewModel.goodsScan.value?.let { scan ->
                mViewModel.deviceDetail.value?.let { detail ->
                    mViewModel.requestShopListAsync(detail.shopId, scan.goodsId, detail.categoryId)
                }
            }
        }

        LiveDataBus.with(BusEvents.APPOINT_ORDER_USE_STATUS)?.observe(this) {
            finish()
        }
    }

    /**
     * 构建关键sku的布局
     */
    private fun buildAttachSkuView(detail: DeviceDetailEntity) {
        if (detail.hasAttachGoods && detail.attachItems.isNotEmpty()) {
            val attachList = detail.attachItems.filter { item -> 1 == item.soldState }
            mBinding.llScanOrderConfigsAttrSku.buildChild<ItemScanOrderModelBinding, DeviceDetailItemEntity>(
                if (detail.isShowDispenser)
                    attachList
                else
                    detail.attachItems.subList(0, 1)
            ) { _, childBinding, data ->
                if (detail.isShowDispenser) {
                    childBinding.modelTitle = "自动投放${data.name}"
                    childBinding.clScanOrderConfig.let { cl ->
                        if (cl.childCount > 3) {
                            cl.removeViews(3, cl.childCount - 3)
                        }
                        val inflater = LayoutInflater.from(this@ScanOrderActivity)
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
                                    mViewModel.goodsScan.value?.categoryCode?.let {
                                        itemBinding.code = it
                                    }
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
                    childBinding.modelTitle = "自动投放" + attachList.joinToString("/") { item ->
                        item.name
                    }
                    childBinding.desc = detail.hideDispenserTips
                }
            }
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

                if (true == mViewModel.shopConfig.value?.result) {
                    mViewModel.deviceDetail.value?.shopId?.let { shopId ->
                        CommonDialog.Builder("海星余额不足，请先购买海星后再使用").apply {
                            title = StringUtils.getString(R.string.scan_order_tips_hint)
                            isCancelable = mViewModel.shopConfig.value?.closable ?: true
                            isNegativeShow = false
                            setPositiveButton(StringUtils.getString(R.string.go_buy)) {
                                startActivity(
                                    Intent(
                                        this@ScanOrderActivity,
                                        RechargeStarfishActivity::class.java
                                    ).apply {
                                        putExtras(IntentParams.RechargeStarfishParams.pack(shopId))
                                    })
                            }
                        }.build().show(supportFragmentManager)
                    }
                    return@setOnClickListener
                }

                val isSpecialDevice = mViewModel.deviceDetail.value?.spuCode == "04001030"
                if ((!SPRepository.isNoPrompt && null != mViewModel.goodsScan.value
                            && !DeviceCategory.isHair(mViewModel.goodsScan.value!!.categoryCode))
                    || isSpecialDevice
                ) {
                    ScanOrderConfirmDialog.Builder(
                        mViewModel.goodsScan.value!!.categoryCode,
                        isSpecialDevice
                    ) {
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
            (if (true == mViewModel.isDryer.value) mViewModel.selectExtAttr.value?.unitAmount else 1)
                ?: return

        if (null == mViewModel.deviceDetail.value) return

        // 主商品
        val goods = mutableListOf(
            IntentParams.OrderSubmitParams.OrderSubmitGood(
                categoryCode,
                goodId,
                goodItemId,
                "$num",
            )
        )
        // 关联sku
        goods.addAll(mViewModel.selectAttachSku.filter { item -> !item.value.value?.unitAmount.isNullOrEmpty() }
            .mapNotNull { item ->
                item.value.value?.let { dtos ->
                    IntentParams.OrderSubmitParams.OrderSubmitGood(
                        DeviceCategory.Dispenser,
                        mViewModel.deviceDetail.value!!.attachGoodsId,
                        item.key,
                        dtos.unitAmount,
                    )
                }
            })
        startActivity(Intent(this, OrderSubmitActivity::class.java).apply {
            putExtras(
                IntentParams.OrderSubmitParams.pack(goods)
            )
        })
    }

    override fun initData() {
        mViewModel.requestData(IntentParams.ScanOrderParams.parseCode(intent))
    }
}
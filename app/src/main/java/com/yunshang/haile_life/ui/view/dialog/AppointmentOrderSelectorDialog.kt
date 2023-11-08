package com.yunshang.haile_life.ui.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.AppointmentOrderParams
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.Purchase
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.databinding.DialogAppointmentOrderSelectorBinding
import com.yunshang.haile_life.databinding.ItemDeviceStatusProgressBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelItemBinding
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import java.math.BigDecimal

/**
 * Title :
 * Author: Lsy
 * Date: 2023/7/3 10:18
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class AppointmentOrderSelectorDialog private constructor(private val builder: Builder) :
    BottomSheetDialogFragment() {
    private val ORDER_SELECTOR_TAG = "order_selector_tag"
    private lateinit var mBinding: DialogAppointmentOrderSelectorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CommonBottomSheetDialogStyle)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            if (this is BottomSheetDialog) {
                setOnShowListener {
                    behavior.isHideable = false
                    // dialog上还有一层viewGroup，需要设置成透明
                    (requireView().parent as ViewGroup).setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_appointment_order_selector,
            container,
            false
        )
        mBinding.setVariable(BR.vm, builder)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.ibAppointmentOrderSelectorClose.setOnClickListener {
            dismiss()
        }
        mBinding.tvOrderSelectorTitle.text = builder.deviceDetail.value?.name
        // 空闲设备不显示
        mBinding.includeOrderSelectorDeviceStatus.tvDeviceStatusRefresh.setOnClickListener {
            builder.refreshStateList()
        }
        builder.stateList.observe(this) {
            buildStateListView(it)
        }

        val inflater = LayoutInflater.from(requireContext())
        builder.deviceDetail.observe(this) { detail ->
            mBinding.includeOrderSelectorDeviceStatus.llDeviceStatusItems.visibility(1 != detail.deviceState)
            detail.items.filter { item -> 1 == item.soldState }.let { configs ->
                if (configs.isNotEmpty()) {
                    mBinding.includeOrderSelectorSpec.clScanOrderConfig.let { cl ->
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
                                    builder.selectDeviceConfig.observe(this) {
                                        (item.id == it.id).let { isSame ->
                                            if (isSame != rb.isChecked) {
                                                rb.isChecked = isSame
                                            }
                                        }
                                    }
                                    rb.setOnClickListener {
                                        builder.selectDeviceConfig.value = item
                                        builder.changeDeviceConfig(item)
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
                        mBinding.includeOrderSelectorSpec.flowScanOrderItem.referencedIds = idList
                        mBinding.includeOrderSelectorSpec.flowScanOrderItem.visibility =
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
        builder.selectDeviceConfig.observe(this) {
            mBinding.includeAppointSubmitMinute.clScanOrderConfig.let { cl ->
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
                            itemBinding.code = builder.deviceDetail.value?.categoryCode
                            itemBinding.item = item
                            itemBinding.root.id = index + 1
                            itemBinding.rbOrderModelItem.let { rb ->
                                builder.selectExtAttr.observe(this) { select ->
                                    (item.unitAmount == select?.unitAmount).let { isSame ->
                                        if (isSame != rb.isChecked) {
                                            rb.isChecked = isSame
                                        }
                                    }
                                }
                                rb.setOnClickListener {
                                    builder.selectExtAttr.value = item
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
                    mBinding.includeAppointSubmitMinute.flowScanOrderItem.referencedIds = idList
                    mBinding.includeAppointSubmitMinute.flowScanOrderItem.visibility = View.VISIBLE
                }
            }
        }

        builder.selectExtAttr.observe(this) {
            builder.totalPrice()
        }

        mBinding.viewOrderSelectSubmitSelected.setOnClickListener {
            if (null == builder.selectDeviceConfig.value) {
                SToast.showToast(requireContext(), "请先选择设备模式")
                return@setOnClickListener
            }

            if (null == builder.selectExtAttr.value) {
                SToast.showToast(requireContext(), "请先选择运行配置")
                return@setOnClickListener
            }

            val num =
                (if (true == builder.isDryer.value) builder.selectExtAttr.value?.unitAmount else 1)
                    ?: return@setOnClickListener

            // 主商品
            val goods = mutableListOf(
                Purchase(
                    builder.deviceDetail.value?.id,
                    builder.selectDeviceConfig.value?.id,
                    "$num",
                    if (DeviceCategory.isDryerOrHairOrDispenser(builder.deviceDetail.value?.categoryCode)) 2 else 1,
                )
            )

            // 关联sku
            goods.addAll(builder.selectAttachSku.filter { item -> !item.value.value?.unitAmount.isNullOrEmpty() }
                .mapNotNull { item ->
                    item.value.value?.let { dtos ->
                        Purchase(builder.deviceDetail.value?.id, item.key, dtos.unitAmount, 2)
                    }
                })

            // 提交
            builder.submitCallBack(AppointmentOrderParams(goods).apply {
                if (2 == builder.deviceDetail.value?.deviceState) {
                    reserveMethod = builder.deviceDetail.value?.reserveMethod
                }
            })
        }
    }

    private fun buildStateListView(stateList: List<DeviceStateEntity>?) {
        val size = stateList?.let { it.size - 1 } ?: 0
        mBinding.includeOrderSelectorDeviceStatus.includeAppointmentDeviceStatusProgress.llDeviceStateProgress.buildChild<ItemDeviceStatusProgressBinding, DeviceStateEntity>(
            stateList,
            LinearLayoutCompat.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            ).apply {
                weight = 1f
            }
        ) { index, childBinding, data ->
            childBinding.isFirst = 0 == index
            childBinding.isLast = index == size
            childBinding.item = data
        }
    }

    /**
     * 构建关键sku的布局
     */
    private fun buildAttachSkuView(detail: DeviceDetailEntity) {
        if (detail.hasAttachGoods && !detail.attachItems.isNullOrEmpty()) {
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
                        val inflater = LayoutInflater.from(requireContext())
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
                                        builder.selectAttachSku[data.id]?.observe(this) {
                                            it?.let {
                                                (item.unitAmount == it.unitAmount).let { isSame ->
                                                    if (isSame != rb.isChecked) {
                                                        rb.isChecked = isSame
                                                    }
                                                }
                                            }
                                        }

                                        rb.setOnClickListener {
                                            builder.selectAttachSku[data.id]?.value = item
                                            builder.totalPrice()
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

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        show(manager, ORDER_SELECTOR_TAG)
    }

    internal class Builder(
        val deviceDetail: MutableLiveData<DeviceDetailEntity>,
        val stateList: MutableLiveData<List<DeviceStateEntity>?>,
        val refreshStateList: () -> Unit,
        val submitCallBack: (params: AppointmentOrderParams) -> Unit
    ) {

        // 选择的设备模式
        val selectDeviceConfig: MutableLiveData<DeviceDetailItemEntity> by lazy {
            MutableLiveData()
        }

        // 选择的设备属性
        val selectExtAttr: MutableLiveData<ExtAttrDtoItem?> by lazy {
            MutableLiveData()
        }

        var selectAttachSku: MutableMap<Int, MutableLiveData<ExtAttrDtoItem?>> = mutableMapOf()

        val needAttach: LiveData<Boolean> = selectDeviceConfig.map {
            it?.let { item ->
                !(item.name == "单脱" || item.name == "筒自洁")
            } ?: false
        }

        val isDryer: LiveData<Boolean> =
            deviceDetail.map { DeviceCategory.isDryer(it.categoryCode) }

        val totalPriceVal: MutableLiveData<Double> by lazy {
            MutableLiveData()
        }

        init {
            deviceDetail.value?.let { detail ->
                val list = detail.items.filter { item -> 1 == item.soldState }
                //如果没有默认，就显示第一个
                (list.find { item -> item.extAttrDto.items.any { attr -> attr.isEnabled && attr.isDefault } }
                    ?: run { list.firstOrNull() })?.let { first ->
                    selectDeviceConfig.value = first
                    changeDeviceConfig(first)
                }

                if (detail.hasAttachGoods && !detail.attachItems.isNullOrEmpty()) {
                    // 初始化关联的sku
                    selectAttachSku = mutableMapOf()
                    detail.attachItems.forEach { item ->
                        if (item.extAttrDto.items.isNotEmpty()) {
                            selectAttachSku[item.id] =
                                item.extAttrDto.items.find { dto -> dto.isEnabled && dto.isDefault }
                                    ?.let { default ->
                                        MutableLiveData(default)
                                    } ?: MutableLiveData(null)
                        }
                    }
                }
            }
        }

        val modelTitle: String = StringUtils.getString(
            R.string.select_work_model,
            deviceDetail.value?.categoryName?.replace("机", "")
        )

        /**
         * 切换选择功能的配置
         */
        fun changeDeviceConfig(itemEntity: DeviceDetailItemEntity) {
            (itemEntity.extAttrDto.items.firstOrNull() { item ->
                item.isEnabled && item.isDefault
            }
                ?: itemEntity.extAttrDto.items.firstOrNull { item -> item.isEnabled })?.let { firstAttr ->
                selectExtAttr.value = firstAttr
            }
        }

        fun totalPrice() {
            var attachTotal = BigDecimal("0.0")
            selectAttachSku.forEach { item ->
                if (!item.value.value?.unitPrice.isNullOrEmpty()) {
                    attachTotal = attachTotal.add(BigDecimal(item.value.value!!.unitPrice))
                }
            }
            totalPriceVal.value =
                BigDecimal(selectExtAttr.value?.unitPrice ?: "0.0").add(attachTotal).toDouble()
        }

        /**
         * 构建
         */
        fun build(): AppointmentOrderSelectorDialog = AppointmentOrderSelectorDialog(this)
    }
}
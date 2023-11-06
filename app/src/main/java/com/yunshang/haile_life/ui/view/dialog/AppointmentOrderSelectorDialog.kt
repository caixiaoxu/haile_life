package com.yunshang.haile_life.ui.view.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.entities.*
import com.yunshang.haile_life.databinding.DialogAppointmentOrderSelectorBinding
import com.yunshang.haile_life.databinding.ItemDeviceStatusProgressBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelBinding
import com.yunshang.haile_life.databinding.ItemScanOrderModelItemBinding
import com.yunshang.haile_life.ui.activity.order.AppointmentSuccessActivity
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
        mBinding.tvOrderSelectorTitle.text = builder.deviceDetail.name
        // 空闲设备不显示
        mBinding.includeOrderSelectorDeviceStatus.root.visibility(1 != builder.deviceStatus.state)
        mBinding.includeOrderSelectorDeviceStatus.tvDeviceStatusRefresh.setOnClickListener {
            builder.requestRefreshStateList() {
                buildStateListView()
            }
        }
        buildStateListView()

        val inflater = LayoutInflater.from(requireContext())
        builder.deviceDetail.items.filter { item -> 1 == item.soldState }.let { configs ->
            if (configs.isNotEmpty()) {
                mBinding.includeOrderSelectorSpec.clScanOrderConfig.let { cl ->
                    if (cl.childCount > 3) {
                        cl.removeViews(3, cl.childCount - 3)
                    }
                    configs.forEachIndexed { index, item ->
                        DataBindingUtil.inflate<ItemScanOrderModelItemBinding>(
                            inflater, R.layout.item_scan_order_model_item, null, false
                        )?.let { itemBinding ->
                            itemBinding.code = builder.deviceDetail.categoryCode
                            itemBinding.item = item
                            itemBinding.root.id = index + 1
                            itemBinding.rbOrderModelItem.let { rb ->

                                // 吹风机使用中不可点击
                                if (DeviceCategory.isHair(builder.deviceDetail.categoryCode) && 0 == item.amount) {
                                    rb.setTextColor(
                                        ContextCompat.getColor(
                                            requireContext(),
                                            R.color.color_black_25
                                        )
                                    )
                                    rb.setOnRadioClickListener { true }
                                }

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
        buildAttachSkuView(builder.deviceDetail)

        // 时长
        builder.selectDeviceConfig.observe(this) {
            builder.selectExtAttr.value = null
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
                            itemBinding.code = builder.deviceDetail.categoryCode
                            itemBinding.item = item
                            itemBinding.root.id = index + 1
                            itemBinding.rbOrderModelItem.let { rb ->
                                builder.selectExtAttr.observe(this) {
                                    (item.unitAmount == it?.unitAmount).let { isSame ->
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
            startActivity(Intent(requireContext(), AppointmentSuccessActivity::class.java))
        }
    }

    private fun buildStateListView() {
        val size = builder.stateList?.let { it.size - 1 } ?: 0
        mBinding.includeOrderSelectorDeviceStatus.includeAppointmentDeviceStatusProgress.llDeviceStateProgress.buildChild<ItemDeviceStatusProgressBinding, DeviceStateEntity>(
            builder.stateList
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
        val deviceStatus: ShopPositionDeviceEntity,
        val deviceDetail: DeviceDetailEntity,
        var stateList: List<DeviceStateEntity>?,
        val refreshStateList: (callBack: (stateList: List<DeviceStateEntity>?) -> Unit) -> Unit,
        val callBack: (() -> Unit)? = null
    ) {

        // 选择的设备模式
        val selectDeviceConfig: MutableLiveData<DeviceDetailItemEntity> by lazy {
            MutableLiveData()
        }

        /**
         * 切换选择功能的配置
         */
        fun changeDeviceConfig(itemEntity: DeviceDetailItemEntity) {
            (itemEntity.extAttrDto.items.firstOrNull() { item ->
                item.isEnabled && item.isDefault
            }
                ?: itemEntity.extAttrDto.items.firstOrNull { item -> item.isEnabled })?.let { firstAttr ->
                selectExtAttr.postValue(firstAttr)
            }
        }

        // 选择的设备属性
        val selectExtAttr: MutableLiveData<ExtAttrDtoItem?> by lazy {
            MutableLiveData()
        }

        var selectAttachSku: MutableMap<Int, MutableLiveData<ExtAttrDtoItem?>> = mutableMapOf()

        init {
            // 吹风机只选中未使用，
            val list = deviceDetail.items.filter { item -> 1 == item.soldState }
            if (DeviceCategory.isHair(deviceDetail.categoryCode)) {
                list.find { item -> 1 == item.amount }
            } else {
                //如果没有默认，就显示第一个
                list.find { item -> item.extAttrDto.items.any { attr -> attr.isEnabled && attr.isDefault } }
                    ?: run { list.firstOrNull() }
            }?.let { first ->
                selectDeviceConfig.postValue(first)
                changeDeviceConfig(first)
            }

            if (deviceDetail.hasAttachGoods && !deviceDetail.attachItems.isNullOrEmpty()) {
                // 初始化关联的sku
                selectAttachSku = mutableMapOf()
                deviceDetail.attachItems.forEach { item ->
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

        val needAttach: LiveData<Boolean> = selectDeviceConfig.map {
            it?.let { item ->
                !(item.name == "单脱" || item.name == "筒自洁")
            } ?: false
        }

        val isDryerOrHair: Boolean
            get() = DeviceCategory.isDryerOrHair(deviceDetail.categoryCode)

        val totalPriceVal: MutableLiveData<Double> by lazy {
            MutableLiveData()
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

        fun requestRefreshStateList(callBack: () -> Unit) {
            refreshStateList(){
                stateList = it
                callBack()
            }
        }

        /**
         * 构建
         */
        fun build(): AppointmentOrderSelectorDialog = AppointmentOrderSelectorDialog(this)
    }
}
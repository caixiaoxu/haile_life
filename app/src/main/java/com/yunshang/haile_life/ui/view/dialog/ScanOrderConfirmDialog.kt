package com.yunshang.haile_life.ui.view.dialog

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.DialogScanOrderConfirmBinding
import com.yunshang.haile_life.databinding.ItemScanOrderConfirmItemBinding
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility

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
class ScanOrderConfirmDialog private constructor(private val builder: Builder) :
    BottomSheetDialogFragment() {
    private val SCAN_ORDER_CONFIRM_TAG = "scan_order_confirm_tag"
    private lateinit var mBinding: DialogScanOrderConfirmBinding

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
        mBinding = DialogScanOrderConfirmBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isDryer = DeviceCategory.isDryerOrHair(builder.deviceCategoryCode)

        if (builder.isAppoint) {
            mBinding.tvScanOrderConfirmPrompt.text = "确认后，机器将自动启动，请务必确认"
        }
        mBinding.tvScanOrderConfirmPrompt.visibility(
            builder.isAppoint || DeviceCategory.isWashingOrShoes(
                builder.deviceCategoryCode
            ) || builder.isSpecialDevice
        )

        val color = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                if (isDryer) R.color.color_ff630e else R.color.colorPrimary
            )
        )

        mBinding.btnScanOrderConfirmNext.backgroundTintList = color
        mBinding.ivScanOrderConfirmMain.setImageResource(if (isDryer) R.mipmap.icon_scan_order_tips_dryer_main else R.mipmap.icon_scan_order_tips_main)
        mBinding.llScanOrderConfirmItems.buildChild<ItemScanOrderConfirmItemBinding, String>(
            requireContext().resources.getStringArray(
                when (builder.deviceCategoryCode) {
                    DeviceCategory.Dryer -> R.array.scan_order_dryer_confirm
                    DeviceCategory.Shoes -> R.array.scan_order_shoes_confirm
                    else -> R.array.scan_order_wash_confirm
                }
            ).toList()
        ) { _, childBinding, data ->
            childBinding.tvScanOrderConfirmItem.setCompoundDrawablesWithIntrinsicBounds(
                if (isDryer) R.mipmap.icon_scan_order_tips_dryer_ok else R.mipmap.icon_scan_order_tips_ok,
                0, 0, 0
            )
            childBinding.tvScanOrderConfirmItem.text = data
        }

        mBinding.cbScanOrderConfirmNoPrompt.setCompoundDrawablesWithIntrinsicBounds(
            if (isDryer) R.drawable.selector_orange_check else R.drawable.selector_cyan_check,
            0, 0, 0
        )
        mBinding.cbScanOrderConfirmNoPrompt.visibility =
            if (builder.isSpecialDevice) View.GONE else View.VISIBLE

        mBinding.btnScanOrderConfirmCancel.visibility(!builder.isAppoint)
        mBinding.btnScanOrderConfirmCancel.setOnClickListener {
            dismiss()
        }
        mBinding.btnScanOrderConfirmNext.text =
            StringUtils.getString(if (builder.isAppoint) R.string.affirm else R.string.next_step)
        mBinding.btnScanOrderConfirmNext.setOnClickListener {
            if (mBinding.cbScanOrderConfirmNoPrompt.isChecked) {
                if (builder.isAppoint)
                    SPRepository.isNoAppointPrompt = true
                else
                    SPRepository.isNoPrompt = true
            }
            builder.callBack?.invoke()
            dismiss()
        }
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        isCancelable = !builder.isAppoint
        show(manager, SCAN_ORDER_CONFIRM_TAG)
    }

    internal class Builder(
        val deviceCategoryCode: String,
        val isSpecialDevice: Boolean,
        val isAppoint: Boolean = false,
        val callBack: (() -> Unit)? = null
    ) {
        /**
         * 构建
         */
        fun build(): ScanOrderConfirmDialog = ScanOrderConfirmDialog(this)
    }
}
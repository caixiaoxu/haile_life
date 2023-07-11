package com.yunshang.haile_life.ui.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.databinding.DialogOrderPayBinding

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
class OrderPayDialog private constructor(private val builder: Builder) :
    BottomSheetDialogFragment() {
    private val ORDER_PAY_TAG = "order_pay_tag"
    private lateinit var mBinding: DialogOrderPayBinding

    private var type: Int = -1

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
                    (requireView().parent as ViewGroup).setBackgroundResource(R.drawable.shape_sffffff_rt8)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_order_pay, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.isDryer = DeviceCategory.isDryerOrHair(builder.deviceCategoryCode)
        mBinding.isZero = builder.isZero

        // 支付方式
        mBinding.rgOrderPayWay.setOnCheckedChangeListener { group, checkedId ->
            type = when (checkedId) {
                R.id.rb_order_balance_pay_way -> 0
                R.id.rb_order_alipay_pay_way -> 1
                R.id.rb_order_wechat_pay_way -> 2
                else -> -1
            }
        }
        mBinding.tvOrderPayCancel.setOnClickListener {
            dismiss()
        }
        mBinding.btnOrderPay.setOnClickListener {
            if (-1 == type){
                SToast.showToast(requireContext(),"请先选择支付方式")
                return@setOnClickListener
            }
            dismiss()
            builder.callBack?.invoke(type)
        }
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        show(manager, ORDER_PAY_TAG)
    }

    internal class Builder(
        val deviceCategoryCode: String,
        val isZero: Boolean,
        val callBack: ((type: Int) -> Unit)? = null
    ) {
        /**
         * 构建
         */
        fun build(): OrderPayDialog = OrderPayDialog(this)
    }
}
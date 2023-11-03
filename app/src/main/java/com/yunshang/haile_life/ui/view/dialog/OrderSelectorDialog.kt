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
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.DialogOrderSelectorBinding
import com.yunshang.haile_life.databinding.DialogScanOrderConfirmBinding
import com.yunshang.haile_life.databinding.ItemScanOrderConfirmItemBinding

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
class OrderSelectorDialog private constructor(private val builder: Builder) :
    BottomSheetDialogFragment() {
    private val ORDER_SELECTOR_TAG = "order_selector_tag"
    private lateinit var mBinding: DialogOrderSelectorBinding

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
        mBinding = DialogOrderSelectorBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        show(manager, ORDER_SELECTOR_TAG)
    }

    internal class Builder(val callBack: (() -> Unit)? = null) {
        /**
         * 构建
         */
        fun build(): OrderSelectorDialog = OrderSelectorDialog(this)
    }
}
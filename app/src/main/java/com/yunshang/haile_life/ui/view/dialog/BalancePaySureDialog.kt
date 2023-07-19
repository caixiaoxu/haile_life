package com.yunshang.haile_life.ui.view.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.rule.ICommonBottomItemEntity
import com.yunshang.haile_life.databinding.DialogBalancePaySureBinding
import com.yunshang.haile_life.databinding.DialogCommonBottomSheetBinding
import com.yunshang.haile_life.databinding.ItemCommonBottomSheetDialogBinding


/**
 * Title : 日期选择Dialog
 * Author: Lsy
 * Date: 2023/4/11 09:47
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class BalancePaySureDialog(
    private val balance: String,//余额
    private val payPrice: String,//支付金额
    private val callBack: () -> Unit
) :
    BottomSheetDialogFragment() {
    private val BALANCE_PAY_SURE_TAG = "balance_pay_sure_tag"
    private lateinit var mBinding: DialogBalancePaySureBinding

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
        mBinding = DialogBalancePaySureBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 关闭
        mBinding.btnBalancePaySureCancel.setOnClickListener {
            dismiss()
        }

        // 支付金额
        mBinding.tvBalancePaySureAmount.text = payPrice

        // 余额
        mBinding.tvBalancePaySureBalance.text = StringUtils.getString(R.string.unit_money) + balance

        // 支付
        mBinding.btnBalancePaySurePay.setOnClickListener {
            dismiss()
            callBack()
        }
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        show(manager, BALANCE_PAY_SURE_TAG)
    }
}
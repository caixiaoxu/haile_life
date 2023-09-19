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
import com.yunshang.haile_life.R
import com.yunshang.haile_life.data.rule.ICommonBottomItemEntity
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
class CommonBottomSheetDialog<D : ICommonBottomItemEntity> private constructor(private val builder: Builder<D>) :
    BottomSheetDialogFragment() {
    private val COMMON_BOTTOM_SHEET_TAG = "common_bottom_sheet_tag"
    private lateinit var mBinding: DialogCommonBottomSheetBinding

    private var curEntity: D? = null

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
        mBinding = DialogCommonBottomSheetBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 关闭
        mBinding.tvCommonDialogClose.visibility =
            if (1 == builder.selectModel) View.GONE else View.VISIBLE
        mBinding.tvCommonDialogClose.setOnClickListener {
            dismiss()
        }
        mBinding.tvCommonDialogCancel.setOnClickListener {
            dismiss()
        }

        mBinding.tvCommonDialogTitle.text = builder.title

        mBinding.clCommonDialogTitle.visibility =
            if (builder.title.isEmpty()) View.GONE else View.VISIBLE

        // 确定
        mBinding.tvCommonDialogSure.visibility =
            if (1 == builder.selectModel) View.GONE else View.VISIBLE
        mBinding.tvCommonDialogSure.setOnClickListener {
            if (builder.mustSelect && null == curEntity) {
                SToast.showToast(context, "您还没有选择选项")
                return@setOnClickListener
            }

            dismiss()
            builder.onValueSureListener?.onValue(curEntity)
        }

        //选项
        mBinding.llDialogCommonList.removeAllViews()
        builder.list.forEachIndexed { index, data ->
            ItemCommonBottomSheetDialogBinding.bind(
                layoutInflater.inflate(
                    R.layout.item_common_bottom_sheet_dialog,
                    null
                )
            ).let { itemBinding ->
                itemBinding.root.id = index

                if (index > 0) {
                    mBinding.llDialogCommonList.addView(
                        View(context).apply {
                            setBackgroundResource(R.drawable.divder_efefef)
                        },
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            DimensionUtils.dip2px(requireContext(), 0.5f)
                        )
                    )
                }

                mBinding.llDialogCommonList.addView(
                    itemBinding.root.apply {
                        text = data.getTitle()

                        if (builder.title.isEmpty()) {
                            setBackgroundColor(Color.TRANSPARENT)
                        }

                        setOnRadioClickListener {
                            if (1 == builder.selectModel) {
                                builder.onValueSureListener?.onValue(data)
                                dismiss()
                                true
                            } else false
                        }

                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                curEntity = data
                            }
                        }

                        isChecked = builder.selectData == data
                    },
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        DimensionUtils.dip2px(requireContext(), 54f)
                    )
                )
            }
        }
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        //不可取消
        isCancelable = builder.isCancelable
        show(manager, COMMON_BOTTOM_SHEET_TAG)
    }

    internal class Builder<D : ICommonBottomItemEntity>(val title: String, val list: List<D>) {

        // 选择模式 0选中再点确定，1选中直接返回
        var selectModel = 0

        // 不可取消
        var isCancelable = true

        // 选择监听
        var onValueSureListener: OnValueSureListener<D>? = null

        // 是否可不选
        var mustSelect = true

        // 已选择的数据
        var selectData: D? = null

        /**
         * 构建
         */
        fun build(): CommonBottomSheetDialog<D> = CommonBottomSheetDialog(this)
    }

    interface OnValueSureListener<D : ICommonBottomItemEntity> {
        fun onValue(data: D?)
    }
}
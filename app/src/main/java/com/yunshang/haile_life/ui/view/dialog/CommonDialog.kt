package com.yunshang.haile_life.ui.view.dialog

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.yunshang.haile_life.R
import com.yunshang.haile_life.databinding.DialogCommonBinding

/**
 * Title : 常用的dialog
 * Author: Lsy
 * Date: 2023/4/4 09:55
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class CommonDialog private constructor(private val builder: Builder) : AppCompatDialogFragment() {
    private val DEFAULT_TAG = "default_tag"
    private lateinit var mBinding: DialogCommonBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogCommonBinding.inflate(inflater, container, false)
        // 背景图
        if (null != builder.dialogBgDrawable) {
            dialog?.window?.setBackgroundDrawable(builder.dialogBgDrawable)
        } else {
            dialog?.window?.setBackgroundDrawableResource(builder.dialogBgResource)
        }
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        //宽高
        dialog?.window?.setLayout(
            builder.dialogW ?: resources.getDimensionPixelOffset(R.dimen.common_dialog_w),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 标题
        if (builder.title.isNullOrEmpty()) {
            mBinding.tvCommonDialogTitle.visibility = View.GONE
        } else {
            mBinding.tvCommonDialogTitle.visibility = View.VISIBLE
            mBinding.tvCommonDialogTitle.text = builder.title
        }

        //内容
        mBinding.tvCommonDialogContent.movementMethod = LinkMovementMethod.getInstance()
        mBinding.tvCommonDialogContent.highlightColor = Color.TRANSPARENT
        mBinding.tvCommonDialogContent.text = builder.msg

        //拒绝按钮
        if (builder.isNegativeShow) {
            builder.negativeTxt?.let { mBinding.btnCommonDialogNo.text = it }
            mBinding.btnCommonDialogNo.setOnClickListener {
                dismiss()
                builder.negativeClickListener?.onClick(it)
            }
            mBinding.btnCommonDialogNo.visibility = View.VISIBLE
        } else {
            mBinding.btnCommonDialogNo.visibility = View.GONE
        }

        //确定按钮
        if (builder.isPositiveShow) {
            builder.positiveTxt?.let { mBinding.btnCommonDialogYes.text = it }
            mBinding.btnCommonDialogYes.setOnClickListener {
                dismiss()
                builder.positiveClickListener?.onClick(it)
            }
            mBinding.btnCommonDialogYes.visibility = View.VISIBLE
        } else {
            mBinding.btnCommonDialogYes.visibility = View.GONE
        }
    }

    /**
     * 获取同意按钮
     */
    fun getPositiveButton() = mBinding.btnCommonDialogYes

    /**
     * 获取拒绝按钮
     */
    fun getNegativeButton() = mBinding.btnCommonDialogNo

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        //不可取消
        isCancelable = builder.isCancelable
        show(manager, DEFAULT_TAG)
    }

    internal class Builder(val msg: CharSequence) {
        // 标题
        var title: String? = null

        // 拒绝按钮文本
        var negativeTxt: String? = null
        var negativeClickListener: OnClickListener? = null
        var isNegativeShow: Boolean = true

        // 同意按钮文本
        var positiveTxt: String? = null
        var positiveClickListener: OnClickListener? = null
        var isPositiveShow: Boolean = true

        // 对话框的宽
        var dialogW: Int? = null

        // 对话框的高
        var dialogH: Int? = null

        // 对话框背景
        var dialogBgDrawable: Drawable? = null
        var dialogBgResource: Int = R.drawable.shape_sffffff_r8

        // 不可取消
        var isCancelable = false

        /**
         * 设置拒绝按钮
         */
        fun setNegativeButton(txt: String, clickListener: OnClickListener) {
            negativeTxt = txt
            negativeClickListener = clickListener
            isNegativeShow = true
        }

        /**
         * 设置确定按钮
         */
        fun setPositiveButton(txt: String, clickListener: OnClickListener) {
            positiveTxt = txt
            positiveClickListener = clickListener
            isPositiveShow = true
        }

        /**
         * 构建
         */
        fun build(): CommonDialog = CommonDialog(this)
    }
}
package com.yunshang.haile_life.ui.view.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.yunshang.haile_life.R
import com.yunshang.haile_life.databinding.DialogIssueEvaluateSureBinding

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
class IssueEvaluateSureDialog private constructor(private val builder: Builder) :
    AppCompatDialogFragment() {
    private val ISSUE_EVALUATE_SURE_TAG = "issue_evaluate_sure_tag"
    private lateinit var mBinding: DialogIssueEvaluateSureBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogIssueEvaluateSureBinding.inflate(inflater, container, false)
        // 背景图
        dialog?.window?.setBackgroundDrawableResource(R.drawable.shape_sffffff_r8)
        return mBinding.root
    }

    override fun onResume() {
        super.onResume()
        //宽高
        dialog?.window?.setLayout(
            resources.getDimensionPixelOffset(R.dimen.common_dialog_w),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.ibIssueEvaluateSureClose.setOnClickListener {
            dismiss()
        }

        mBinding.btnIssueEvaluateSureContactService.setOnClickListener {
            dismiss()
            builder.positiveClickListener()
        }

        mBinding.btnIssueEvaluateSureIssue.setOnClickListener {
            dismiss()
            builder.negativeClickListener()
        }
    }

    /**
     * 默认显示
     */
    fun show(manager: FragmentManager) {
        //不可取消
        show(manager, ISSUE_EVALUATE_SURE_TAG)
    }

    internal class Builder(
        val negativeClickListener: () -> Unit,
        val positiveClickListener: () -> Unit
    ) {

        /**
         * 构建
         */
        fun build(): IssueEvaluateSureDialog = IssueEvaluateSureDialog(this)
    }
}
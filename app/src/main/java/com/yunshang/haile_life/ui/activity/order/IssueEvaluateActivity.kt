package com.yunshang.haile_life.ui.activity.order

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.IssueEvaluateViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.FeedbackTemplateProjectDto
import com.yunshang.haile_life.databinding.ActivityIssueEvaluateBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluateScoreBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluateTagBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import com.yunshang.haile_life.utils.DialogUtils

class IssueEvaluateActivity :
    BaseBusinessActivity<ActivityIssueEvaluateBinding, IssueEvaluateViewModel>(
        IssueEvaluateViewModel::class.java, BR.vm
    ) {

    private val filePermissions = SystemPermissionHelper.readWritePermissions()

    // 文件读取权限
    private val requestFilePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            if (result.values.any { it }) {
                // 授权权限成功
                DialogUtils.showImgSelectorDialog(
                    this@IssueEvaluateActivity,
                    6 - (mViewModel.evaluatePics.value?.size ?: 0),
                    needCrop = false,
                    title = "选择操作"
                ) { isSuccess, picList ->
                    if (isSuccess && !picList.isNullOrEmpty()) {
                        mViewModel.uploadEvaluatePic(picList.mapNotNull { item ->
                            item.cutPath ?: item.realPath
                        }.toList())
                    }
                }
            } else {
                // 授权失败
                SToast.showToast(this, R.string.empty_permission)
            }
        }

    override fun layoutId(): Int = R.layout.activity_issue_evaluate

    override fun backBtn(): View = mBinding.barIssueEvaluateTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()

        mViewModel.issueEvaluateParams.value?.orderId =
            IntentParams.OrderIssueEvaluateParams.parseOrderId(intent)
        mViewModel.issueEvaluateParams.value?.orderNo =
            IntentParams.OrderIssueEvaluateParams.parseOrderNo(intent)
        mViewModel.issueEvaluateParams.value?.goodsId =
            IntentParams.OrderIssueEvaluateParams.parseGoodId(intent)
        mViewModel.issueEvaluateParams.value?.buyerId =
            IntentParams.OrderIssueEvaluateParams.parseBuyerId(intent)
        mViewModel.issueEvaluateParams.value?.sellerId =
            IntentParams.OrderIssueEvaluateParams.parseSellerId(intent)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.evaluateScoreTemplates.observe(this) {
            mBinding.llIssueEvaluateScore.buildChild<ItemIssueEvaluateScoreBinding, FeedbackTemplateProjectDto>(
                it
            ) { _, childBinding, data ->
                childBinding.vm = mViewModel
                childBinding.child = data
            }
        }

        mViewModel.showEvaluateTagTemplates.observe(this) {
            val childCount = mBinding.clIssueEvaluateTag.childCount
            if (childCount > 1) {
                mBinding.clIssueEvaluateTag.removeViews(1, childCount - 1)
            }

            if (it.isNullOrEmpty()) {
                mBinding.clIssueEvaluateTag.visibility(false)
            } else {
                it.forEachIndexed { index, item ->
                    mBinding.clIssueEvaluateTag.addView(
                        DataBindingUtil.inflate<ItemIssueEvaluateTagBinding>(
                            LayoutInflater.from(this@IssueEvaluateActivity),
                            R.layout.item_issue_evaluate_tag,
                            null,
                            false
                        ).apply {
                            child = item
                            root.id = index + 1
                        }.root, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
                    )
                }
                mBinding.flowIssueEvaluateTag.referencedIds = IntArray(it.size) { item -> item + 1 }
                mBinding.clIssueEvaluateTag.visibility(true)
            }
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
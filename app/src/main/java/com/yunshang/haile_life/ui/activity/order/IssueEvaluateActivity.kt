package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.databinding.DataBindingUtil
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.ScreenUtils
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.IssueEvaluateViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.FeedbackTemplateProjectDto
import com.yunshang.haile_life.databinding.ActivityIssueEvaluateBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluatePicBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluateScoreBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluateTagBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.common.PicBrowseActivity
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.loadImage
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import com.yunshang.haile_life.ui.view.dialog.IssueEvaluateSureDialog
import com.yunshang.haile_life.utils.DialogUtils

class IssueEvaluateActivity :
    BaseBusinessActivity<ActivityIssueEvaluateBinding, IssueEvaluateViewModel>(
        IssueEvaluateViewModel::class.java, BR.vm
    ) {

    private val filePermissions = SystemPermissionHelper.readWritePermissions()

    private val permissions = SystemPermissionHelper.cameraPermissions()
        .plus(filePermissions)

    // 文件读取权限
    private val requestFilePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            if (result.values.any { it }) {
                // 授权权限成功
                DialogUtils.showImgSelectorDialog(
                    this@IssueEvaluateActivity,
                    5 - (mViewModel.evaluatePics.value?.size ?: 0),
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

    // 拨打电话权限
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                // 授权权限成功
                mViewModel.orderShopPhone?.let {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$it")))
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

        mViewModel.orderShopPhone = IntentParams.OrderIssueEvaluateParams.parseOrderShopPhone(intent)
    }

    override fun initEvent() {
        super.initEvent()

        // 评分
        mViewModel.evaluateScoreTemplates.observe(this) {
            mBinding.llIssueEvaluateScore.buildChild<ItemIssueEvaluateScoreBinding, FeedbackTemplateProjectDto>(
                it
            ) { _, childBinding, data ->
                childBinding.vm = mViewModel
                childBinding.child = data
            }
        }

        // 标签
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

        // 图片
        mViewModel.evaluatePics.observe(this) {
            mBinding.glRechargeStarfishList.removeAllViews()
            val pS = DimensionUtils.dip2px(this@IssueEvaluateActivity, 4f)
            val picItemWH = (ScreenUtils.screenWidth - DimensionUtils.dip2px(
                this@IssueEvaluateActivity,
                16f
            ) * 2 - pS * 3) / 4
            val inflater = LayoutInflater.from(this@IssueEvaluateActivity)
            it.forEachIndexed { index, url ->
                mBinding.glRechargeStarfishList.addView(
                    DataBindingUtil.inflate<ItemIssueEvaluatePicBinding>(
                        inflater,
                        R.layout.item_issue_evaluate_pic,
                        null,
                        false
                    ).apply {
                        showDel = true

                        root.setPadding(
                            if (0 != index % mBinding.glRechargeStarfishList.columnCount) pS else 0,
                            if (index >= mBinding.glRechargeStarfishList.columnCount) pS else 0,
                            0,
                            0
                        )

                        ivIssueEvaluatePic.loadImage(imgHeadUrl = url)
                        ivIssueEvaluatePicDel.setOnClickListener {
                            mViewModel.evaluatePics.value?.let { picList ->
                                picList.removeAt(index)
                                mViewModel.evaluatePics.value = picList
                            }
                        }
                        root.setOnClickListener {
                            startActivity(
                                Intent(
                                    this@IssueEvaluateActivity,
                                    PicBrowseActivity::class.java
                                ).apply {
                                    putExtras(IntentParams.PicParams.pack(url))
                                })
                        }
                    }.root, picItemWH, picItemWH
                )
            }

            if (it.size < 5) {
                mBinding.glRechargeStarfishList.addView(
                    DataBindingUtil.inflate<ItemIssueEvaluatePicBinding>(
                        inflater,
                        R.layout.item_issue_evaluate_pic,
                        null,
                        false
                    ).apply {
                        showDel = false
                        root.setPadding(
                            if (0 != (it.size) % mBinding.glRechargeStarfishList.columnCount) pS else 0,
                            if (it.size >= mBinding.glRechargeStarfishList.columnCount) pS else 0,
                            0,
                            0
                        )
                        root.setOnClickListener {
                            DialogUtils.checkPermissionDialog(
                                this@IssueEvaluateActivity,
                                supportFragmentManager,
                                permissions,
                                "需要媒体读取和拍照权限来选择图片"
                            ) {
                                requestFilePermission.launch(permissions)
                            }
                        }
                    }.root, picItemWH, picItemWH
                )
            }
        }

        mViewModel.jump.observe(this) {
            finish()
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.btnIssueEvaluateSubmit.setOnClickListener {
            if (!mViewModel.evaluateScoreTemplates.value.isNullOrEmpty() && mViewModel.evaluateScoreTemplates.value!!.any { item -> 0 == item.scoreVal }) {
                SToast.showToast(this, "请先选择评分")
                return@setOnClickListener
            }

            // 差评
            if (!mViewModel.evaluateScoreTemplates.value.isNullOrEmpty() && mViewModel.calculateScoreTotal() < 3) {
                IssueEvaluateSureDialog.Builder(
                    negativeClickListener = {
                        mViewModel.submit(this)
                    }, positiveClickListener = {
                        DialogUtils.checkPermissionDialog(
                            this,
                            supportFragmentManager,
                            SystemPermissionHelper.callPhonePermissions(),
                            "需要权限来拨打电话"
                        ) {
                            requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
                        }
                    }
                ).build().show(supportFragmentManager)
            } else {
                mViewModel.submit(this)
            }
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
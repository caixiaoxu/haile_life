package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
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
import com.yunshang.haile_life.data.agruments.SearchSelectParam
import com.yunshang.haile_life.data.entities.FeedbackTemplateProjectDto
import com.yunshang.haile_life.databinding.ActivityIssueEvaluateBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluatePicBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluateScoreBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluateTagBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.common.PicBrowseActivity
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.loadImage
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import com.yunshang.haile_life.ui.view.dialog.CommonBottomSheetDialog
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.IssueEvaluateSureDialog
import com.yunshang.haile_life.utils.DialogUtils

class IssueEvaluateActivity :
    BaseBusinessActivity<ActivityIssueEvaluateBinding, IssueEvaluateViewModel>(
        IssueEvaluateViewModel::class.java, BR.vm
    ) {

    private var callPhone: String? = ""

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
                if (!callPhone.isNullOrEmpty()) {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$callPhone")))
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

        mViewModel.isAdd = IntentParams.OrderIssueEvaluateParams.parseIsAdd(intent)
        mViewModel.issueEvaluateParams.value?.orderId =
            IntentParams.OrderIssueEvaluateParams.parseOrderId(intent)
        mViewModel.issueEvaluateParams.value?.orderNo =
            IntentParams.OrderIssueEvaluateParams.parseOrderNo(intent)
        if (!mViewModel.isAdd) {
            mViewModel.issueEvaluateParams.value?.goodsId =
                IntentParams.OrderIssueEvaluateParams.parseGoodId(intent)
            mViewModel.issueEvaluateParams.value?.buyerId =
                IntentParams.OrderIssueEvaluateParams.parseBuyerId(intent)
            mViewModel.issueEvaluateParams.value?.sellerId =
                IntentParams.OrderIssueEvaluateParams.parseSellerId(intent)
        }

        mViewModel.orderShopPhone =
            IntentParams.OrderIssueEvaluateParams.parseOrderShopPhone(intent)

        mViewModel.originScoreList = IntentParams.OrderIssueEvaluateParams.parseScoreList(intent)
        mViewModel.originTagList = IntentParams.OrderIssueEvaluateParams.parseTagList(intent)
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
            mViewModel.refreshTagList()
        }

        // 标签
        mViewModel.showEvaluateTagTemplates.observe(this) {
            if (0 < mViewModel.calculateScoreTotal()) {
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
                    mBinding.flowIssueEvaluateTag.referencedIds =
                        IntArray(it.size) { item -> item + 1 }
                    mBinding.clIssueEvaluateTag.visibility(true)
                }
            }
        }

        // 图片
        mViewModel.evaluatePics.observe(this) {
            mBinding.glIssueEvaluatePic.removeAllViews()
            val pS = DimensionUtils.dip2px(this@IssueEvaluateActivity, 4f)
            val picItemWH = (ScreenUtils.screenWidth - DimensionUtils.dip2px(
                this@IssueEvaluateActivity,
                16f
            ) * 2 - pS * 3) / 4
            val inflater = LayoutInflater.from(this@IssueEvaluateActivity)
            val columnCount = mBinding.glIssueEvaluatePic.columnCount
            it.forEachIndexed { index, url ->
                mBinding.glIssueEvaluatePic.addView(
                    DataBindingUtil.inflate<ItemIssueEvaluatePicBinding>(
                        inflater,
                        R.layout.item_issue_evaluate_pic,
                        null,
                        false
                    ).apply {
                        showDel = true

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
                    }.root, GridLayout.LayoutParams().apply {
                        width = picItemWH
                        height = picItemWH
                        setMargins(
                            if (0 != (index % columnCount)) pS else 0,
                            if ((index / columnCount) > 0) pS else 0,
                            0,
                            0
                        )
                    }
                )
            }

            if (it.size < 5) {
                mBinding.glIssueEvaluatePic.addView(
                    DataBindingUtil.inflate<ItemIssueEvaluatePicBinding>(
                        inflater,
                        R.layout.item_issue_evaluate_pic,
                        null,
                        false
                    ).apply {
                        showDel = false
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
                    }.root, GridLayout.LayoutParams().apply {
                        width = picItemWH
                        height = picItemWH
                        setMargins(
                            if (0 != (it.size % columnCount)) pS else 0,
                            if ((it.size / columnCount) > 0) pS else 0,
                            0,
                            0
                        )
                    }
                )
            }
        }

        mViewModel.jump.observe(this) {
            if (1 == it) {
                startActivity(
                    Intent(
                        this@IssueEvaluateActivity,
                        IssueEvaluateSuccessActivity::class.java
                    )
                )
            }
            finish()
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.barIssueEvaluateTitle.setTitle(if (mViewModel.isAdd) R.string.add_evaluate else R.string.issue_evaluate)

        mBinding.btnIssueEvaluateSubmit.setOnClickListener {
            if (!mViewModel.evaluateScoreTemplates.value.isNullOrEmpty() && mViewModel.evaluateScoreTemplates.value!!.any { item -> 0 == item.scoreVal }) {
                SToast.showToast(this, "请先选择评分")
                return@setOnClickListener
            }

            // 差评
            if (!mViewModel.isAdd && !mViewModel.evaluateScoreTemplates.value.isNullOrEmpty() && mViewModel.calculateScoreTotal() == 3) {
                IssueEvaluateSureDialog.Builder(
                    negativeClickListener = {
                        mViewModel.submit(this)
                    }, positiveClickListener = {
                        if (!mViewModel.orderShopPhone.isNullOrEmpty()) {
                            mViewModel.orderShopPhone?.split(",")
                                ?.mapIndexed { index, phone -> SearchSelectParam(index, phone) }
                                ?.let { phoneList ->
                                    CommonBottomSheetDialog.Builder("", phoneList).apply {
                                        selectModel = 1
                                        onValueSureListener =
                                            object :
                                                CommonBottomSheetDialog.OnValueSureListener<SearchSelectParam> {
                                                override fun onValue(data: SearchSelectParam?) {
                                                    CommonDialog.Builder("是否拨打电话").apply {
                                                        title =
                                                            com.lsy.framelib.utils.StringUtils.getString(
                                                                R.string.tip
                                                            )
                                                        negativeTxt =
                                                            com.lsy.framelib.utils.StringUtils.getString(
                                                                R.string.cancel
                                                            )
                                                        setPositiveButton(
                                                            com.lsy.framelib.utils.StringUtils.getString(
                                                                R.string.sure
                                                            )
                                                        ) {
                                                            callPhone = data?.name
                                                            requestPermission.launch(
                                                                SystemPermissionHelper.callPhonePermissions()
                                                            )
                                                        }
                                                    }.build().show(supportFragmentManager)
                                                }
                                            }
                                    }.build().show(supportFragmentManager)
                                }
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
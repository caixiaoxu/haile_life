package com.yunshang.haile_life.ui.activity.order

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.ScreenUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.EvaluateDetailsViewModel
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.EvaluateTagTemplate
import com.yunshang.haile_life.data.entities.FeedbackOrderReplayDtos
import com.yunshang.haile_life.data.entities.FeedbackTemplateProjectDto
import com.yunshang.haile_life.databinding.ActivityEvaluateDetailsBinding
import com.yunshang.haile_life.databinding.ItemEvaluateDetailsPicBinding
import com.yunshang.haile_life.databinding.ItemEvaluateDetailsReplyBinding
import com.yunshang.haile_life.databinding.ItemEvaluateDetailsScoreBinding
import com.yunshang.haile_life.databinding.ItemIssueEvaluateTagBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.common.PicBrowseActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.loadImage
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility

class EvaluateDetailsActivity :
    BaseBusinessActivity<ActivityEvaluateDetailsBinding, EvaluateDetailsViewModel>(
        EvaluateDetailsViewModel::class.java, BR.vm
    ) {

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemEvaluateDetailsReplyBinding, FeedbackOrderReplayDtos>(
            R.layout.item_evaluate_details_reply, BR.item
        ) { mItemBinding, _, item ->
            val pH: Int
            if (1 == item.type || 2 == item.type) {
                mItemBinding?.root?.setBackgroundResource(R.drawable.shape_sf7f8fb_r4)
                pH = DimensionUtils.dip2px(
                    this@EvaluateDetailsActivity,
                    12f
                )
                mItemBinding?.root?.setPadding(pH)
            } else {
                pH = 0
                mItemBinding?.root?.setBackgroundResource(0)
                mItemBinding?.root?.setPadding(0)
            }

            // 图片
            mItemBinding?.glEvaluateReplyPic?.let {
                val space = DimensionUtils.dip2px(this@EvaluateDetailsActivity, 14f)
                val itemWH = (ScreenUtils.screenWidth - DimensionUtils.dip2px(
                    this@EvaluateDetailsActivity,
                    16f
                ) * 2 - space * 3 - pH * 2) / 4
                buildImageView(mItemBinding.glEvaluateReplyPic, item.pictures, itemWH, space)
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_evaluate_details

    override fun backBtn(): View = mBinding.barEvaluateDetailsTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.orderId = IntentParams.OrderEvaluateDetailsParams.parseOrderId(intent)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.evaluateDetails.observe(this) {
            // 评分
            mBinding.llEvaluateDetailsScore.buildChild<ItemEvaluateDetailsScoreBinding, FeedbackTemplateProjectDto>(
                it?.feedbackProjectListDtos
            ) { _, childBinding, data ->
                childBinding.child = data
            }
            // 标签
            val childCount = mBinding.clEvaluateDetailsTag.childCount
            if (childCount > 1) {
                mBinding.clEvaluateDetailsTag.removeViews(1, childCount - 1)
            }
            if (it?.feedbackOrderTagModels.isNullOrEmpty()) {
                mBinding.clEvaluateDetailsTag.visibility(false)
            } else {
                it.feedbackOrderTagModels!!.map { item ->
                    EvaluateTagTemplate(
                        item.id,
                        item.tagName
                    )
                }.forEachIndexed { index, item ->
                    mBinding.clEvaluateDetailsTag.addView(
                        DataBindingUtil.inflate<ItemIssueEvaluateTagBinding>(
                            LayoutInflater.from(this@EvaluateDetailsActivity),
                            R.layout.item_issue_evaluate_tag,
                            null,
                            false
                        ).apply {
                            child = item
                            root.id = index + 1
                        }.root,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT,
                        ConstraintLayout.LayoutParams.WRAP_CONTENT
                    )
                }
                mBinding.flowEvaluateDetailsTag.referencedIds =
                    IntArray(it.feedbackOrderTagModels.size) { item -> item + 1 }
                mBinding.clEvaluateDetailsTag.visibility(true)
            }

            // 图片
            val space = DimensionUtils.dip2px(this@EvaluateDetailsActivity, 14f)
            val itemWH = (ScreenUtils.screenWidth - DimensionUtils.dip2px(
                this@EvaluateDetailsActivity,
                16f
            ) * 2 - space * 3) / 4
            buildImageView(mBinding.glEvaluatePicList, it?.pictures, itemWH, space)

            // 回复
            mAdapter.refreshList(it.feedbackOrderReplayDtos?.toMutableList())
        }
    }

    private fun buildImageView(
        gridLayout: GridLayout,
        pictures: List<String>?,
        itemWH: Int,
        space: Int
    ) {
        gridLayout.removeAllViews()
        if (pictures.isNullOrEmpty()) {
            gridLayout.visibility(false)
        } else {
            val inflater = LayoutInflater.from(this@EvaluateDetailsActivity)
            val columnCount = gridLayout.columnCount
            pictures.forEachIndexed { index, url ->
                gridLayout.addView(
                    DataBindingUtil.inflate<ItemEvaluateDetailsPicBinding>(
                        inflater,
                        R.layout.item_evaluate_details_pic,
                        null,
                        false
                    ).apply {
                        ivEvaluateDetailsPic.loadImage(imgHeadUrl = url)
                        root.setOnClickListener {
                            startActivity(
                                Intent(
                                    this@EvaluateDetailsActivity,
                                    PicBrowseActivity::class.java
                                ).apply {
                                    putExtras(IntentParams.PicParams.pack(url))
                                })
                        }
                    }.root, GridLayout.LayoutParams().apply {
                        width = itemWH
                        height = itemWH
                        setMargins(
                            if (0 != (index % columnCount)) space else 0,
                            if ((index / columnCount) > 0) space else 0,
                            0,
                            0
                        )
                    }
                )
            }
            gridLayout.visibility(true)
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.rvEvaluateDetails.layoutManager = LinearLayoutManager(this)
        mBinding.rvEvaluateDetails.adapter = mAdapter

        mBinding.btnEvaluateDetailAdd.setOnClickListener {
            startActivity(
                Intent(
                    this@EvaluateDetailsActivity,
                    IssueEvaluateActivity::class.java
                ).apply {
                    mViewModel.evaluateDetails.value?.let { details ->
                        putExtras(
                            IntentParams.OrderIssueEvaluateParams.pack(
                                details.orderId,
                                details.orderNo,
                                details.buyerId,
                                details.sellerId,
                                details.serviceTelephone?.split(",")?.first(),
                                isAdd = true,
                                scoreList = details.feedbackProjectListDtos,
                                tagList = details.feedbackOrderTagModels
                            )
                        )
                    }
                })
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
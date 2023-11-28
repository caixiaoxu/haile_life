package com.yunshang.haile_life.ui.activity.personal

import android.content.Intent
import android.graphics.Color
import android.view.View
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.ScreenUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.FaultRepairsDetailViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.ReplyDTOS
import com.yunshang.haile_life.databinding.ActivityFaultRepairsRecordDetailBinding
import com.yunshang.haile_life.databinding.ItemFaultRepairsRecordDetailReplayBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.common.PicBrowseActivity
import com.yunshang.haile_life.ui.view.RoundImageView
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.loadImage
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility

class FaultRepairsRecordDetailActivity :
    BaseBusinessActivity<ActivityFaultRepairsRecordDetailBinding, FaultRepairsDetailViewModel>(
        FaultRepairsDetailViewModel::class.java, BR.vm
    ) {

    override fun layoutId(): Int = R.layout.activity_fault_repairs_record_detail

    override fun backBtn(): View = mBinding.barFaultRepairsRecordDetailTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.repairsId = IntentParams.IdParams.parseId(intent)
    }

    override fun initEvent() {
        super.initEvent()

        mViewModel.recordDetail.observe(this) { detail ->
            mBinding.clFaultRepairsRecordDetailFaultPic.removeViews(
                1,
                mBinding.clFaultRepairsRecordDetailFaultPic.childCount - 1
            )
            val picItemWH = (ScreenUtils.screenWidth - DimensionUtils.dip2px(
                this@FaultRepairsRecordDetailActivity,
                16f
            ) * 2 - DimensionUtils.dip2px(this@FaultRepairsRecordDetailActivity, 12f) * 3) / 4
            if (detail.pics.isNullOrEmpty()) {
                mBinding.clFaultRepairsRecordDetailFaultPic.visibility(false)
            } else {
                mBinding.clFaultRepairsRecordDetailFaultPic.visibility(true)
                val picItemRadius =
                    DimensionUtils.dip2px(this@FaultRepairsRecordDetailActivity, 12f)
                detail.pics.forEachIndexed { index, url ->
                    mBinding.clFaultRepairsRecordDetailFaultPic.addView(RoundImageView(this@FaultRepairsRecordDetailActivity).apply {
                        radius = picItemRadius.toFloat()
                        id = index + 1
                        loadImage(imgHeadUrl = url)
                        setOnClickListener {
                            startActivity(
                                Intent(
                                    this@FaultRepairsRecordDetailActivity,
                                    PicBrowseActivity::class.java
                                ).apply {
                                    putExtras(IntentParams.PicParams.pack(url))
                                })
                        }
                    }, picItemWH, picItemWH)
                }
                mBinding.flowFaultRepairsRecordDetailFaultPic.referencedIds =
                    IntArray(detail.pics.size) { item -> item + 1 }
            }

            mBinding.llFaultRepairsRecordDetailReply.buildChild<ItemFaultRepairsRecordDetailReplayBinding, ReplyDTOS>(
                detail.replyDTOS
            ) { _, childBinding, data ->
                childBinding.item = data
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
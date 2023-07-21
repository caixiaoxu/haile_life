package com.yunshang.haile_life.ui.activity.shop

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.StarfishRefundRecordViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.StarfishRefundRecordEntity
import com.yunshang.haile_life.databinding.ActivityStarfishRefundRecordBinding
import com.yunshang.haile_life.databinding.ItemRechargeStarfishRefundListBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView

class StarfishRefundRecordActivity :
    BaseBusinessActivity<ActivityStarfishRefundRecordBinding, StarfishRefundRecordViewModel>(
        StarfishRefundRecordViewModel::class.java
    ) {

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemRechargeStarfishRefundListBinding, StarfishRefundRecordEntity>(
            R.layout.item_recharge_starfish_refund_list, BR.item
        ) { mItemBinding, _, item ->
            mItemBinding?.root?.setOnClickListener {
                startActivity(
                    Intent(
                        this@StarfishRefundRecordActivity, StarfishRefundDetailActivity::class.java
                    ).apply {
                        putExtras(IntentParams.StarfishRefundDetailParams.pack(item.id))
                    })
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_starfish_refund_record

    override fun backBtn(): View =
        mBinding.includeTitleRefreshList.barTitleRefreshListTitle.getBackBtn()

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.includeTitleRefreshList.barTitleRefreshListTitle.setTitle(R.string.refund_record)
        mBinding.includeTitleRefreshList.rvTitleRefreshList.setPadding(
            0, DimensionUtils.dip2px(this, 8f), 0, 0
        )
        mBinding.includeTitleRefreshList.rvTitleRefreshList.layoutManager =
            LinearLayoutManager(this)
        mBinding.includeTitleRefreshList.rvTitleRefreshList.adapter = mAdapter
        ContextCompat.getDrawable(this, R.drawable.divder_efefef)?.let {
            mBinding.includeTitleRefreshList.rvTitleRefreshList.addItemDecoration(
                DividerItemDecoration(
                    this@StarfishRefundRecordActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.includeTitleRefreshList.rvTitleRefreshList.requestData = object :
            CommonRefreshRecyclerView.OnRequestDataListener<StarfishRefundRecordEntity>() {
            override fun requestData(
                isRefresh: Boolean,
                page: Int,
                pageSize: Int,
                callBack: (responseList: ResponseList<out StarfishRefundRecordEntity>?) -> Unit
            ) {
//                if (isRefresh) {
//                    mViewModel.searchDate = DateTimeUtils.curMonthFirst
//                }
                mViewModel.requestRechargeRefundList(page, pageSize, callBack)
            }
//          按月分查寻，不做了去掉
//            override fun onCommonDeal(
//                responseList: ResponseList<out StarfishRefundRecordEntity>,
//                isRefresh: Boolean
//            ): Boolean {
//
//                if (responseList.items.size < responseList.pageSize) {
//                    mBinding.includeTitleRefreshList.rvTitleRefreshList.page = 1
//                    mViewModel.searchDate = Calendar.getInstance().apply {
//                        time = mViewModel.searchDate
//                        add(Calendar.MONTH, -1)
//                    }.time
//                }
//
//                // 刷新数据
//                mAdapter.refreshList(responseList.items, isRefresh)
//                return true
//            }
        }
    }

    override fun initData() {
        mBinding.includeTitleRefreshList.rvTitleRefreshList.requestRefresh()
    }
}
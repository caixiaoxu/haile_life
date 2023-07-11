package com.yunshang.haile_life.ui.activity.shop

import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.network.response.ResponseList
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.RechargeStarfishDetailListViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.RechargeStarfishDetailEntity
import com.yunshang.haile_life.databinding.ActivityRechargeStarfishDetailListBinding
import com.yunshang.haile_life.databinding.ItemRechargeStarfishDetailListBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView

class RechargeStarfishDetailListActivity :
    BaseBusinessActivity<ActivityRechargeStarfishDetailListBinding, RechargeStarfishDetailListViewModel>(
        RechargeStarfishDetailListViewModel::class.java
    ) {

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemRechargeStarfishDetailListBinding, RechargeStarfishDetailEntity>(
            R.layout.item_recharge_starfish_detail_list, BR.item
        ) { mItemBinding, pos, item ->
        }
    }

    override fun layoutId(): Int = R.layout.activity_recharge_starfish_detail_list

    override fun backBtn(): View =
        mBinding.includeTitleRefreshList.barTitleRefreshListTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.shopId = IntentParams.RechargeStarfishDetailListParams.parseShopId(intent)
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.includeTitleRefreshList.rvTitleRefreshList.layoutManager =
            LinearLayoutManager(this)
        mBinding.includeTitleRefreshList.rvTitleRefreshList.adapter = mAdapter
        ContextCompat.getDrawable(this, R.drawable.divder_efefef)?.let {
            mBinding.includeTitleRefreshList.rvTitleRefreshList.addItemDecoration(
                DividerItemDecoration(
                    this@RechargeStarfishDetailListActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.includeTitleRefreshList.rvTitleRefreshList.requestData = object :
            CommonRefreshRecyclerView.OnRequestDataListener<RechargeStarfishDetailEntity>() {
            override fun requestData(
                isRefresh: Boolean,
                page: Int,
                pageSize: Int,
                callBack: (responseList: ResponseList<out RechargeStarfishDetailEntity>?) -> Unit
            ) {
                mViewModel.requestRechargeDetailList(page, pageSize, callBack)
            }
        }
    }

    override fun initData() {
        mBinding.includeTitleRefreshList.rvTitleRefreshList.requestRefresh()
    }
}
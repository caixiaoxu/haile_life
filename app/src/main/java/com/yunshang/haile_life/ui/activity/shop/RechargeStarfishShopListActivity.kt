package com.yunshang.haile_life.ui.activity.shop

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.async.LiveDataBus
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.RechargeStarfishShopListViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.RechargeShopListItem
import com.yunshang.haile_life.databinding.ActivityRechargeStarfishShopListBinding
import com.yunshang.haile_life.databinding.ItemRechargeStartfishShopListBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter

class RechargeStarfishShopListActivity :
    BaseBusinessActivity<ActivityRechargeStarfishShopListBinding, RechargeStarfishShopListViewModel>(
        RechargeStarfishShopListViewModel::class.java
    ) {

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemRechargeStartfishShopListBinding, RechargeShopListItem>(
            R.layout.item_recharge_startfish_shop_list, BR.item
        ) { mItemBinding, _, item ->
            mItemBinding?.root?.setOnClickListener {
                startActivity(
                    Intent(
                        this@RechargeStarfishShopListActivity,
                        RechargeStarfishDetailListActivity::class.java
                    ).apply {
                        putExtras(IntentParams.RechargeStarfishDetailListParams.pack(item.shopId))
                    })
            }

            mItemBinding?.btnRechargeStarfish?.setOnClickListener {
                startActivity(
                    Intent(
                        this@RechargeStarfishShopListActivity,
                        RechargeStarfishActivity::class.java
                    ).apply {
                        putExtras(IntentParams.RechargeStarfishParams.pack(item.shopId))
                    })
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_recharge_starfish_shop_list

    override fun backBtn(): View = mBinding.includeTitleList.barTitleListTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()
        mViewModel.rechargeShopList.observe(this) {
            mAdapter.refreshList(it.items, true)
        }
        LiveDataBus.with(BusEvents.RECHARGE_SUCCESS_STATUS)?.observe(this) {
            mViewModel.requestData()
        }
    }

    override fun initView() {
        mBinding.includeTitleList.barTitleListTitle.setTitle(R.string.starfish)
        window.statusBarColor = Color.WHITE
        mBinding.includeTitleList.barTitleListTitle
        mBinding.includeTitleList.barTitleListTitle.getRightBtn().run {
            setText(R.string.refund_record)
            setOnClickListener {
                startActivity(
                    Intent(
                        this@RechargeStarfishShopListActivity,
                        StarfishRefundRecordActivity::class.java
                    )
                )
            }
        }

        mBinding.includeTitleList.rvTitleListList.layoutManager = LinearLayoutManager(this)
        ContextCompat.getDrawable(this, R.drawable.divide_size8)?.let {
            mBinding.includeTitleList.rvTitleListList.addItemDecoration(
                DividerItemDecoration(
                    this@RechargeStarfishShopListActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.includeTitleList.rvTitleListList.adapter = mAdapter
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
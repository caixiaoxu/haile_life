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
import com.yunshang.haile_life.business.vm.StarfishRefundListViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.StarfishRefundApplyEntity
import com.yunshang.haile_life.databinding.ActivityStarfishRefundListBinding
import com.yunshang.haile_life.databinding.ItemStarfishRefundListBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter

class StarfishRefundListActivity :
    BaseBusinessActivity<ActivityStarfishRefundListBinding, StarfishRefundListViewModel>(
        StarfishRefundListViewModel::class.java
    ) {
    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemStarfishRefundListBinding, StarfishRefundApplyEntity>(
            R.layout.item_starfish_refund_list, BR.item,
        ) { mItemBinding, _, item ->
            mItemBinding?.cbStarfishRefundList?.setOnCheckedChangeListener { _, isChecked ->
                item.isCheck = isChecked
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_starfish_refund_list

    override fun backBtn(): View = mBinding.barStarfishRefundListTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.refundId = IntentParams.ScanOrderParams.parseCode(intent)
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.refundApplyList.observe(this) {
            mAdapter.refreshList(it, true)
        }
        LiveDataBus.with(BusEvents.STARFISH_REFUND_STATUS)?.observe(this){
            finish()
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.rvStarfishRefundListList.layoutManager =
            LinearLayoutManager(this)
        ContextCompat.getDrawable(this, R.drawable.divide_size8)?.let {
            mBinding.rvStarfishRefundListList.addItemDecoration(
                DividerItemDecoration(
                    this@StarfishRefundListActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.rvStarfishRefundListList.adapter = mAdapter

        mBinding.btnStarfishRefundApply.setOnClickListener {
            startActivity(
                Intent(
                    this@StarfishRefundListActivity,
                    StarfishRefundActivity::class.java
                ).apply {
                    mViewModel.refundApplyList.value?.filter { item -> item.isCheck }
                        ?.let { arr ->
                            var total = 0
                            var rate = 1
                            arr.forEach {
                                rate = it.exchangeRate
                                total += it.totalAmount
                            }
                            putExtras(IntentParams.StarfishRefundParams.pack(arr.map { item -> item.shopId }
                                .distinct().toIntArray(), total * 1.0 / rate))
                        }
                }
            )
        }
    }

    override fun initData() {
        mViewModel.requestStarfishRefundApplyList()
    }
}
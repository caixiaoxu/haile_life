package com.yunshang.haile_life.ui.activity.personal

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.network.response.ResponseList
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.FaultRepairsRecordViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.FaultRepairsRecordEntity
import com.yunshang.haile_life.databinding.ActivityFaultRepairsRecordBinding
import com.yunshang.haile_life.databinding.ItemFaultRepairsRecordBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView

class FaultRepairsRecordActivity :
    BaseBusinessActivity<ActivityFaultRepairsRecordBinding, FaultRepairsRecordViewModel>(
        FaultRepairsRecordViewModel::class.java
    ) {

    override fun layoutId(): Int = R.layout.activity_fault_repairs_record

    override fun backBtn(): View = mBinding.barFaultRepairsRecordTitle.getBackBtn()

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.rvFaultRepairsRecord.layoutManager = LinearLayoutManager(this)
        mBinding.rvFaultRepairsRecord.adapter =
            CommonRecyclerAdapter<ItemFaultRepairsRecordBinding, FaultRepairsRecordEntity>(
                R.layout.item_fault_repairs_record,
                BR.item
            ) { mItemBinding, _, item ->
                mItemBinding?.root?.setOnClickListener {

                    startActivity(
                        Intent(
                            this@FaultRepairsRecordActivity,
                            FaultRepairsRecordDetailActivity::class.java
                        ).apply {
                            item.id?.let { id ->
                                putExtras(IntentParams.IdParams.pack(id))
                            }
                        })
                }
            }
        mBinding.rvFaultRepairsRecord.requestData =
            object : CommonRefreshRecyclerView.OnRequestDataListener<FaultRepairsRecordEntity>() {
                override fun requestData(
                    isRefresh: Boolean,
                    page: Int,
                    pageSize: Int,
                    callBack: (responseList: ResponseList<out FaultRepairsRecordEntity>?) -> Unit
                ) {
                    mViewModel.requestData(page, pageSize, callBack)
                }
            }
    }

    override fun initData() {
        mBinding.rvFaultRepairsRecord.requestRefresh()
    }
}
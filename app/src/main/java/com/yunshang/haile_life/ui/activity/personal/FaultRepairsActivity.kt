package com.yunshang.haile_life.ui.activity.personal

import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.FaultRepairsViewModel
import com.yunshang.haile_life.databinding.ActivityFaultRepairsBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class FaultRepairsActivity : BaseBusinessActivity<ActivityFaultRepairsBinding, FaultRepairsViewModel>(
    FaultRepairsViewModel::class.java,BR.vm
) {

    override fun layoutId(): Int  = R.layout.activity_fault_repairs

    override fun initView() {
    }

    override fun initData() {
    }
}
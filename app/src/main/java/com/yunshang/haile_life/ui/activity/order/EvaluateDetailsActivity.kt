package com.yunshang.haile_life.ui.activity.order

import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.EvaluateDetailsViewModel
import com.yunshang.haile_life.business.vm.IssueEvaluateViewModel
import com.yunshang.haile_life.databinding.ActivityEvaluateDetailsBinding
import com.yunshang.haile_life.databinding.ActivityIssueEvaluateBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class EvaluateDetailsActivity : BaseBusinessActivity<ActivityEvaluateDetailsBinding, EvaluateDetailsViewModel>(
    EvaluateDetailsViewModel::class.java,BR.vm
) {

    override fun layoutId(): Int = R.layout.activity_evaluate_details

    override fun initView() {
    }

    override fun initData() {
    }
}
package com.yunshang.haile_life.ui.activity.order

import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.IssueEvaluateViewModel
import com.yunshang.haile_life.databinding.ActivityIssueEvaluateBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class IssueEvaluateActivity : BaseBusinessActivity<ActivityIssueEvaluateBinding, IssueEvaluateViewModel>(
    IssueEvaluateViewModel::class.java,BR.vm
) {

    override fun layoutId(): Int = R.layout.activity_issue_evaluate

    override fun initView() {
    }

    override fun initData() {
    }
}
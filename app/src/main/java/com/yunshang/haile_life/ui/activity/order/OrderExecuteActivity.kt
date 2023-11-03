package com.yunshang.haile_life.ui.activity.order

import android.view.View
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.OrderExecuteViewModel
import com.yunshang.haile_life.databinding.ActivityOrderExecuteBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class OrderExecuteActivity : BaseBusinessActivity<ActivityOrderExecuteBinding, OrderExecuteViewModel>(
    OrderExecuteViewModel::class.java,BR.vm
) {

    override fun isFullScreen(): Boolean = true

    override fun layoutId(): Int = R.layout.activity_order_execute

    override fun backBtn(): View = mBinding.barOrderExecuteTitle.getBackBtn()

    override fun initView() {
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)
    }

    override fun initData() {
    }
}
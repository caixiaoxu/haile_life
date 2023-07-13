package com.yunshang.haile_life.ui.activity.personal

import android.content.Intent
import android.view.View
import com.lsy.framelib.utils.AppManager
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.CloseAccountViewModel
import com.yunshang.haile_life.databinding.ActivityCloseAccountBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity

class CloseAccountActivity :
    BaseBusinessActivity<ActivityCloseAccountBinding, CloseAccountViewModel>(
        CloseAccountViewModel::class.java, BR.vm
    ) {

    override fun layoutId(): Int = R.layout.activity_close_account

    override fun backBtn(): View = mBinding.barCloseAccountTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()
        mViewModel.jump.observe(this) {
            AppManager.finishAllActivity()
            startActivity(Intent(this@CloseAccountActivity, LoginActivity::class.java))
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }
}
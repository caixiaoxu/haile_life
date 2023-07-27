package com.yunshang.haile_life.ui.activity.personal

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.View
import com.lsy.framelib.utils.AppManager
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.CloseAccountViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.ActivityCloseAccountBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog

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
            startActivity(Intent(this@CloseAccountActivity, MainActivity::class.java).apply {
                putExtras(IntentParams.DefaultPageParams.pack(0))
            })
        }
    }

    override fun initView() {
        mBinding.btnCloseAccountSubmit.setOnClickListener {
            CommonDialog.Builder("注销后您的身份、账号信息、交易信息、钱包资产等都会被清空且无法找回请谨慎操作。").apply {
                setNegativeButton("确定注销") {
                    mViewModel.closeAccount()
                }
                positiveTxt = "我再想想"
            }.build().show(supportFragmentManager)
        }
        mViewModel.timer.start()
    }

    override fun initData() {
    }

    override fun onDestroy() {
        mViewModel.timer.cancel()
        super.onDestroy()
    }
}
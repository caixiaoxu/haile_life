package com.yunshang.haile_life.ui.activity.personal

import android.graphics.Color
import android.view.View
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.UpdateNickNameViewModel
import com.yunshang.haile_life.databinding.ActivityUpdateNickNameBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class UpdateNickNameActivity :
    BaseBusinessActivity<ActivityUpdateNickNameBinding, UpdateNickNameViewModel>(
        UpdateNickNameViewModel::class.java, BR.vm
    ) {
    override fun layoutId(): Int = R.layout.activity_update_nick_name

    override fun backBtn(): View = mBinding.barUpdateNicknameTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()
        mSharedViewModel.userInfo.observe(this) {
            it?.let { user ->
                mViewModel.nickName.value = user.nickname
            }
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.btnUpdateNickNameSave.setOnClickListener {
            mViewModel.updatePersonalName(it) {
                mSharedViewModel.requestUserInfoAsync()
                finish()
            }
        }
    }

    override fun initData() {
    }
}
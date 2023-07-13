package com.yunshang.haile_life.ui.activity.personal

import android.content.Intent
import android.graphics.Color
import android.view.View
import com.lsy.framelib.utils.SToast
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.PersonalInfoViewModel
import com.yunshang.haile_life.databinding.ActivityPersonalInfoBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.utils.DialogUtils

class PersonalInfoActivity :
    BaseBusinessActivity<ActivityPersonalInfoBinding, PersonalInfoViewModel>(
        PersonalInfoViewModel::class.java,
    ) {

    override fun layoutId(): Int = R.layout.activity_personal_info

    override fun backBtn(): View = mBinding.barPersonalInfoTitle.getBackBtn()

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.clPersonalInfoHeadImage.setOnClickListener {
            DialogUtils.showImgSelectorDialog(this@PersonalInfoActivity, 1) { isSuccess, result ->
                if (isSuccess && !result.isNullOrEmpty()) {
                    mViewModel.uploadHeadIcon(result[0].cutPath) {
                        if (it) {
                            mSharedViewModel.requestUserInfoAsync()
                        } else {
                            SToast.showToast(this@PersonalInfoActivity, R.string.update_failure)
                        }
                    }
                }
            }
        }

        mBinding.clPersonalInfoNickname.setOnClickListener {
            startActivity(Intent(this@PersonalInfoActivity, UpdateNickNameActivity::class.java))
        }
    }

    override fun initData() {
        mBinding.shared = mSharedViewModel
    }
}
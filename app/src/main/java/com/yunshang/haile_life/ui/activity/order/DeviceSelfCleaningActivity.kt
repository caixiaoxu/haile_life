package com.yunshang.haile_life.ui.activity.order

import androidx.databinding.library.baseAdapters.BR
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.DeviceSelfCleaningViewModel
import com.yunshang.haile_life.databinding.ActivityDeviceSelfCleaningBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity

class DeviceSelfCleaningActivity :
    BaseBusinessActivity<ActivityDeviceSelfCleaningBinding, DeviceSelfCleaningViewModel>(
        DeviceSelfCleaningViewModel::class.java, BR.vm
    ) {

    override fun isFullScreen(): Boolean = true

    override fun layoutId(): Int = R.layout.activity_device_self_cleaning

    override fun initView() {
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

    }

    override fun initData() {
        mViewModel.requestData()
    }
}
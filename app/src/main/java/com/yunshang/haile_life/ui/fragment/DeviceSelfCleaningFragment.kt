package com.yunshang.haile_life.ui.fragment

import android.content.Intent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.databinding.library.baseAdapters.BR
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.DeviceSelfCleaningViewModel
import com.yunshang.haile_life.business.vm.OrderStatusViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.databinding.FragmentDeviceSelfCleaningBinding
import com.yunshang.haile_life.ui.activity.MainActivity


class DeviceSelfCleaningFragment :
    BaseBusinessFragment<FragmentDeviceSelfCleaningBinding, DeviceSelfCleaningViewModel>(
        DeviceSelfCleaningViewModel::class.java, BR.vm
    ) {

    val mActivityViewModel by lazy {
        getActivityViewModelProvider(requireActivity())[OrderStatusViewModel::class.java]
    }

    override fun layoutId(): Int = R.layout.fragment_device_self_cleaning

    override fun backBtn(): View = mBinding.barOrderDeviceSelfCleaningTitle.getBackBtn()

    override fun onBackListener() {
        requireActivity().finish()
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            putExtras(IntentParams.DefaultPageParams.pack(3))
        })
    }

    override fun initView() {
        mBinding.avm = mActivityViewModel
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

        mBinding.refreshView.requestData = {
            mActivityViewModel.requestData()
        }

        mBinding.ivDeviceSelfCleaningMainRun.startAnimation(
            RotateAnimation(
                0f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            ).apply {
                duration = 2400
                interpolator = LinearInterpolator()
                repeatCount = Animation.INFINITE
            })
    }

    override fun initData() {
    }
}
package com.yunshang.haile_life.ui.activity.device

import android.graphics.Color
import android.view.View
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.CardBindingViewModel
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityCardBindingBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.common.CustomCaptureActivity
import timber.log.Timber

class CardBindingActivity : BaseBusinessActivity<ActivityCardBindingBinding, CardBindingViewModel>(
    CardBindingViewModel::class.java, BR.vm
) {
    // 扫码相机启动器
    private val scanCodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let {
            Timber.i("二维码：$it")
            mViewModel.cardSn.value = it
        }
    }

    private val scanOptions: ScanOptions by lazy {
        ScanOptions().apply {
            captureActivity = CustomCaptureActivity::class.java
            setPrompt("请对准二维码")//提示语
            setOrientationLocked(true)
            setCameraId(0) // 选择摄像头
            setBeepEnabled(true) // 开启声音
        }
    }

    override fun layoutId(): Int = R.layout.activity_card_binding

    override fun backBtn(): View = mBinding.barCardBindingTitle.getBackBtn()
    override fun initIntent() {
        super.initIntent()
        mViewModel.phone.value = mSharedViewModel.userInfo.value?.phone
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.jump.observe(this) {
            finish()
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.ibCardBindingScan.setOnClickListener {
            scanCodeLauncher.launch(scanOptions)
        }
    }

    override fun initData() {
    }

    override fun onDestroy() {
        mViewModel.timer?.cancel()
        super.onDestroy()
    }
}
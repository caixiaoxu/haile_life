package com.yunshang.haile_life.ui.activity.device

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.king.camera.scan.CameraScan
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.CardBindingViewModel
import com.yunshang.haile_life.databinding.ActivityCardBindingBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.common.WeChatQRCodeScanActivity
import com.yunshang.haile_life.utils.DialogUtils
import timber.log.Timber

class CardBindingActivity : BaseBusinessActivity<ActivityCardBindingBinding, CardBindingViewModel>(
    CardBindingViewModel::class.java, BR.vm
) {

    private val permissions = SystemPermissionHelper.cameraPermissions()
        .plus(SystemPermissionHelper.readWritePermissions())

    // 权限
    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            if (result.values.any { it }) {
                // 授权权限成功
                startQRActivity(false)
            } else {
                // 授权失败
                SToast.showToast(this, R.string.empty_permission)
            }
        }

    private fun startQRActivity(isOne: Boolean) {
        startQRCodeScan.launch(
            Intent(
                this,
                WeChatQRCodeScanActivity::class.java
            ).apply {
                putExtra("isOne", isOne)
            })
    }

    // 二维码
    private val startQRCodeScan =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                // 扫码结果
                CameraScan.parseScanResult(result.data)?.let {
                    Timber.i("二维码：$it")
                    mViewModel.cardSn.value = it
                }
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

    override fun onResume() {
        super.onResume()
        LiveDataBus.with(BusEvents.SCAN_CHANGE_STATUS, Boolean::class.java)?.observe(this) {
            startQRActivity(it)
        }
    }

    override fun onPause() {
        super.onPause()
        LiveDataBus.remove(BusEvents.SCAN_CHANGE_STATUS)
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.ibCardBindingScan.setOnClickListener {
            DialogUtils.checkPermissionDialog(
                this,
                supportFragmentManager,
                permissions,
                "需要相机权限和媒体读取权限来扫描或读取卡片码"
            ) {
                requestMultiplePermission.launch(permissions)
            }
        }
    }

    override fun initData() {
    }

    override fun onDestroy() {
        mViewModel.timer?.cancel()
        super.onDestroy()
    }
}
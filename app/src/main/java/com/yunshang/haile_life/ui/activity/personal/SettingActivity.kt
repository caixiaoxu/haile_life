package com.yunshang.haile_life.ui.activity.personal

import android.content.Intent
import android.graphics.Color
import android.view.View
import com.lsy.framelib.utils.AppManager
import com.lsy.framelib.utils.AppPackageUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.SettingViewModel
import com.yunshang.haile_life.data.entities.AppVersionEntity
import com.yunshang.haile_life.data.model.OnDownloadProgressListener
import com.yunshang.haile_life.databinding.ActivitySettingBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity
import com.yunshang.haile_life.ui.view.dialog.UpdateAppDialog
import timber.log.Timber
import java.io.File

class SettingActivity : BaseBusinessActivity<ActivitySettingBinding, SettingViewModel>(
    SettingViewModel::class.java, BR.vm
) {
    override fun layoutId(): Int = R.layout.activity_setting

    override fun backBtn(): View = mBinding.barSettingTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()
        mViewModel.appVersionInfo.observe(this) { version ->
            if (version.needUpdate || version.forceUpdate) {
                mBinding.tvSettingNewVersion.visibility = View.VISIBLE
                mBinding.llSettingVersion.setOnClickListener {
                    showUpdateDialog(version)
                }
            } else mBinding.tvSettingNewVersion.visibility = View.GONE
        }
        mViewModel.jump.observe(this) {
            AppManager.finishAllActivity()
            startActivity(Intent(this@SettingActivity, LoginActivity::class.java))
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.tvSettingPersonalInfo.setOnClickListener {
            startActivity(Intent(this@SettingActivity, PersonalInfoActivity::class.java))
        }

        mBinding.tvSettingCurVersion.text =
            "${StringUtils.getString(R.string.version)} ${AppPackageUtils.getVersionName(this)}"
    }


    private fun showUpdateDialog(appVersion: AppVersionEntity) {
        val updateAppDialog = UpdateAppDialog.Builder(appVersion).apply {
            positiveClickListener = { callBack ->
                // 授权权限成功
                mViewModel.downLoadApk(appVersion, object : OnDownloadProgressListener {

                    override fun onProgress(curSize: Long, totalSize: Long) {
                        callBack(curSize, totalSize, 0)
                    }

                    override fun onSuccess(file: File) {
                        Timber.i("文件下载完成：${file.path}")
                        callBack(0, 0, 1)
                        AppPackageUtils.installApk(this@SettingActivity, file)
                    }

                    override fun onFailure(e: Throwable) {
                        Timber.i("文件下载失败：${e.message}")
                        callBack(0, 0, -1)
                    }
                })
            }
        }.build()
        updateAppDialog.show(supportFragmentManager)
    }

    override fun initData() {
        mViewModel.checkVersion(this)
    }
}
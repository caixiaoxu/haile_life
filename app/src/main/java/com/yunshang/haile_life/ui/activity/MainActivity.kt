package com.yunshang.haile_life.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.SparseArray
import androidx.fragment.app.Fragment
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.lsy.framelib.utils.ActivityUtils
import com.lsy.framelib.utils.AppPackageUtils
import com.lsy.framelib.utils.SToast
import com.tencent.map.geolocation.TencentLocationManager
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.MainViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.AppVersionEntity
import com.yunshang.haile_life.data.model.OnDownloadProgressListener
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityMainBinding
import com.yunshang.haile_life.ui.activity.common.CustomCaptureActivity
import com.yunshang.haile_life.ui.activity.order.ScanOrderActivity
import com.yunshang.haile_life.ui.activity.shop.StarfishRefundListActivity
import com.yunshang.haile_life.ui.fragment.HomeFragment
import com.yunshang.haile_life.ui.fragment.MineFragment
import com.yunshang.haile_life.ui.fragment.OrderFragment
import com.yunshang.haile_life.ui.fragment.StoreFragment
import com.yunshang.haile_life.ui.view.dialog.UpdateAppDialog
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.string.StringUtils
import timber.log.Timber
import java.io.File
import java.util.*

class MainActivity :
    BaseBusinessActivity<ActivityMainBinding, MainViewModel>(MainViewModel::class.java, BR.vm) {

    // 当前的fragment
    private var curFragment: Fragment? = null

    private val fragments = SparseArray<Fragment>(2).apply {
        put(R.id.rb_main_tab_home, HomeFragment())
        put(R.id.rb_main_tab_store, StoreFragment())
        put(R.id.rb_main_tab_scan, HomeFragment())
        put(R.id.rb_main_tab_order, OrderFragment().apply {
            arguments = IntentParams.OrderListParams.pack(true)
        })
        put(R.id.rb_main_tab_mine, MineFragment())
    }

    // 扫码相机启动器
    private val scanCodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let {
            Timber.i("二维码：$it")
            val code = StringUtils.getPayCode(it) ?: if (StringUtils.isImeiCode(it)) it else null
            code?.let { code ->
                startActivity(Intent(this@MainActivity, ScanOrderActivity::class.java).apply {
                    putExtras(IntentParams.ScanOrderParams.pack(code))
                })
            } ?: run {
                StringUtils.refundCode(it)?.let {
                    startActivity(Intent(this@MainActivity, StarfishRefundListActivity::class.java).apply {
                        putExtras(IntentParams.ScanOrderParams.pack(it))
                    })
                } ?: SToast.showToast(this, R.string.pay_code_error)
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        TencentLocationManager.setUserAgreePrivacy(true)
        super.onCreate(savedInstanceState)
        changeDefaultPage(IntentParams.MainParams.parseDefaultPage(intent))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            changeDefaultPage(IntentParams.MainParams.parseDefaultPage(intent))
        }
    }

    private fun changeDefaultPage(parseDefaultPage: Int) {
        if (0 == parseDefaultPage){
            mViewModel.checkId.value = R.id.rb_main_tab_home
        }
    }

    override fun isFullScreen(): Boolean = true

    override fun layoutId(): Int = R.layout.activity_main

    override fun initEvent() {
        super.initEvent()
        mViewModel.checkId.observe(this) {
            if (it != R.id.rb_main_tab_scan) {
                showChildFragment(it)
            }
        }
    }

    override fun initView() {
    }

    override fun initData() {
        mBinding.ivMainScan.setOnClickListener {
            scanCodeLauncher.launch(scanOptions)
        }

        checkUpdate()
    }

    /**
     * 显示子布局
     */
    private fun showChildFragment(id: Int) {
        curFragment?.let {
            supportFragmentManager.beginTransaction().hide(it).commit()
        }

        fragments[id]?.let {
            if (it.isAdded) {
                supportFragmentManager.beginTransaction().show(it).commit()
            } else {
                supportFragmentManager.beginTransaction().add(R.id.fl_main_controller, it)
                    .commit()
            }
            curFragment = it
        }
    }

    private fun checkUpdate() {
        mViewModel.checkVersion(this) {
            if (it.forceUpdate) {
                // 强制更新
                showUpdateDialog(it)
                return@checkVersion
            } else if (it.needUpdate
                && !DateTimeUtils.isSameDay(Date(SPRepository.checkUpdateTime), Date())
            ) {
                // 非强制更新并有更新,每天一次
                showUpdateDialog(it)
                SPRepository.checkUpdateTime = System.currentTimeMillis()
            }
        }
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
                        AppPackageUtils.installApk(this@MainActivity, file)
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

    override fun onBackListener() {
        ActivityUtils.doubleBack(this)
    }
}
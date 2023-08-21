package com.yunshang.haile_life.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.king.camera.scan.CameraScan
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.lsy.framelib.utils.ActivityUtils
import com.lsy.framelib.utils.AppPackageUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.tencent.map.geolocation.TencentLocationManager
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.MainViewModel
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.AppVersionEntity
import com.yunshang.haile_life.data.model.OnDownloadProgressListener
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityMainBinding
import com.yunshang.haile_life.ui.activity.common.CustomCaptureActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity
import com.yunshang.haile_life.ui.activity.order.DrinkingScanOrderActivity
import com.yunshang.haile_life.ui.activity.order.OrderDetailActivity
import com.yunshang.haile_life.ui.activity.order.ScanOrderActivity
import com.yunshang.haile_life.ui.activity.shop.RechargeStarfishActivity
import com.yunshang.haile_life.ui.activity.shop.StarfishRefundListActivity
import com.yunshang.haile_life.ui.view.dialog.UpdateAppDialog
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.qrcode.WeChatQRCodeScanActivity
import com.yunshang.haile_life.utils.scheme.SchemeURLHelper
import com.yunshang.haile_life.utils.string.StringUtils
import org.opencv.OpenCV
import timber.log.Timber
import java.io.File
import java.util.*

class MainActivity :
    BaseBusinessActivity<ActivityMainBinding, MainViewModel>(MainViewModel::class.java, BR.vm) {

    // 权限
    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            if (result.values.any { it }) {
                // 授权权限成功
//                scanCodeLauncher.launch(scanOptions)
                startQRCodeScan.launch(
                    Intent(
                        this@MainActivity,
                        WeChatQRCodeScanActivity::class.java
                    )
                )
            } else {
                // 授权失败
                SToast.showToast(this, R.string.empty_permission)
            }
        }

    // 补偿界面界面
    private val startQRCodeScan =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                // 扫码结果
                CameraScan.parseScanResult(it.data)?.let { code ->
                    Timber.d("二维码：$code")
                    SToast.showToast(this, code)
                }
            }
        }

    // 扫码相机启动器
    private val scanCodeLauncher = registerForActivityResult(ScanContract()) { result ->
        result.contents?.let {
            Timber.i("二维码：$it")
            var code = StringUtils.getPayCode(it) ?: if (StringUtils.isImeiCode(it)) it else null
            StringUtils.getPayImeiCode(it)?.let { imei ->
                code = imei
            }

            code?.let { code ->
                mViewModel.requestScanResult(code) { scan, detail, appoint ->
                    if (detail.deviceErrorCode > 0) {
                        SToast.showToast(
                            this@MainActivity,
                            detail.deviceErrorMsg.ifEmpty { "设备故障,请稍后再试!" }
                        )
                        return@requestScanResult
                    } else if (2 == detail.soldState) {
                        SToast.showToast(
                            this@MainActivity,
                            detail.deviceErrorMsg.ifEmpty { "设备已停用,请稍后再试!" }
                        )
                        return@requestScanResult
                    } else if (0 == detail.amount) {
                        SToast.showToast(
                            this@MainActivity, "设备工作中,请稍后再试!"
                        )
                        return@requestScanResult
                    } else if (detail.shopClosed) {
                        SToast.showToast(
                            this@MainActivity, "门店不在营业时间内,请稍后再试!"
                        )
                        return@requestScanResult
                    }
                    if (!appoint?.orderNo.isNullOrEmpty()) {
                        // 预约详情界面
                        startActivity(
                            Intent(
                                this@MainActivity,
                                OrderDetailActivity::class.java
                            ).apply {
                                putExtras(IntentParams.ScanOrderParams.pack(code, scan))
                                putExtras(IntentParams.OrderParams.pack(appoint!!.orderNo, true, 1))
                            })
                    } else {
                        if (DeviceCategory.isDrinking(detail.categoryCode))
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    DrinkingScanOrderActivity::class.java
                                ).apply {
                                    putExtras(IntentParams.ScanOrderParams.pack(code, scan, detail))
                                })
                        else
                            startActivity(
                                Intent(
                                    this@MainActivity,
                                    ScanOrderActivity::class.java
                                ).apply {
                                    putExtras(IntentParams.ScanOrderParams.pack(code, scan, detail))
                                })
                    }
                }
            } ?: run {
                // 充值码
                val rechargeCode = StringUtils.rechargeCode(it)
                rechargeCode?.let {
                    try {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                RechargeStarfishActivity::class.java
                            ).apply {
                                putExtras(IntentParams.RechargeStarfishParams.pack(it.toInt()))
                            })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                // 退款码
                val refundCode = StringUtils.refundCode(it)
                refundCode?.let {
                    startActivity(
                        Intent(
                            this@MainActivity,
                            StarfishRefundListActivity::class.java
                        ).apply {
                            putExtras(IntentParams.ScanOrderParams.pack(it))
                        })
                }
                if (null == rechargeCode && null == refundCode) {
                    SToast.showToast(this, R.string.pay_code_error)
                }
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
        changeDefaultPage(IntentParams.DefaultPageParams.parseDefaultPage(intent))

        // 初始化OpenCV
        OpenCV.initAsync(this)
        // 初始化WeChatQRCodeDetector
        WeChatQRCodeDetector.init(this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            changeDefaultPage(IntentParams.DefaultPageParams.parseDefaultPage(intent))
        }
    }

    private fun changeDefaultPage(parseDefaultPage: Int) {
        if (0 == parseDefaultPage) {
            mViewModel.checkId.value = R.id.rb_main_tab_home
        }
    }

    override fun isFullScreen(): Boolean = true

    override fun layoutId(): Int = R.layout.activity_main

    override fun initEvent() {
        super.initEvent()
        mViewModel.checkId.observe(this) {
            if (it != R.id.rb_main_tab_scan && it != R.id.rb_main_tab_store) {
                showChildFragment(it)
            }
        }
    }

    override fun initView() {
        mBinding.ivMainScan.setOnClickListener {
            if (checkLogin()) {
                requestMultiplePermission.launch(
                    SystemPermissionHelper.cameraPermissions()
                        .plus(SystemPermissionHelper.readWritePermissions())
                )
            }
        }

        mBinding.rbMainTabStore.setOnRadioClickListener {
            if (checkLogin()) {
                mViewModel.storeAdEntity.value?.let { ad ->
                    ad.images.firstOrNull()?.let { image ->
                        SchemeURLHelper.parseSchemeURL(
                            this@MainActivity,
                            image.linkUrl,
                            image.linkType
                        )
                    }
                }
            }
            true
        }

        mBinding.rbMainTabOrder.setOnRadioClickListener {
            !checkLogin()
        }

        mBinding.rbMainTabMine.setOnRadioClickListener {
            !checkLogin()
        }
    }

    /**
     * 检测是否登录
     */
    private fun checkLogin(): Boolean =
        if (!SPRepository.isLogin()) {
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            false
        } else true


    override fun initData() {
        mViewModel.requestData()
        checkUpdate()
    }

    /**
     * 显示子布局
     */
    private fun showChildFragment(id: Int) {
        // 隐藏之前的界面
        mViewModel.curFragmentTag?.let {
            supportFragmentManager.findFragmentByTag(it)?.let { fragment ->
                supportFragmentManager.beginTransaction().hide(fragment).commit()
            }
        } ?: run {
            supportFragmentManager.fragments.forEach { fragment ->
                supportFragmentManager.beginTransaction().hide(fragment).commit()
            }
        }

        mViewModel.fragments[id]?.let {
            val fragmentName = it.javaClass.name
            supportFragmentManager.findFragmentByTag(fragmentName)?.let { fragment ->
                supportFragmentManager.beginTransaction().show(fragment).commit()
                if (it != fragment) {
                    mViewModel.fragments[id] = fragment
                }
            } ?: supportFragmentManager.beginTransaction()
                .add(R.id.fl_main_controller, it, fragmentName)
                .commit()
            mViewModel.curFragmentTag = fragmentName
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
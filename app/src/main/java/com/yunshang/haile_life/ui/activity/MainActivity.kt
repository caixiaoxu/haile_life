package com.yunshang.haile_life.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.github.penfeizhou.animation.apng.APNGDrawable
import com.github.penfeizhou.animation.loader.AssetStreamLoader
import com.king.camera.scan.CameraScan
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.ActivityUtils
import com.lsy.framelib.utils.AppPackageUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.tencent.map.geolocation.TencentLocationManager
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.MainViewModel
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.AppVersionEntity
import com.yunshang.haile_life.data.model.OnDownloadProgressListener
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityMainBinding
import com.yunshang.haile_life.ui.activity.common.WeChatQRCodeScanActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity
import com.yunshang.haile_life.ui.activity.order.AppointmentOrderActivity
import com.yunshang.haile_life.ui.activity.order.AppointmentOrderSelectorActivity
import com.yunshang.haile_life.ui.activity.order.DrinkingScanOrderActivity
import com.yunshang.haile_life.ui.activity.order.ScanOrderActivity
import com.yunshang.haile_life.ui.activity.shop.RechargeStarfishActivity
import com.yunshang.haile_life.ui.activity.shop.StarfishRefundListActivity
import com.yunshang.haile_life.ui.view.dialog.Hint3SecondDialog
import com.yunshang.haile_life.ui.view.dialog.UpdateAppDialog
import com.yunshang.haile_life.utils.DateTimeUtils
import com.yunshang.haile_life.utils.DialogUtils
import com.yunshang.haile_life.utils.string.StringUtils
import org.opencv.OpenCV
import timber.log.Timber
import java.io.File
import java.util.*

class MainActivity :
    BaseBusinessActivity<ActivityMainBinding, MainViewModel>(MainViewModel::class.java, BR.vm) {

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
        startQRCodeScan.launch(Intent(
            this@MainActivity,
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
                    dealQrCode(it)
                }
            }
        }

    private fun dealQrCode(originCode: String) {
        Timber.i("二维码：$originCode")
        val originCodeTrim = originCode.trim()
        var code =
            StringUtils.getPayCode(originCodeTrim)
                ?: if (StringUtils.isImeiCode(originCodeTrim)) originCodeTrim else null
        StringUtils.getPayImeiCode(originCodeTrim)?.let { imei ->
            code = imei
        }

        code?.let { code ->
            mViewModel.requestScanResult(code, supportFragmentManager) { scan, detail, appoint ->
                if (detail.deviceErrorCode > 0 || 3 == detail.deviceState) {
                    Hint3SecondDialog.Builder(detail.deviceErrorMsg.ifEmpty { "设备故障,请稍后再试!" })
                        .apply {
                            dialogBgResource = R.drawable.shape_dialog_bg
                        }.build().show(supportFragmentManager)
                    return@requestScanResult
                } else if (2 == detail.soldState) {
                    Hint3SecondDialog.Builder(detail.deviceErrorMsg.ifEmpty { "设备已停用,请稍后再试!" })
                        .apply {
                            dialogBgResource = R.drawable.shape_dialog_bg
                        }.build().show(supportFragmentManager)
                    return@requestScanResult
                } else if (detail.shopClosed) {
                    Hint3SecondDialog.Builder("门店不在营业时间内,请稍后再试!").apply {
                        dialogBgResource = R.drawable.shape_dialog_bg
                    }.build().show(supportFragmentManager)
                    return@requestScanResult
                }
                if (!appoint?.orderNo.isNullOrEmpty()) {
                    // 预约详情界面
                    startActivity(
                        Intent(
                            this,
                            AppointmentOrderActivity::class.java
                        ).apply {
                            putExtras(IntentParams.OrderParams.pack(appoint!!.orderNo))
                        }
                    )
                } else if (2 == detail.deviceState && 1 == detail.reserveState
                    && true == detail.enableReserve
                    && (DeviceCategory.isWashingOrShoes(detail.categoryCode) || DeviceCategory.isDryer(
                        detail.categoryCode
                    ))
                ) {
                    // 跳转预约下单，
                    startActivity(
                        Intent(
                            this@MainActivity,
                            AppointmentOrderSelectorActivity::class.java
                        ).apply {
                            putExtras(
                                IntentParams.DeviceParams.pack(
                                    detail.categoryCode,
                                    detail.id
                                )
                            )
                        })
                } else {
                    if (0 == detail.amount) {
                        Hint3SecondDialog.Builder("设备工作中,请稍后再试!")
                            .build().show(supportFragmentManager)
                        return@requestScanResult
                    }

                    if (DeviceCategory.isDrinkingOrShower(detail.categoryCode))
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
            val rechargeCode = StringUtils.rechargeCode(originCodeTrim)
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
            val refundCode = StringUtils.refundCode(originCodeTrim)
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
                Hint3SecondDialog.Builder(com.lsy.framelib.utils.StringUtils.getString(R.string.pay_code_error))
                    .build().show(supportFragmentManager)
            }
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
            if (it != R.id.rb_main_tab_scan) {
                showChildFragment(it)
            }
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
        // 从asset file中加载
        val assetLoader = AssetStreamLoader(this, "icon_main_scan_anim.png")
        // 创建APNG Drawable
        val apngDrawable = APNGDrawable(assetLoader)
        // 自动播放
        mBinding.ivMainScan.setImageDrawable(apngDrawable)
        mBinding.ivMainScan.setOnClickListener {
            if (checkLogin()) {
                DialogUtils.checkPermissionDialog(
                    this,
                    supportFragmentManager,
                    permissions,
                    "需要相机权限和媒体读取权限来扫描或读取设备码"
                ) {
                    requestMultiplePermission.launch(permissions)
                }
            }
        }

//        mBinding.rbMainTabStore.setOnRadioClickListener {
//            if (checkLogin()) {
//                mViewModel.storeAdEntity.value?.let { ad ->
//                    ad.images.firstOrNull()?.let { image ->
//                        SchemeURLHelper.parseSchemeURL(
//                            this@MainActivity,
//                            image.linkUrl,
//                            image.linkType
//                        )
//                    }
//                }
//            }
//            true
//        }

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
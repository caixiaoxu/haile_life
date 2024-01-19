package com.yunshang.haile_life.ui.activity.device

import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.king.camera.scan.CameraScan
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.ScreenUtils
import com.lsy.framelib.utils.SystemPermissionHelper
import com.youth.banner.indicator.CircleIndicator
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.DeviceNavigationViewModel
import com.yunshang.haile_life.data.Constants
import com.yunshang.haile_life.data.agruments.DeviceCategory
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityDeviceNavigationBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity
import com.yunshang.haile_life.ui.activity.personal.DiscountCouponActivity
import com.yunshang.haile_life.ui.activity.shop.RechargeStarfishActivity
import com.yunshang.haile_life.ui.activity.shop.StarfishRefundListActivity
import com.yunshang.haile_life.ui.view.adapter.ImageAdapter
import com.yunshang.haile_life.ui.activity.common.WeChatQRCodeScanActivity
import com.yunshang.haile_life.ui.activity.order.*
import com.yunshang.haile_life.utils.DialogUtils
import com.yunshang.haile_life.utils.scheme.SchemeURLHelper
import com.yunshang.haile_life.utils.string.StringUtils
import com.yunshang.haile_life.web.WebViewActivity
import timber.log.Timber

class DeviceNavigationActivity :
    BaseBusinessActivity<ActivityDeviceNavigationBinding, DeviceNavigationViewModel>(
        DeviceNavigationViewModel::class.java, BR.vm
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
                    val originCodeTrim = it.trim()
                    val code =
                        StringUtils.getPayCode(originCodeTrim) ?: if (StringUtils.isImeiCode(
                                originCodeTrim
                            )
                        ) it else null
                    code?.let {
                        mViewModel.requestScanResult(code) { scan, detail, appoint ->
                            if (detail.deviceErrorCode > 0) {
                                SToast.showToast(
                                    this@DeviceNavigationActivity,
                                    detail.deviceErrorMsg.ifEmpty { "设备故障,请稍后再试!" }
                                )
                                return@requestScanResult
                            } else if (2 == detail.soldState) {
                                SToast.showToast(
                                    this@DeviceNavigationActivity,
                                    detail.deviceErrorMsg.ifEmpty { "设备已停用,请稍后再试!" }
                                )
                                return@requestScanResult
                            } else if (0 == detail.amount) {
                                SToast.showToast(
                                    this@DeviceNavigationActivity, "设备工作中,请稍后再试!"
                                )
                                return@requestScanResult
                            } else if (detail.shopClosed) {
                                SToast.showToast(
                                    this@DeviceNavigationActivity, "门店不在营业时间内,请稍后再试!"
                                )
                                return@requestScanResult
                            }
                            if (!appoint?.orderNo.isNullOrEmpty()) {
                                // 预约详情界面
                                startActivity(
                                    Intent(
                                        this@DeviceNavigationActivity,
                                        OrderDetailActivity::class.java
                                    ).apply {
                                        putExtras(IntentParams.ScanOrderParams.pack(code, scan))
                                        putExtras(
                                            IntentParams.OrderParams.pack(
                                                appoint!!.orderNo,
                                                true,
                                                1
                                            )
                                        )
                                    })
                            } else {
                                startActivity(
                                    if (DeviceCategory.isDrinkingOrShower(detail.categoryCode))
                                        Intent(
                                            this@DeviceNavigationActivity,
                                            DrinkingScanOrderActivity::class.java
                                        ).apply {
                                            putExtras(
                                                IntentParams.ScanOrderParams.pack(
                                                    code, scan, detail
                                                )
                                            )
                                        }
                                    else
                                        Intent(
                                            this@DeviceNavigationActivity,
                                            OrderSelectorActivity::class.java
                                        ).apply {
                                            putExtras(
                                                IntentParams.OrderSelectorParams.pack(
                                                    detail.id,
                                                    scan.activityHashKey
                                                )
                                            )
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
                                        this@DeviceNavigationActivity,
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
                                    this@DeviceNavigationActivity,
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
        }

    override fun layoutId(): Int = R.layout.activity_device_navigation

    override fun backBtn(): View = mBinding.barDeviceNavigationTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.categoryCode = IntentParams.DeviceParams.parseCategoryCode(intent)
    }

    override fun initEvent() {
        super.initEvent()

        // banner
        mViewModel.adEntity.observe(this) { ad ->
            if (ad.images.isEmpty()) {
                mBinding.bannerDeviceNavigationBanner.visibility = View.GONE
            } else {
                mBinding.bannerDeviceNavigationBanner.addBannerLifecycleObserver(this)
                    .setIndicator(CircleIndicator(this))
                    .setAdapter(ImageAdapter(
                        ad.images.sortedBy { item -> item.sortValue },
                        { it.imageUrl }) { data, _ ->
                        SchemeURLHelper.parseSchemeURL(
                            this,
                            data.linkUrl,
                            data.linkType
                        )
                    })
                mBinding.bannerDeviceNavigationBanner.visibility = View.VISIBLE
            }
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        val itemWH = (ScreenUtils.screenWidth - 2 * DimensionUtils.dip2px(this, 12f)) / 4
        mBinding.itemWH = itemWH

        mBinding.btnDeviceNavigationScan.setOnClickListener {
            if (checkLogin())
                DialogUtils.checkPermissionDialog(
                    this@DeviceNavigationActivity,
                    supportFragmentManager,
                    permissions,
                    "需要相机权限和媒体读取权限来扫描或读取设备码"
                ) {
                    requestMultiplePermission.launch(permissions)
                }
        }
        mBinding.btnDeviceNavigationCardManager.setOnClickListener {
            if (checkLogin()) {
                startActivity(
                    Intent(
                        this@DeviceNavigationActivity,
                        CardManagerActivity::class.java
                    )
                )
            }
        }

        mBinding.btnDeviceNavigationControlCode.setOnClickListener {
            if (checkLogin())
                startActivity(
                    Intent(this@DeviceNavigationActivity, WaterControlCodeActivity::class.java)
                )
        }
//        mBinding.btnDeviceNavigationAppoint.setOnClickListener {
//            SToast.showToast(this@DeviceNavigationActivity, R.string.coming_soon)
//        }

        mBinding.btnDeviceNavigationOrder.setOnClickListener {
            if (checkLogin())
                startActivity(
                    Intent(this@DeviceNavigationActivity, OrderListActivity::class.java)
                )
        }

        mBinding.btnDeviceNavigationCoupon.setOnClickListener {
            if (checkLogin())
                startActivity(
                    Intent(
                        this@DeviceNavigationActivity,
                        DiscountCouponActivity::class.java
                    ).apply {
                        putExtras(intent)
                    })
        }

        mBinding.btnDeviceNavigationFaq.setOnClickListener {
            startActivity(
                Intent(
                    this@DeviceNavigationActivity,
                    WebViewActivity::class.java
                ).apply { putExtras(IntentParams.WebViewParams.pack(Constants.guide)) })
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

    /**
     * 检测是否登录
     */
    private fun checkLogin(): Boolean =
        if (!SPRepository.isLogin()) {
            startActivity(Intent(this@DeviceNavigationActivity, LoginActivity::class.java))
            false
        } else true

    override fun initData() {
        mViewModel.requestData()
    }

    //方法一：自己控制banner的生命周期
    override fun onStart() {
        super.onStart()
        //开始轮播
        mBinding.bannerDeviceNavigationBanner.start()
    }

    override fun onStop() {
        super.onStop()
        //停止轮播
        mBinding.bannerDeviceNavigationBanner.stop()
    }

    override fun onDestroy() {
        //销毁
        mBinding.bannerDeviceNavigationBanner.destroy()
        super.onDestroy()
    }
}
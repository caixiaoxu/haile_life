package com.yunshang.haile_life.ui.activity.personal

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.king.camera.scan.CameraScan
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.utils.*
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.FaultRepairsViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.FaultCategoryEntity
import com.yunshang.haile_life.databinding.ActivityFaultRepairsBinding
import com.yunshang.haile_life.databinding.ItemFaultRepairsBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.common.PicBrowseActivity
import com.yunshang.haile_life.ui.activity.common.WeChatQRCodeScanActivity
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.loadImage
import com.yunshang.haile_life.ui.view.dialog.CommonBottomSheetDialog
import com.yunshang.haile_life.utils.DialogUtils
import com.yunshang.haile_life.utils.string.StringUtils
import timber.log.Timber

class FaultRepairsActivity :
    BaseBusinessActivity<ActivityFaultRepairsBinding, FaultRepairsViewModel>(
        FaultRepairsViewModel::class.java, BR.vm
    ) {

    private val filePermissions = SystemPermissionHelper.readWritePermissions()

    private val permissions = SystemPermissionHelper.cameraPermissions()
        .plus(filePermissions)

    // 文件读取权限
    private val requestFilePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            if (result.values.any { it }) {
                // 授权权限成功
                DialogUtils.showImgSelectorDialog(
                    this@FaultRepairsActivity,
                    6 - (mViewModel.faultPics.value?.size ?: 0),
                    needCrop = false
                ) { isSuccess, picList ->
                    if (isSuccess && !picList.isNullOrEmpty()) {
                        mViewModel.uploadHeadIcon(picList.mapNotNull { item ->
                            item.cutPath ?: item.realPath
                        }.toList())
                    }
                }
            } else {
                // 授权失败
                SToast.showToast(this, R.string.empty_permission)
            }
        }

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
                this@FaultRepairsActivity,
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
        code?.let {
            mViewModel.requestCategory(it)
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

    override fun layoutId(): Int = R.layout.activity_fault_repairs

    override fun backBtn(): View = mBinding.barFaultRepairsTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()

        mViewModel.faultPics.observe(this) {
            mBinding.clFaultRepairsPic.removeViews(1, mBinding.clFaultRepairsPic.childCount - 1)
            val picItemWH = (ScreenUtils.screenWidth - DimensionUtils.dip2px(
                this@FaultRepairsActivity,
                16f
            ) * 2 - DimensionUtils.dip2px(this@FaultRepairsActivity, 8f) * 3) / 4
            val inflater = LayoutInflater.from(this@FaultRepairsActivity)
            it.forEachIndexed { index, url ->
                val faultBinding = DataBindingUtil.inflate<ItemFaultRepairsBinding>(
                    inflater,
                    R.layout.item_fault_repairs,
                    null,
                    false
                )
                faultBinding.ivFaultRepairsPic.loadImage(imgHeadUrl = url)
                faultBinding.ivFaultRepairsPicDel.setOnClickListener {
                    mViewModel.faultPics.value?.let { picList ->
                        picList.removeAt(index)
                        mViewModel.faultPics.value = picList
                    }
                }
                faultBinding.root.setOnClickListener {
                    startActivity(
                        Intent(
                            this@FaultRepairsActivity,
                            PicBrowseActivity::class.java
                        ).apply {
                            putExtras(IntentParams.PicParams.pack(url))
                        })
                }
                faultBinding.root.id = index + 1
                mBinding.clFaultRepairsPic.addView(faultBinding.root, picItemWH, picItemWH)
            }

            if (it.size < 6) {
                val faultBinding = DataBindingUtil.inflate<ItemFaultRepairsBinding>(
                    inflater,
                    R.layout.item_fault_repairs,
                    null,
                    false
                )
                faultBinding.isAdd = true
                faultBinding.ivFaultRepairsPic.loadImage(R.mipmap.icon_fault_add_pic)
                faultBinding.root.setOnClickListener {
                    DialogUtils.checkPermissionDialog(
                        this,
                        supportFragmentManager,
                        permissions,
                        "需要媒体读取权限来读取文件"
                    ) {
                        requestFilePermission.launch(filePermissions)
                    }
                }
                faultBinding.root.id = it.size + 1
                mBinding.clFaultRepairsPic.addView(faultBinding.root, picItemWH, picItemWH)
            }
            mBinding.flowFaultRepairsDescPic.referencedIds =
                IntArray(it.size + 1) { item -> item + 1 }
        }

        mViewModel.jump.observe(this) {
            //报修列表界面
            startActivity(Intent(this@FaultRepairsActivity, FaultRepairsRecordActivity::class.java))
            finish()
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.barFaultRepairsTitle.getRightBtn().apply {
            setText(R.string.repairs_record)
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@FaultRepairsActivity, R.color.color_black_85))
            setOnClickListener {
                startActivity(
                    Intent(
                        this@FaultRepairsActivity,
                        FaultRepairsRecordActivity::class.java
                    )
                )
            }
        }

        mBinding.llFaultRepairsImei.setOnClickListener {
            if (!ViewUtils.isFastDoubleClick()) {
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
        mBinding.llFaultRepairsFaultType.setOnClickListener {
            if (mViewModel.faultCategorys.value.isNullOrEmpty()) {
                SToast.showToast(this@FaultRepairsActivity, "请先扫描设备码")
                return@setOnClickListener
            }
            CommonBottomSheetDialog.Builder(
                com.lsy.framelib.utils.StringUtils.getString(R.string.fault_type),
                mViewModel.faultCategorys.value!!
            ).apply {
                onValueSureListener = object :
                    CommonBottomSheetDialog.OnValueSureListener<FaultCategoryEntity> {
                    override fun onValue(data: FaultCategoryEntity?) {
                        mViewModel.faultType.value = data
                    }
                }
            }.build().show(supportFragmentManager)
        }

        mBinding.btnFaultRepairsSubmit.setOnClickListener {
            mViewModel.submitFaultRepairs(this@FaultRepairsActivity)
        }
    }

    override fun initData() {
    }
}
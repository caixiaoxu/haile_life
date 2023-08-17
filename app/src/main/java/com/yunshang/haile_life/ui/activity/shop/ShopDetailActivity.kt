package com.yunshang.haile_life.ui.activity.shop

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.ShopDetailViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.StoreDeviceEntity
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityShopDetailBinding
import com.yunshang.haile_life.databinding.ItemHomeNearStoresBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity
import com.yunshang.haile_life.ui.activity.order.AppointmentSubmitActivity
import com.yunshang.haile_life.ui.view.dialog.ShopNoticeDialog

class ShopDetailActivity : BaseBusinessActivity<ActivityShopDetailBinding, ShopDetailViewModel>(
    ShopDetailViewModel::class.java, BR.vm
) {

    // 拨打电话权限
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                // 授权权限成功
                mViewModel.shopDetail.value?.serviceTelephone?.let {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$it")))
                }
            } else {
                // 授权失败
                SToast.showToast(this, R.string.empty_permission)
            }
        }

    override fun layoutId(): Int = R.layout.activity_shop_detail

    override fun backBtn(): View = mBinding.barShopDetailTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()

        mViewModel.shopNotice.observe(this) {
            if (!it.isNullOrEmpty()) {
                ShopNoticeDialog(it).show(supportFragmentManager)
            }
        }
    }

    override fun initIntent() {
        super.initIntent()
        mViewModel.shopId = IntentParams.IdParams.parseId(intent)

        mViewModel.shopDetail.observe(this) {
            it?.let { detail ->
                mBinding.llShopDetailDevices.buildChild<ItemHomeNearStoresBinding, StoreDeviceEntity>(
                    detail.shopDeviceDetailList,
                    LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        DimensionUtils.dip2px(this@ShopDetailActivity, 34f)
                    ), 1
                ) { _, childBinding, data ->
                    childBinding.name = data.categoryName
                    childBinding.num = "${data.total}"
                    childBinding.status = "${data.idleCount}"
                    childBinding.nameIcon = data.shopIcon()
                }

                mBinding.tvShopDetailRecharge.visibility =
                    if (detail.rechargeFlag) View.VISIBLE else View.GONE
            }
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.tvShopDetailContactPhone.setOnClickListener {
            requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
        }
        mBinding.tvShopDetailRecharge.setOnClickListener {
            if (!checkLogin()) {
                return@setOnClickListener
            }
            startActivity(
                Intent(
                    this@ShopDetailActivity,
                    RechargeStarfishActivity::class.java
                ).apply {
                    putExtras(IntentParams.RechargeStarfishParams.pack(mViewModel.shopId))
                })
        }
        mBinding.btnShopDetailAppoint.setOnClickListener {
            if (!checkLogin()) {
                return@setOnClickListener
            }
            mViewModel.shopDetail.value?.let {
                startActivity(Intent(this, AppointmentSubmitActivity::class.java).apply {
                    putExtras(
                        IntentParams.ShopParams.pack(it.id)
                    )
                })
            }
        }
    }

    /**
     * 检测是否登录
     */
    private fun checkLogin(): Boolean =
        if (!SPRepository.isLogin()) {
            startActivity(Intent(this@ShopDetailActivity, LoginActivity::class.java))
            false
        } else true


    override fun initData() {
        mViewModel.requestData()
    }
}
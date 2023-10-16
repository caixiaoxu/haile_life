package com.yunshang.haile_life.ui.activity.shop

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.ShopPositionDetailViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.agruments.SearchSelectParam
import com.yunshang.haile_life.data.entities.StoreDeviceEntity
import com.yunshang.haile_life.data.entities.TimeMarketVO
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityShopPositionDetailBinding
import com.yunshang.haile_life.databinding.ItemHomeNearStoresBinding
import com.yunshang.haile_life.databinding.ItemShopPositionDetailTagsBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity
import com.yunshang.haile_life.ui.activity.order.AppointmentSubmitActivity
import com.yunshang.haile_life.ui.view.dialog.CommonBottomSheetDialog
import com.yunshang.haile_life.ui.view.dialog.ShopNoticeDialog
import com.yunshang.haile_life.utils.MapManagerUtils


class ShopPositionDetailActivity :
    BaseBusinessActivity<ActivityShopPositionDetailBinding, ShopPositionDetailViewModel>(
        ShopPositionDetailViewModel::class.java, BR.vm
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

    override fun layoutId(): Int = R.layout.activity_shop_position_detail

    override fun backBtn(): View = mBinding.barShopDetailTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()
        mSharedViewModel.mSharedLocation.observe(this) {
            mViewModel.location = it
            mViewModel.requestData()
        }

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
                val mS = DimensionUtils.dip2px(this@ShopPositionDetailActivity, 4f)
                mBinding.llShopPositionDetailTags.buildChild<ItemShopPositionDetailTagsBinding, TimeMarketVO>(
                    it.timeMarketVOList,
                    start = 1
                ) { _, childBinding, data ->
                    childBinding.tvShopPositionDetailTag.text =
                        StringUtils.getString(R.string.limited_time_offer_prompt, data.discount)
                    (childBinding.tvShopPositionDetailTag.layoutParams as? LinearLayoutCompat.LayoutParams)?.marginStart =
                        mS
                }

                mBinding.llShopDetailDevices.buildChild<ItemHomeNearStoresBinding, StoreDeviceEntity>(
                    detail.positionDeviceDetailList,
                    LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        DimensionUtils.dip2px(this@ShopPositionDetailActivity, 34f)
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

        val list = listOf(
            SearchSelectParam(
                0,
                "腾讯地图${if (MapManagerUtils.checkTencentMapEInstall(this)) "" else "（安装）"}"
            ),
            SearchSelectParam(
                1,
                "高德地图${if (MapManagerUtils.checkGeoMapEInstall(this)) "" else "（安装）"}"
            ),
            SearchSelectParam(
                2,
                "百度地图${if (MapManagerUtils.checkBaiduMapEInstall(this)) "" else "（安装）"}"
            ),
        )

        //导航
        mBinding.tvShopPositionDetailNavigation.setOnClickListener {
            mViewModel.shopDetail.value?.let { detail ->
                if (null != detail.lat && null != detail.lng) {
                    CommonBottomSheetDialog.Builder("", list).apply {
                        selectModel = 1
                        onValueSureListener = object :
                            CommonBottomSheetDialog.OnValueSureListener<SearchSelectParam> {
                            override fun onValue(data: SearchSelectParam?) {
                                when (data?.id) {
                                    0 -> MapManagerUtils.goToTencentMap(
                                        this@ShopPositionDetailActivity,
                                        detail.lat,
                                        detail.lng,
                                        detail.name
                                    )
                                    1 -> MapManagerUtils.goToGeoMap(
                                        this@ShopPositionDetailActivity,
                                        detail.lat,
                                        detail.lng,
                                        detail.name
                                    )
                                    2 -> MapManagerUtils.goToBaiduMap(
                                        this@ShopPositionDetailActivity,
                                        detail.lat,
                                        detail.lng,
                                        detail.name
                                    )
                                }
                            }
                        }
                    }.build().show(supportFragmentManager)
                }
            }
        }

        // 营业时间
        mBinding.tvShopDetailWorkTime.setOnClickListener {
            mViewModel.shopDetail.value?.let { detail ->
                startActivity(
                    Intent(
                        this@ShopPositionDetailActivity,
                        ShopBusinessWorkTimeActivity::class.java
                    ).apply {
                        putExtras(IntentParams.ShopWorkTimeParams.pack(detail.workTimeArr()))
                    })
            }
        }

        mBinding.tvShopDetailContactPhone.setOnClickListener {
            requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
        }
        mBinding.tvShopDetailRecharge.setOnClickListener {
            if (!checkLogin()) {
                return@setOnClickListener
            }
            startActivity(
                Intent(
                    this@ShopPositionDetailActivity,
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
            startActivity(Intent(this@ShopPositionDetailActivity, LoginActivity::class.java))
            false
        } else true


    override fun initData() {
    }
}
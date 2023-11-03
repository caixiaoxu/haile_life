package com.yunshang.haile_life.ui.activity.shop

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.ShopPositionDetailViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.agruments.SearchSelectParam
import com.yunshang.haile_life.data.entities.ShopPositionDeviceEntity
import com.yunshang.haile_life.data.entities.TimeMarketVO
import com.yunshang.haile_life.data.extend.hasVal
import com.yunshang.haile_life.data.model.SPRepository
import com.yunshang.haile_life.databinding.ActivityShopPositionDetailBinding
import com.yunshang.haile_life.databinding.ItemShopPositionDetailDeviceBinding
import com.yunshang.haile_life.databinding.ItemShopPositionDetailFloorBinding
import com.yunshang.haile_life.databinding.ItemShopPositionDetailTagsBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.login.LoginActivity
import com.yunshang.haile_life.ui.activity.order.AppointmentSubmitActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.dialog.CommonBottomSheetDialog
import com.yunshang.haile_life.ui.view.dialog.ShopNoticeDialog
import com.yunshang.haile_life.ui.view.refresh.CommonLoadMoreRecyclerView
import com.yunshang.haile_life.utils.MapManagerUtils
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView


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

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemShopPositionDetailDeviceBinding, ShopPositionDeviceEntity>(
            R.layout.item_shop_position_detail_device, BR.item
        ) { mItemBinding, _, item ->

            mItemBinding?.root?.setOnClickListener {
                if (!checkLogin()) {
                    return@setOnClickListener
                }
                mViewModel.shopDetail.value?.shopId?.let {
                    startActivity(Intent(this, AppointmentSubmitActivity::class.java).apply {
                        putExtras(
                            IntentParams.DeviceParams.pack(deviceId = item.id)
                        )
                    })
                }
            }
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

        mViewModel.curDeviceCategory.observe(this) {
            if (mBinding.rgShopPositionDetailFloor.childCount > 0) {
                val index =
                    mViewModel.shopDetail.value?.floorList?.indexOfFirst { item -> item.value == it.selectFloor?.value || (item.value.isEmpty() && it.selectFloor?.value.isNullOrEmpty()) }
                if (index.hasVal()) {
                    mBinding.rgShopPositionDetailFloor.check(mBinding.rgShopPositionDetailFloor[index!!].id)
                }
            }
            mBinding.rvShopPositionDetailDevices.requestLoadMore(true)
        }
    }

    override fun initIntent() {
        super.initIntent()
        mViewModel.positionId = IntentParams.IdParams.parseId(intent)

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

                mBinding.tvShopDetailRecharge.visibility =
                    if (detail.rechargeFlag) View.VISIBLE else View.GONE

                if (!detail.positionDeviceDetailList.isNullOrEmpty()) {
                    mBinding.indicatorShopPositionDetailDeviceCategory.navigator =
                        CommonNavigator(this@ShopPositionDetailActivity).apply {
                            adapter = object : CommonNavigatorAdapter() {
                                override fun getCount(): Int = detail.positionDeviceDetailList.size

                                override fun getTitleView(
                                    context: Context?,
                                    index: Int
                                ): IPagerTitleView {
                                    return SimplePagerTitleView(context).apply {
                                        normalColor = ContextCompat.getColor(
                                            this@ShopPositionDetailActivity,
                                            R.color.color_black_65
                                        )
                                        selectedColor = ContextCompat.getColor(
                                            this@ShopPositionDetailActivity,
                                            R.color.color_black_85
                                        )
                                        detail.positionDeviceDetailList[index].let { category ->
                                            text = category.categoryName
                                            setOnClickListener {
                                                mViewModel.curDeviceCategory.value = category
                                                onPageSelected(index)
                                                notifyDataSetChanged()
                                            }
                                        }
                                    }
                                }

                                override fun getIndicator(context: Context?): IPagerIndicator {
                                    return LinePagerIndicator(context).apply {
                                        mode = LinePagerIndicator.MODE_WRAP_CONTENT
                                        setColors(
                                            ContextCompat.getColor(
                                                this@ShopPositionDetailActivity,
                                                R.color.colorPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                }

                mBinding.rgShopPositionDetailFloor.removeAllViews()
                val inflater = LayoutInflater.from(this@ShopPositionDetailActivity)
                detail.floorList?.forEachIndexed { index, floor ->
                    val itemFloorBinding =
                        DataBindingUtil.inflate<ItemShopPositionDetailFloorBinding>(
                            inflater,
                            R.layout.item_shop_position_detail_floor,
                            null,
                            false
                        )
                    itemFloorBinding.root.id = index + 1
                    itemFloorBinding.rbShopPositionDetailFloor.setOnCheckedChangeListener { _, b ->
                        if (b) {
                            mViewModel.curDeviceCategory.value?.let { device ->
                                device.selectFloor = floor
                                mBinding.rvShopPositionDetailDevices.requestLoadMore(true)
                            }
                        }
                    }
                    itemFloorBinding.item = floor
                    mBinding.rgShopPositionDetailFloor.addView(
                        itemFloorBinding.root, ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    )
                }
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
            mViewModel.shopDetail.value?.shopId?.let {
                startActivity(
                    Intent(
                        this@ShopPositionDetailActivity,
                        RechargeStarfishActivity::class.java
                    ).apply {
                        putExtras(IntentParams.RechargeStarfishParams.pack(it))
                    })
            }
        }

        mBinding.rvShopPositionDetailDevices.layoutManager = LinearLayoutManager(this)
        mBinding.rvShopPositionDetailDevices.adapter = mAdapter
        mBinding.rvShopPositionDetailDevices.requestData = object :
            CommonLoadMoreRecyclerView.OnRequestDataListener<ShopPositionDeviceEntity>() {
            override fun requestData(
                page: Int,
                pageSize: Int,
                callBack: (responseList: MutableList<out ShopPositionDeviceEntity>?) -> Unit
            ) {
                mViewModel.requestDeviceList(page, pageSize, callBack)
            }

            override fun onLoadMore(
                isRefresh: Boolean,
                responseList: MutableList<out ShopPositionDeviceEntity>
            ): Boolean {
                if (isRefresh && responseList.isEmpty()) {
                    val behavior =
                        (mBinding.appbarShopPositionDetail.layoutParams as CoordinatorLayout.LayoutParams).behavior
                    if (behavior is AppBarLayout.Behavior) {
                        behavior.topAndBottomOffset = 0 //快熟滑动到顶部
                    }
                }
                return false
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
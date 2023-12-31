package com.yunshang.haile_life.ui.activity.shop

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.NearByShopViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.NearStorePositionEntity
import com.yunshang.haile_life.databinding.ActivityNearByShopBinding
import com.yunshang.haile_life.databinding.ItemNearByShopBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.dialog.Hint3SecondDialog
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView
import com.yunshang.haile_life.utils.DialogUtils
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView

class NearByShopActivity : BaseBusinessActivity<ActivityNearByShopBinding, NearByShopViewModel>(
    NearByShopViewModel::class.java, BR.vm
) {
    private val permissions = SystemPermissionHelper.locationPermissions()

    private var emptyShopDialog: Hint3SecondDialog? = null

    private var curLocationRequest = false

    // 权限
    private val requestMultiplePermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            var isAllow = true
            for (permission in permissions) {
                if (true != result[permission]) {
                    isAllow = false
                    break
                }
            }
            if (isAllow) {
                curLocationRequest = true
                mSharedViewModel.requestLocationInfo(this)
            }
        }

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemNearByShopBinding, NearStorePositionEntity>(
            R.layout.item_near_by_shop, BR.item
        ) { mItemBinding, _, item ->
            mItemBinding?.root?.setOnClickListener {
                item.id?.let { positionId ->
                    startActivity(Intent(
                        this@NearByShopActivity,
                        ShopPositionDetailActivity::class.java
                    ).apply {
                        putExtras(IntentParams.IdParams.pack(positionId))
                    })
                }
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_near_by_shop

    override fun backBtn(): View = mBinding.barNearByShopTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.isRechargeShop = IntentParams.NearByShopParams.parseIsRechargeShop(intent)
    }

    override fun initEvent() {
        super.initEvent()
        mSharedViewModel.mSharedLocation.observe(this) {
            if (curLocationRequest) {
                mViewModel.location = it
                mBinding.includeIndicatorList.rvIndicatorListList.requestRefresh()
            }
        }
        mViewModel.curCategoryCode.observe(this) {
            mBinding.includeIndicatorList.rvIndicatorListList.requestRefresh()
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE
        mBinding.barNearByShopTitle.setTitle(if (mViewModel.isRechargeShop) R.string.recharge_shop_title else R.string.nearby_stores)

        if (mViewModel.isRechargeShop) {
            mBinding.includeIndicatorList.indicatorIndicatorListStatus.visibility = View.GONE
        } else {
            mBinding.includeIndicatorList.indicatorIndicatorListStatus.navigator =
                CommonNavigator(this).apply {
                    adapter = object : CommonNavigatorAdapter() {
                        override fun getCount(): Int = mViewModel.mNearByShopIndicators.size

                        override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
                            return SimplePagerTitleView(context).apply {
                                normalColor =
                                    ContextCompat.getColor(
                                        this@NearByShopActivity,
                                        R.color.color_black_65
                                    )
                                selectedColor =
                                    ContextCompat.getColor(
                                        this@NearByShopActivity,
                                        R.color.color_black_85
                                    )
                                mViewModel.mNearByShopIndicators[index].run {
                                    text = title
                                    setOnClickListener {
                                        mViewModel.curCategoryCode.value = value
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
                                        this@NearByShopActivity,
                                        R.color.colorPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            // 初始化选中状态
            when (IntentParams.DefaultPageParams.parseDefaultPage(intent)) {
                1 -> mBinding.includeIndicatorList.indicatorIndicatorListStatus.navigator.onPageSelected(
                    1
                )

                4 -> mBinding.includeIndicatorList.indicatorIndicatorListStatus.navigator.onPageSelected(
                    4
                )
            }
            mBinding.includeIndicatorList.indicatorIndicatorListStatus.visibility = View.VISIBLE
        }

        mBinding.includeIndicatorList.rvIndicatorListList.layoutManager = LinearLayoutManager(this)
        mBinding.includeIndicatorList.rvIndicatorListList.enableEmptyStatus = true
        ResourcesCompat.getDrawable(resources, R.drawable.divide_size8, null)?.let {
            mBinding.includeIndicatorList.rvIndicatorListList.addItemDecoration(
                DividerItemDecoration(
                    this@NearByShopActivity,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.includeIndicatorList.rvIndicatorListList.adapter = mAdapter
        mBinding.includeIndicatorList.rvIndicatorListList.requestData =
            object : CommonRefreshRecyclerView.OnRequestDataListener<NearStorePositionEntity>() {
                override fun requestData(
                    isRefresh: Boolean,
                    page: Int,
                    pageSize: Int,
                    callBack: (responseList: ResponseList<out NearStorePositionEntity>?) -> Unit
                ) {
                    mViewModel.requestNearByStores(page, pageSize, callBack)
                }

                override fun onRefresh(responseList: ResponseList<out NearStorePositionEntity>): Boolean {
                    if (0 == responseList.total) {
                        emptyShopDialog?.dismiss()
                        emptyShopDialog =
                            Hint3SecondDialog.Builder("附近2公里内暂无营业点").apply {
                                dialogBgResource = R.drawable.shape_dialog_bg
                            }.build().apply {
                                show(supportFragmentManager)
                            }
                    }
                    return super.onRefresh(responseList)
                }
            }
    }

    override fun initData() {
        if (!mViewModel.isRechargeShop) {
            when (IntentParams.DefaultPageParams.parseDefaultPage(intent)) {
                1 -> mViewModel.curCategoryCode.value = mViewModel.mNearByShopIndicators[1].value
                4 -> mViewModel.curCategoryCode.value = mViewModel.mNearByShopIndicators[4].value
                else -> mViewModel.curCategoryCode.value = mViewModel.mNearByShopIndicators[0].value
            }
        } else {
            mViewModel.curCategoryCode.value = mViewModel.mNearByShopIndicators[0].value
        }

        DialogUtils.checkPermissionDialog(
            this,
            supportFragmentManager,
            permissions,
            "需要定位权限来寻找附近营业点"
        ) {
            requestMultiplePermission.launch(permissions)
        }
    }
}
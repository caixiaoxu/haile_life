package com.yunshang.haile_life.ui.fragment

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver.OnScrollChangedListener
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StatusBarUtils
import com.youth.banner.indicator.CircleIndicator
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.HomeCategory
import com.yunshang.haile_life.business.vm.HomeViewModel
import com.yunshang.haile_life.data.Constants
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.ADImage
import com.yunshang.haile_life.data.entities.StoreDeviceEntity
import com.yunshang.haile_life.databinding.*
import com.yunshang.haile_life.ui.activity.shop.NearByShopActivity
import com.yunshang.haile_life.ui.activity.shop.ShopDetailActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.adapter.ImageAdapter
import com.yunshang.haile_life.utils.scheme.SchemeURLHelper
import com.yunshang.haile_life.web.WebViewActivity


/**
 * Title :
 * Author: Lsy
 * Date: 2023/4/7 13:58
 * Version: 1
 * Description:
 * History:
 * <author> <time> <version> <desc>
 * 作者姓名 修改时间 版本号 描述
 */
class HomeFragment : BaseBusinessFragment<FragmentHomeBinding, HomeViewModel>(
    HomeViewModel::class.java, BR.vm
) {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
    )

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
                mSharedViewModel.requestLocationInfo(requireContext())
            }
        }

    private val scrollChanged: OnScrollChangedListener by lazy {
        OnScrollChangedListener {
            if (mBinding.svHomeContent.scrollY <= 0) {
                mBinding.bgHomeTitle.setBackgroundColor(Color.TRANSPARENT)
            } else {
                mBinding.bgHomeTitle.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.home_top
                    )
                )
            }
        }
    }

    private val mStudentAdapter by lazy {
        CommonRecyclerAdapter<ItemHomeStudentRecommendBinding, ADImage>(
            R.layout.item_home_student_recommend,
            BR.item
        ) { mItemBinding, _, item ->
            mItemBinding?.root?.setOnClickListener {
                SchemeURLHelper.parseSchemeURL(
                    requireContext(),
                    item.linkUrl,
                    item.linkType
                )
            }
        }
    }

    override fun layoutId(): Int = R.layout.fragment_home

    override fun initEvent() {
        super.initEvent()

        // banner
        mViewModel.adEntity.observe(this) { ad ->
            if (ad.images.isEmpty()) {
                mBinding.bannerHomeBanner.visibility = View.GONE
            } else {
                mBinding.bannerHomeBanner.addBannerLifecycleObserver(this)
                    .setIndicator(CircleIndicator(requireContext()))
                    .setAdapter(ImageAdapter(
                        ad.images.sortedBy { item -> item.sortValue },
                        { it.imageUrl }) { data, _ ->
                        SchemeURLHelper.parseSchemeURL(
                            requireContext(),
                            data.linkUrl,
                            data.linkType
                        )
                    })
                mBinding.bannerHomeBanner.visibility = View.VISIBLE
            }
        }
        // 商城
        mViewModel.storeAdEntity.observe(this) { ad ->
            ad.images.firstOrNull()?.let { img ->
                mBinding.groupGoodsRecommendMore.visibility = View.VISIBLE
                mBinding.viewGoodsRecommendMore.setOnClickListener {
                    SchemeURLHelper.parseSchemeURL(
                        requireContext(),
                        img.linkUrl, img.linkType
                    )
                }
            } ?: run { mBinding.groupGoodsRecommendMore.visibility = View.GONE }
        }
        // 好物推荐
        mViewModel.goodsRecommendAdEntity.observe(this) {
            if (it.images.isEmpty()) {
                mBinding.clGoodsRecommend.visibility = View.GONE
            } else {
                mBinding.clGoodsRecommend.visibility = View.VISIBLE
                val mH = DimensionUtils.dip2px(requireContext(), 4f)
                mBinding.llGoodsRecommend.buildChild<ItemHomeGoodsRecommendBinding, ADImage>(
                    it.images.sortedBy { item -> item.sortValue },
                    LinearLayoutCompat.LayoutParams(0, LinearLayoutCompat.LayoutParams.WRAP_CONTENT)
                        .apply {
                            weight = 1f
                            setMargins(mH, 0, mH, 0)
                        }
                ) { _, childBinding, data ->
                    childBinding.item = data
                    childBinding.root.setOnClickListener {
                        SchemeURLHelper.parseSchemeURL(
                            requireContext(),
                            data.linkUrl,
                            data.linkType
                        )
                    }
                }
            }
        }
        // 学生专区
        mViewModel.studentRecommendAdEntity.observe(this) {
            if (it.images.isEmpty()) {
                mBinding.clStudentArea.visibility = View.GONE
            } else {
                mBinding.clStudentArea.visibility = View.VISIBLE
                mStudentAdapter.refreshList(it.images.sortedBy { item -> item.sortValue }
                    .toMutableList(), true)
            }
        }

        mViewModel.nearStoreEntity.observe(this) {
            mBinding.groupNearStores.visibility = View.VISIBLE
            mViewModel.requestStoreDevices()
        }
        mViewModel.storeDevices.observe(this) {
            if (it.isEmpty()) {
                mBinding.groupNearDevice.visibility = View.GONE
            } else {
                mBinding.llHomeNearStoresDevices.buildChild<ItemHomeNearStoresBinding, StoreDeviceEntity>(
                    it,
                    LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        DimensionUtils.dip2px(requireContext(), 34f)
                    ), 1
                ) { _, childBinding, data ->
                    childBinding.name = data.categoryName
                    childBinding.num = "${data.total}"
                    childBinding.status = "${data.idleCount}"
                    childBinding.nameIcon = data.shopIcon()
                }
                mBinding.groupNearDevice.visibility = View.VISIBLE
            }
        }

        mSharedViewModel.mSharedLocation.observe(this) {
            mViewModel.requestNearByStore(it)
        }
    }

    override fun initView() {
        //设置顶部距离
        mBinding.ivHomeTitle.layoutParams.let {
            (it as ConstraintLayout.LayoutParams).topMargin =
                StatusBarUtils.getStatusBarHeight() + DimensionUtils.dip2px(requireContext(), 10f)
        }
        // 滚动监听
        mBinding.svHomeContent.viewTreeObserver.addOnScrollChangedListener(scrollChanged)

        // 分类1(大图)
        mBinding.llHomeCategoryBig.buildChild<ItemHomeCategoryBigBinding, HomeCategory>(
            mViewModel.bigCategory,
            LinearLayoutCompat.LayoutParams(
                0,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
        ) { _, childBinding, data ->
            childBinding.item = data
            childBinding.root.setOnClickListener {
                when (data.icon) {
                    R.mipmap.icon_home_wash ->
                        startActivity(
                            Intent(
                                requireContext(),
                                NearByShopActivity::class.java
                            ).apply {
                                putExtras(IntentParams.DefaultPageParams.pack(1))
                            })

                    R.mipmap.icon_home_hair ->
                        startActivity(
                            Intent(
                                requireContext(),
                                NearByShopActivity::class.java
                            ).apply {
                                putExtras(IntentParams.DefaultPageParams.pack(2))
                            })
                    else -> SToast.showToast(requireContext(), "敬请期待")
                }
            }
        }

        // 分类2(小图)
        mBinding.llHomeCategorySmall.buildChild<ItemHomeCategorySmallBinding, HomeCategory>(
            mViewModel.smallCategory,
            LinearLayoutCompat.LayoutParams(
                0,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
            ).apply {
                weight = 1f
            }
        ) { _, childBinding, data ->
            childBinding.item = data
            childBinding.root.setOnClickListener {
                SToast.showToast(requireContext(), "敬请期待")
            }
        }

        // 附近门店 更多
        mBinding.clNearByShopMore.setOnClickListener {
            startActivity(Intent(requireContext(), NearByShopActivity::class.java))
        }
        // 附近门店
        mBinding.clNearByShop.setOnClickListener {
            mViewModel.nearStoreEntity.value?.let { nearStore ->
                startActivity(Intent(
                    requireContext(),
                    ShopDetailActivity::class.java
                ).apply {
                    putExtras(IntentParams.IdParams.pack(nearStore.id))
                })
            }
        }

        mBinding.ibHomeCurTaskClose.setOnClickListener {
            mBinding.clHomeCurTask.visibility = View.GONE
        }
        mBinding.btnHomeCurTaskClose.setOnClickListener {
            mBinding.clHomeCurTask.visibility = View.GONE
        }

        // 指南
        mBinding.clHomeGuideMain.setOnClickListener {
            startActivity(
                Intent(
                    requireContext(),
                    WebViewActivity::class.java
                ).apply {
                    putExtras(
                        IntentParams.WebViewParams.pack(
                            Constants.guide,
                        )
                    )
                })
        }

        // 好物推荐
        mBinding.ibGoodsRecommendClose.setOnClickListener {
            mBinding.clGoodsRecommend.visibility = View.GONE
        }

        // 吃喝玩乐（隐藏）
        mBinding.ibBeerAndSkittlesClose.setOnClickListener {
            mBinding.clBeerAndSkittles.visibility = View.GONE
        }

        // 学生专区
        mBinding.ibStudentAreaClose.setOnClickListener {
            mBinding.clStudentArea.visibility = View.GONE
        }
        mBinding.rvStudentAreaList.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        val space = DimensionUtils.dip2px(requireContext(), 8f)
        mBinding.rvStudentAreaList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position % 2 == 0) {
                    outRect.right = space / 2
                } else {
                    outRect.left = space / 2
                }
                outRect.bottom = space
            }
        })
        mBinding.rvStudentAreaList.adapter = mStudentAdapter
    }

    override fun initData() {
        requestMultiplePermission.launch(permissions)
        mViewModel.requestData()
    }

    //方法一：自己控制banner的生命周期
    override fun onStart() {
        super.onStart()
        //开始轮播
        mBinding.bannerHomeBanner.start()
    }

    override fun onStop() {
        super.onStop()
        //停止轮播
        mBinding.bannerHomeBanner.stop()
    }

    override fun onDestroyView() {
        mBinding.svHomeContent.viewTreeObserver.removeOnScrollChangedListener(scrollChanged)
        super.onDestroyView()
    }

    override fun onDestroy() {
        //销毁
        mBinding.bannerHomeBanner.destroy()
        super.onDestroy()
    }
}
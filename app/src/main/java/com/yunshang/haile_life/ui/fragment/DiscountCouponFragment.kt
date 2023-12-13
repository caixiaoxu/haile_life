package com.yunshang.haile_life.ui.fragment

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.DiscountCouponViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.DiscountCouponEntity
import com.yunshang.haile_life.databinding.FragmentCouponBinding
import com.yunshang.haile_life.databinding.ItemDiscountCouponListBinding
import com.yunshang.haile_life.ui.activity.shop.NearByShopActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.setVisibility
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView

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
class DiscountCouponFragment :
    BaseBusinessFragment<FragmentCouponBinding, DiscountCouponViewModel>(
        DiscountCouponViewModel::class.java, BR.vm
    ) {
    companion object{
        val ShowTitleTag:String = "showTitle"
    }

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemDiscountCouponListBinding, DiscountCouponEntity>(
            R.layout.item_discount_coupon_list, BR.item
        ) { mItemBinding, _, _ ->
            mItemBinding?.tvDiscountCouponRuleTitle?.setOnClickListener {
                if (mItemBinding.tvDiscountCouponRule.visibility == View.VISIBLE) {
                    mItemBinding.tvDiscountCouponRule.visibility = View.GONE
                    mItemBinding.tvDiscountCouponRuleTitle.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.mipmap.icon_small_arrow_right, 0
                    )
                } else {
                    mItemBinding.tvDiscountCouponRule.visibility = View.VISIBLE
                    mItemBinding.tvDiscountCouponRuleTitle.setCompoundDrawablesWithIntrinsicBounds(
                        0, 0, R.mipmap.icon_small_arrow_down, 0
                    )
                }
            }
            mItemBinding?.btnDiscountCouponUse?.setOnClickListener {
//                startActivity(Intent(requireContext(), MainActivity::class.java).apply {
//                    putExtras(IntentParams.DefaultPageParams.pack(0))
//                })
                startActivity(Intent(requireContext(), NearByShopActivity::class.java))
            }
        }
    }

    override fun layoutId(): Int = R.layout.fragment_coupon

    override fun initArguments() {
        super.initArguments()
        mViewModel.categoryCode = arguments?.getString(IntentParams.DeviceParams.CategoryCode)
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.curCouponStatus.observe(this) {
            mBinding.includeIndicatorList.rvIndicatorListList.requestRefresh()
        }

        mViewModel.mCouponIndicators.observe(this) { status ->
            mBinding.includeIndicatorList.indicatorIndicatorListStatus.navigator =
                CommonNavigator( requireContext()).apply {
                    isAdjustMode = true
                    adapter = object : CommonNavigatorAdapter() {
                        override fun getCount(): Int = status.size

                        override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
                            return SimplePagerTitleView(context).apply {
                                normalColor =
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.color_black_65
                                    )
                                selectedColor =
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.color_black_85
                                    )
                                status[index].run {
                                    text = title
                                    setOnClickListener {
                                        mViewModel.curCouponStatus.value = value
                                        onPageSelected(index)
                                        notifyDataSetChanged()
                                    }
                                }
                            }
                        }

                        override fun getIndicator(context: Context?): IPagerIndicator {
                            return LinePagerIndicator(context).apply {
                                mode = LinePagerIndicator.MODE_EXACTLY
                                lineWidth =
                                    DimensionUtils.dip2px( requireContext(), 45f)
                                        .toFloat()
                                setColors(
                                    ContextCompat.getColor(
                                        requireContext(),
                                        R.color.colorPrimary
                                    )
                                )
                            }
                        }

                        override fun getTitleWeight(context: Context?, index: Int): Float {
                            return 1f
                        }
                    }
                }
        }
    }

    override fun initView() {
        val showTitle = arguments?.getBoolean(ShowTitleTag,false)
        mBinding.barDiscountCouponTitle.setVisibility(showTitle)
        mBinding.viewOrderPadding.setVisibility(showTitle)
        if (true == showTitle){
            mBinding.barDiscountCouponTitle.getBackBtn().setVisibility(false)
            (mBinding.viewOrderPadding.layoutParams as ViewGroup.LayoutParams).height =
                StatusBarUtils.getStatusBarHeight()
        }

        mBinding.includeIndicatorList.rvIndicatorListList.layoutManager = LinearLayoutManager( requireContext())
        mBinding.includeIndicatorList.rvIndicatorListList.setListStatus(
            R.mipmap.icon_list_coupon_empty,
            R.string.empty_coupon
        )
        ResourcesCompat.getDrawable(resources, R.drawable.divide_size8, null)?.let {
            mBinding.includeIndicatorList.rvIndicatorListList.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.includeIndicatorList.rvIndicatorListList.adapter = mAdapter
        mBinding.includeIndicatorList.rvIndicatorListList.requestData =
            object : CommonRefreshRecyclerView.OnRequestDataListener<DiscountCouponEntity>() {
                override fun requestData(
                    isRefresh: Boolean,
                    page: Int,
                    pageSize: Int,
                    callBack: (responseList: ResponseList<out DiscountCouponEntity>?) -> Unit
                ) {
                    mViewModel.requestData(page, pageSize, callBack)
                }
            }
    }

    override fun initData() {}
}
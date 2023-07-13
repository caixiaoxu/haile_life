package com.yunshang.haile_life.ui.activity.personal

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.DiscountCouponViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.DiscountCouponEntity
import com.yunshang.haile_life.databinding.ActivityDiscountCouponBinding
import com.yunshang.haile_life.databinding.ItemDiscountCouponListBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import com.yunshang.haile_life.ui.view.refresh.CommonRefreshRecyclerView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView

class DiscountCouponActivity :
    BaseBusinessActivity<ActivityDiscountCouponBinding, DiscountCouponViewModel>(
        DiscountCouponViewModel::class.java
    ) {

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
                startActivity(Intent(this@DiscountCouponActivity, MainActivity::class.java).apply {
                    putExtras(IntentParams.DefaultPageParams.pack(0))
                })
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_discount_coupon

    override fun backBtn(): View = mBinding.includeIndicatorList.barIndicatorListTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()
        mViewModel.curCouponStatus.observe(this) {
            mBinding.includeIndicatorList.rvIndicatorListList.requestRefresh()
        }

        mViewModel.mCouponIndicators.observe(this) { status ->
            mBinding.includeIndicatorList.indicatorIndicatorListStatus.navigator =
                CommonNavigator(this).apply {
                    isAdjustMode = true
                    adapter = object : CommonNavigatorAdapter() {
                        override fun getCount(): Int = status.size

                        override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
                            return SimplePagerTitleView(context).apply {
                                normalColor =
                                    ContextCompat.getColor(
                                        this@DiscountCouponActivity,
                                        R.color.color_black_65
                                    )
                                selectedColor =
                                    ContextCompat.getColor(
                                        this@DiscountCouponActivity,
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
                                    DimensionUtils.dip2px(this@DiscountCouponActivity, 45f)
                                        .toFloat()
                                setColors(
                                    ContextCompat.getColor(
                                        this@DiscountCouponActivity,
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
        window.statusBarColor = Color.WHITE
        mBinding.includeIndicatorList.barIndicatorListTitle.setTitle(R.string.discount_coupon)

        mBinding.includeIndicatorList.rvIndicatorListList.layoutManager = LinearLayoutManager(this)
        ResourcesCompat.getDrawable(resources, R.drawable.divide_size8, null)?.let {
            mBinding.includeIndicatorList.rvIndicatorListList.addItemDecoration(
                DividerItemDecoration(
                    this@DiscountCouponActivity,
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

    override fun initData() {
    }
}
package com.yunshang.haile_life.ui.activity.marketing

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.utils.DimensionUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.DiscountCouponSelectorViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.TradePreviewParticipate
import com.yunshang.haile_life.databinding.ActivityDiscountCouponSelectorBinding
import com.yunshang.haile_life.databinding.ItemDiscountCouponBinding
import com.yunshang.haile_life.ui.activity.BaseBusinessActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView

class DiscountCouponSelectorActivity :
    BaseBusinessActivity<ActivityDiscountCouponSelectorBinding, DiscountCouponSelectorViewModel>(
        DiscountCouponSelectorViewModel::class.java, BR.vm
    ) {

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemDiscountCouponBinding, TradePreviewParticipate>(
            R.layout.item_discount_coupon, BR.item
        ) { mItemBinding, pos, item ->
            mViewModel.selectCouponIndicator.observe(this) {
                mItemBinding?.canUse = 1 == it
            }

            (mItemBinding?.root?.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
                it.topMargin = if (0 == pos) DimensionUtils.dip2px(
                    this@DiscountCouponSelectorActivity,
                    12f
                ) else 0
            }

            mItemBinding?.root?.setOnClickListener {
                if (1 == mViewModel.selectCouponIndicator.value){
                    item.isCheck = !item.isCheck
                    // 移除之前旧数据
                    mViewModel.selectParticipate?.clear()
                    if (item.isCheck) {
                        mViewModel.selectParticipate?.add(item)
                    }
                    mViewModel.requestData()
                }
            }
        }
    }

    override fun layoutId(): Int = R.layout.activity_discount_coupon_selector

    override fun backBtn(): View = mBinding.barDiscountCouponSelectorTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.orderNo = IntentParams.DiscountCouponSelectorParams.parseOrderNo(intent)
        mViewModel.promotionProduct =
            IntentParams.DiscountCouponSelectorParams.parsePromotionProduct(intent)
        mViewModel.selectParticipate =
            IntentParams.DiscountCouponSelectorParams.parseSelectCoupon(intent)
        mViewModel.otherSelectParticipate =
            IntentParams.DiscountCouponSelectorParams.parseOtherSelectCoupon(intent)
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.mCouponIndicators.observe(this) { status ->
            mBinding.indicatorDiscountCouponStatus.navigator =
                CommonNavigator(this@DiscountCouponSelectorActivity).apply {
                    isAdjustMode = true
                    adapter = object : CommonNavigatorAdapter() {
                        override fun getCount(): Int = status.size

                        override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
                            return SimplePagerTitleView(context).apply {
                                normalColor =
                                    ContextCompat.getColor(
                                        this@DiscountCouponSelectorActivity,
                                        R.color.color_black_65
                                    )
                                selectedColor =
                                    ContextCompat.getColor(
                                        this@DiscountCouponSelectorActivity,
                                        R.color.color_black_85
                                    )
                                status[index].run {
                                    text = title + if (num > 0) "（${num}张）" else ""
                                    setOnClickListener {
                                        mViewModel.selectCouponIndicator.value = value
                                        notifyPromotionList()
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
                                    DimensionUtils.dip2px(this@DiscountCouponSelectorActivity, 45f)
                                        .toFloat()
                                setColors(
                                    ContextCompat.getColor(
                                        this@DiscountCouponSelectorActivity,
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

        mViewModel.tradePreview.observe(this) {
            it?.let { trade ->
                // 已选优惠
                trade.promotionList.find { item -> mViewModel.promotionProduct == item.promotionProduct }
                    ?.let { promotion ->
                        mViewModel.promotion.value = promotion
                        mViewModel.selectParticipate =
                            mutableListOf<TradePreviewParticipate>().apply {
                                if (promotion.participateList.isNotEmpty()) {
                                    addAll(promotion.participateList)
                                }
                            }
                        notifyPromotionList()
                    }
            }
        }
    }

    /**
     * 刷新优惠券数据
     */
    private fun notifyPromotionList() {
        mViewModel.tradePreview.value?.promotionList?.find { item -> mViewModel.promotionProduct == item.promotionProduct }
            ?.let { promotion ->
                val list =
                    promotion.options?.filter { it.available == (1 == mViewModel.selectCouponIndicator.value) }
                if (list.isNullOrEmpty()) {
                    mBinding.rvDiscountCouponSelectorList.changeStatusView(true)
                } else {
                    mBinding.rvDiscountCouponSelectorList.changeStatusView(false)
                    mAdapter.refreshList(list.also {
                        val idList = promotion.participateList.map { item ->
                            "${item.promotionId}-${item.assetId}-${item.shopId}"
                        }
                        list.forEach { item ->
                            item.isCheck =
                                "${item.promotionId}-${item.assetId}-${item.shopId}" in idList
                        }
                    }.toMutableList(), true)
                }
            }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.rvDiscountCouponSelectorList.recyclerView.layoutManager = LinearLayoutManager(this)
        mBinding.rvDiscountCouponSelectorList.setListStatus(
            R.mipmap.icon_list_coupon_empty,
            R.string.empty_coupon
        )
        ResourcesCompat.getDrawable(resources, R.drawable.divide_size8, null)?.let {
            mBinding.rvDiscountCouponSelectorList.recyclerView.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.rvDiscountCouponSelectorList.recyclerView.adapter = mAdapter

        mBinding.btnDiscountCouponSelectorSure.setOnClickListener {
            setResult(RESULT_OK, Intent().apply {
                mViewModel.selectParticipate?.let { list ->
                    putExtras(
                        IntentParams.DiscountCouponSelectorParams.pack(
                            list,
                            mViewModel.promotionProduct
                        )
                    )
                }
            })
            finish()
        }
    }

    override fun initData() {
        mViewModel.requestData()
    }
}
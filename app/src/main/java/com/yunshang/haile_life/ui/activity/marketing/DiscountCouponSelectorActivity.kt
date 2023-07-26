package com.yunshang.haile_life.ui.activity.marketing

import android.content.Intent
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
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

class DiscountCouponSelectorActivity :
    BaseBusinessActivity<ActivityDiscountCouponSelectorBinding, DiscountCouponSelectorViewModel>(
        DiscountCouponSelectorViewModel::class.java, BR.vm
    ) {

    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemDiscountCouponBinding, TradePreviewParticipate>(
            R.layout.item_discount_coupon, BR.item
        ) { mItemBinding, pos, item ->
            (mItemBinding?.root?.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
                it.topMargin = if (0 == pos) DimensionUtils.dip2px(this@DiscountCouponSelectorActivity, 12f) else 0
            }

            mItemBinding?.root?.setOnClickListener {
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

    override fun layoutId(): Int = R.layout.activity_discount_coupon_selector

    override fun backBtn(): View = mBinding.barDiscountCouponSelectorTitle.getBackBtn()

    override fun initIntent() {
        super.initIntent()
        mViewModel.goods = IntentParams.OrderSubmitParams.parseGoods(intent) ?: mutableListOf()
        mViewModel.promotionProduct =
            IntentParams.DiscountCouponSelectorParams.parsePromotionProduct(intent)
        mViewModel.selectParticipate =
            IntentParams.DiscountCouponSelectorParams.parseSelectCoupon(intent)
    }

    override fun initEvent() {
        super.initEvent()
        mViewModel.tradePreview.observe(this) {
            it?.let { trade ->
                // 已选优惠
                trade.promotionList.find { item -> mViewModel.promotionProduct == item.promotionProduct }
                    ?.let { promotion ->
                        mViewModel.selectParticipate =
                            mutableListOf<TradePreviewParticipate>().apply {
                                if (promotion.participateList.isNotEmpty()) {
                                    addAll(promotion.participateList)
                                }
                            }
                        if (promotion.options.isNotEmpty()) {
                            mAdapter.refreshList(promotion.options.filter { item -> item.available }
                                .also { list ->
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
        }
    }

    override fun initView() {
        window.statusBarColor = Color.WHITE

        mBinding.rvDiscountCouponSelectorList.layoutManager = LinearLayoutManager(this)
        ResourcesCompat.getDrawable(resources, R.drawable.divide_size8, null)?.let {
            mBinding.rvDiscountCouponSelectorList.addItemDecoration(
                DividerItemDecoration(
                    this,
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.rvDiscountCouponSelectorList.adapter = mAdapter

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
package com.yunshang.haile_life.ui.fragment

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.databinding.FragmentOrderBinding
import com.yunshang.haile_life.databinding.ItemMineOrderBinding
import com.yunshang.haile_life.ui.activity.order.OrderDetailActivity
import com.yunshang.haile_life.ui.view.adapter.CommonRecyclerAdapter
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
class OrderFragment : BaseBusinessFragment<FragmentOrderBinding, OrderViewModel>(
    OrderViewModel::class.java, BR.vm
) {
    private val mAdapter by lazy {
        CommonRecyclerAdapter<ItemMineOrderBinding, OrderEntity>(
            R.layout.item_mine_order, BR.item
        ) { mItemBinding, _, item ->
            mItemBinding?.root?.setOnClickListener {
                startActivity(Intent(requireContext(), OrderDetailActivity::class.java).apply {
                    putExtras(IntentParams.OrderParams.pack(item.orderNo))
                })
            }
        }
    }

    override fun layoutId(): Int = R.layout.fragment_order

    override fun initEvent() {
        super.initEvent()
        mViewModel.curOrderStatus.observe(this) {
            mBinding.rvMineOrderList.requestRefresh()
        }
        mViewModel.orderStatus.observe(this) { list ->
            mBinding.indicatorMineOrderStatus.navigator = CommonNavigator(requireContext()).apply {
                adapter = object : CommonNavigatorAdapter() {
                    override fun getCount(): Int = list.size

                    override fun getTitleView(context: Context?, index: Int): IPagerTitleView {
                        return SimplePagerTitleView(context).apply {
                            normalColor =
                                ContextCompat.getColor(requireContext(), R.color.color_black_65)
                            selectedColor =
                                ContextCompat.getColor(requireContext(), R.color.color_black_85)
                            list[index].run {
                                text = title
                                setOnClickListener {
                                    mViewModel.curOrderStatus.value = value
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
                                    requireContext(),
                                    R.color.colorPrimary
                                )
                            )
                        }
                    }
                }
            }
        }

        LiveDataBus.with(BusEvents.ORDER_SUBMIT_STATUS)?.observe(this){
            mBinding.rvMineOrderList.requestRefresh()
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS)?.observe(this){
            mBinding.rvMineOrderList.requestRefresh()
        }
    }

    override fun initView() {
        //设置顶部距离
        (mBinding.viewOrderPadding.layoutParams as ViewGroup.LayoutParams).height =
            StatusBarUtils.getStatusBarHeight()
        mBinding.barMineOrderTitle.getRightBtn().run {
            setText(R.string.appointment_order)
            setOnClickListener {
                // 跳转预约订单
            }
        }

        mBinding.rvMineOrderList.layoutManager = LinearLayoutManager(requireContext())
        ResourcesCompat.getDrawable(resources, R.drawable.divide_size8, null)?.let {
            mBinding.rvMineOrderList.addItemDecoration(
                DividerItemDecoration(
                    requireContext(),
                    DividerItemDecoration.VERTICAL
                ).apply {
                    setDrawable(it)
                })
        }
        mBinding.rvMineOrderList.adapter = mAdapter
        mBinding.rvMineOrderList.requestData =
            object : CommonRefreshRecyclerView.OnRequestDataListener<OrderEntity>() {
                override fun requestData(
                    isRefresh: Boolean,
                    page: Int,
                    pageSize: Int,
                    callBack: (responseList: ResponseList<out OrderEntity>?) -> Unit
                ) {
                    mViewModel.requestOrderList(page, pageSize, callBack)
                }
            }
    }

    override fun initData() {
    }

}
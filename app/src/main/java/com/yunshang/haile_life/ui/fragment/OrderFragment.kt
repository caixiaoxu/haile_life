package com.yunshang.haile_life.ui.fragment

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lsy.framelib.async.LiveDataBus
import com.lsy.framelib.network.response.ResponseList
import com.lsy.framelib.utils.DimensionUtils
import com.lsy.framelib.utils.StatusBarUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.event.BusEvents
import com.yunshang.haile_life.business.vm.OrderViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.OrderEntity
import com.yunshang.haile_life.databinding.FragmentOrderBinding
import com.yunshang.haile_life.databinding.ItemMineOrderBinding
import com.yunshang.haile_life.ui.activity.order.AppointmentOrderActivity
import com.yunshang.haile_life.ui.activity.order.OrderDetailActivity
import com.yunshang.haile_life.ui.activity.order.OrderListActivity
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
        ) { mItemBinding, pos, item ->
            mItemBinding?.isAppoint = mViewModel.isAppoint
            (mItemBinding?.root?.layoutParams as? MarginLayoutParams)?.let {
                it.topMargin = if (0 == pos) DimensionUtils.dip2px(requireContext(), 12f) else 0
            }
            mItemBinding?.root?.setOnClickListener {
                when (item.orderSubType) {
                    106 -> {
                        if (50 == item.state) {
                            when (item.checkInfo?.checkState) {
                                1 -> {
                                    // 待验证
                                    goToAppointmentOrderPage(item.orderNo)
                                }
                                2 -> {
                                    // 支付
                                    goToAppointmentOrderPage(item.orderNo)
                                }
                                else -> goToNormalOrderPage(item.orderNo)
                            }
                        } else goToNormalOrderPage(item.orderNo)
                    }
                    301 -> {
                        if (500 == item.state) {
                            if (1 == item.appointmentState) {
                                // 预约成功
                                goToAppointmentOrderPage(item.orderNo)
                            } else if (5 == item.appointmentState && 1 == item.checkInfo?.checkState) {
                                // 待验证
                            } else goToNormalOrderPage(item.orderNo)
                        } else if (50 == item.state) {
                            // 待支付
                            goToAppointmentOrderPage(item.orderNo)
                        } else goToNormalOrderPage(item.orderNo)
                    }
                    303 -> {
                        if (50 == item.state) {
                            // 待支付
                            when (item.appointmentState) {
                                1 -> {
                                    // 预约成功
                                    goToAppointmentOrderPage(item.orderNo)
                                }
                                5 -> {
                                    when (item.checkInfo?.checkState) {
                                        1 -> {
                                            // 待验证
                                            goToAppointmentOrderPage(item.orderNo)
                                        }
                                        2 -> {
                                            // 待支付
                                            goToAppointmentOrderPage(item.orderNo)
                                        }
                                        else -> goToNormalOrderPage(item.orderNo)
                                    }
                                }
                                else -> goToNormalOrderPage(item.orderNo)
                            }
                        } else goToNormalOrderPage(item.orderNo)
                    }
                    else -> {
                        goToNormalOrderPage(item.orderNo)
                    }
                }
            }
        }
    }

    private fun goToAppointmentOrderPage(orderNo: String) {
        startActivity(
            Intent(
                requireContext(),
                AppointmentOrderActivity::class.java
            ).apply {
                putExtras(IntentParams.OrderParams.pack(orderNo))
            }
        )
    }

    private fun goToNormalOrderPage(orderNo: String) {
        startActivity(
            Intent(
                requireContext(),
                OrderDetailActivity::class.java
            ).apply {
                putExtras(
                    IntentParams.OrderParams.pack(
                        orderNo,
                        mViewModel.isAppoint
                    )
                )
            })
    }

    override fun layoutId(): Int = R.layout.fragment_order

    override fun initEvent() {
        super.initEvent()
        mViewModel.curOrderStatus.observe(this) {
            mBinding.rvMineOrderList.requestRefresh()
        }

        if (!mViewModel.isAppoint) {
            mViewModel.orderStatus.observe(this) { list ->
                mBinding.indicatorMineOrderStatus.navigator =
                    CommonNavigator(requireContext()).apply {
                        adapter = object : CommonNavigatorAdapter() {
                            override fun getCount(): Int = list.size

                            override fun getTitleView(
                                context: Context?,
                                index: Int
                            ): IPagerTitleView {
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
                // 初始化选中状态
                mViewModel.curOrderStatus.value?.let { status ->
                    when (status) {
                        100 -> mBinding.indicatorMineOrderStatus.navigator.onPageSelected(1)
                        500 -> mBinding.indicatorMineOrderStatus.navigator.onPageSelected(2)
                        1000 -> mBinding.indicatorMineOrderStatus.navigator.onPageSelected(3)
                        2099 -> mBinding.indicatorMineOrderStatus.navigator.onPageSelected(4)
                    }
                }
            }
        }

        LiveDataBus.with(BusEvents.ORDER_SUBMIT_STATUS)?.observe(this) {
            mBinding.rvMineOrderList.requestRefresh()
        }

        LiveDataBus.with(BusEvents.APPOINT_ORDER_USE_STATUS)?.observe(this) {
            mBinding.rvMineOrderList.requestRefresh()
        }

        LiveDataBus.with(BusEvents.PAY_SUCCESS_STATUS)?.observe(this) {
            mBinding.rvMineOrderList.requestRefresh()
        }
        LiveDataBus.with(BusEvents.PAY_OVERTIME_STATUS)?.observe(this) {
            mBinding.rvMineOrderList.requestRefresh()
        }

        LiveDataBus.with(BusEvents.ORDER_CANCEL_STATUS)?.observe(this) {
            mBinding.rvMineOrderList.requestRefresh()
        }

        LiveDataBus.with(BusEvents.ORDER_DELETE_STATUS, String::class.java)?.observe(this) {
            mAdapter.deleteItem { item -> item.orderNo == it }
        }
    }

    override fun initArguments() {
        mViewModel.isAppoint = IntentParams.OrderListParams.parseIsAppoint(arguments)
    }

    override fun initView() {
        val isMain = arguments?.let { IntentParams.OrderListParams.parseIsMain(it) } != false
        if (isMain) {
            (mBinding.viewOrderPadding.layoutParams as ViewGroup.LayoutParams).height =
                StatusBarUtils.getStatusBarHeight()
        } else {
            mBinding.viewOrderPadding.visibility = View.GONE
        }
        mBinding.barMineOrderTitle.getBackBtn().visibility =
            if (!isMain) View.VISIBLE else View.GONE
        mBinding.barMineOrderTitle.getBackBtn().setOnClickListener {
            activity?.finish()
        }

        mBinding.barMineOrderTitle.setTitle(if (mViewModel.isAppoint) R.string.appointment_order else R.string.mine_order)

        if (!mViewModel.isAppoint) {
            mBinding.barMineOrderTitle.getRightBtn().run {
                setText(R.string.appointment_order)
                setOnClickListener {
                    // 跳转预约订单
                    startActivity(
                        Intent(requireContext(), OrderListActivity::class.java).apply {
                            putExtras(IntentParams.OrderListParams.pack(isAppoint = true))
                        }
                    )
                }
            }
        }

        mBinding.indicatorMineOrderStatus.visibility =
            if (mViewModel.isAppoint) View.GONE else View.VISIBLE

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
        mViewModel.curOrderStatus.value =
            arguments?.let { IntentParams.OrderListParams.parseStatus(it) }
    }
}
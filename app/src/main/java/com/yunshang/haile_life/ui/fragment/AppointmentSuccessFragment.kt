package com.yunshang.haile_life.ui.fragment

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.lsy.framelib.utils.StatusBarUtils
import com.lsy.framelib.utils.StringUtils
import com.yunshang.haile_life.BR
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.AppointmentOrderViewModel
import com.yunshang.haile_life.business.vm.AppointmentSuccessViewModel
import com.yunshang.haile_life.data.entities.DeviceStateEntity
import com.yunshang.haile_life.data.entities.OrderItem
import com.yunshang.haile_life.data.entities.PromotionParticipation
import com.yunshang.haile_life.databinding.FragmentAppointmentSuccessBinding
import com.yunshang.haile_life.databinding.IncludeAppointmentDeviceStatusProgressBinding
import com.yunshang.haile_life.databinding.IncludeOrderInfoItemBinding
import com.yunshang.haile_life.databinding.ItemDeviceStatusProgressBinding
import com.yunshang.haile_life.ui.view.adapter.ViewBindingAdapter.visibility
import com.yunshang.haile_life.ui.view.dialog.CommonDialog

class AppointmentSuccessFragment :
    BaseBusinessFragment<FragmentAppointmentSuccessBinding, AppointmentSuccessViewModel>(
        AppointmentSuccessViewModel::class.java, BR.vm
    ) {

    private val mActivityViewModel by lazy {
        getActivityViewModelProvider(requireActivity())[AppointmentOrderViewModel::class.java]
    }

    override fun layoutId(): Int = R.layout.fragment_appointment_success

    override fun backBtn(): View = mBinding.barAppointmentSuccessTitle.getBackBtn()

    override fun initEvent() {
        super.initEvent()
        mActivityViewModel.orderDetails.observe(this) { detail ->
            detail?.let {
                mBinding.includeOrderInfo.llOrderInfoItems.buildChild<IncludeOrderInfoItemBinding, OrderItem>(
                    detail.orderItemList
                ) { index, childBinding, data ->
                    childBinding.title =
                        if (0 == index) StringUtils.getString(R.string.service) + "：" else ""
                    childBinding.content = "${data.goodsItemName} ${data.unit}${data.unitValue}"
                    childBinding.tail =
                        com.yunshang.haile_life.utils.string.StringUtils.formatAmountStrOfStr(data.originPrice)
                }
                mBinding.includeOrderInfo.llOrderInfoPromotions.buildChild<IncludeOrderInfoItemBinding, PromotionParticipation>(
                    detail.promotionParticipationList
                ) { index, childBinding, data ->
                    childBinding.title =
                        if (0 == index) StringUtils.getString(R.string.discounts) + "：" else ""
                    childBinding.content = data.promotionProductName
                    childBinding.tail = data.getOrderDeviceDiscountPrice()
                }
            }
        }

        mBinding.includeAppointmentDeviceStatus.tvDeviceStatusRefresh.setOnClickListener {
            mViewModel.requestData(mActivityViewModel.orderNo)
        }
        mViewModel.appointStateList.observe(this) { stateList ->
            stateList?.let {
                val list = arrayListOf<List<DeviceStateEntity>>()
                if (!stateList.underwayList.isNullOrEmpty()) {
                    list.add(stateList.underwayList)
                }
                if (!stateList.inLineList.isNullOrEmpty()) {
                    list.add(stateList.inLineList)
                }
                mBinding.includeAppointmentDeviceStatus.root.visibility(list.isNotEmpty())
                mBinding.includeAppointmentDeviceStatus.llDeviceStatusItems.buildChild<IncludeAppointmentDeviceStatusProgressBinding, List<DeviceStateEntity>>(
                    list
                ) { index, childBinding, data ->
                    childBinding.index = index
                    childBinding.status =
                        if (0 == index && !stateList.underwayList.isNullOrEmpty()) "正在进行" else "排队中"
                    val size = data.size - 1
                    childBinding.llDeviceStateProgress.buildChild<ItemDeviceStatusProgressBinding, DeviceStateEntity>(
                        data,
                        LinearLayoutCompat.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                        ).apply {
                            weight = 1f
                        }
                    ) { progressIndex, childProgressBinding, state ->
                        childProgressBinding.isFirst = 0 == progressIndex
                        childProgressBinding.isLast = progressIndex == size
                        childProgressBinding.item = state
                    }
                }
            }
        }
    }

    override fun initView() {
        mBinding.avm = mActivityViewModel
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)
        mBinding.includeAppointmentDeviceStatus.root.setBackgroundResource(R.drawable.shape_sffffff_r8)

        mBinding.btnAppointmentSuccessCancel.setOnClickListener {
            CommonDialog.Builder("是否结束订单？").apply {
                negativeTxt = StringUtils.getString(R.string.no)
                setPositiveButton(StringUtils.getString(R.string.yes)) {
                    mActivityViewModel.cancelOrder()
                }
            }.build().show(childFragmentManager)
        }
    }

    override fun initData() {
        mViewModel.requestData(mActivityViewModel.orderNo)
        Handler(Looper.getMainLooper()).postDelayed({
            mActivityViewModel.checkLineUp()
        }, 3000)
    }
}
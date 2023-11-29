package com.yunshang.haile_life.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.library.baseAdapters.BR
import com.lsy.framelib.utils.SToast
import com.lsy.framelib.utils.StatusBarUtils
import com.lsy.framelib.utils.StringUtils
import com.lsy.framelib.utils.SystemPermissionHelper
import com.yunshang.haile_life.R
import com.yunshang.haile_life.business.vm.DeviceSelfCleaningViewModel
import com.yunshang.haile_life.business.vm.OrderStatusViewModel
import com.yunshang.haile_life.data.agruments.IntentParams
import com.yunshang.haile_life.data.entities.OrderItem
import com.yunshang.haile_life.data.entities.PromotionParticipation
import com.yunshang.haile_life.data.extend.toRemove0Str
import com.yunshang.haile_life.databinding.FragmentDeviceSelfCleaningBinding
import com.yunshang.haile_life.databinding.IncludeOrderInfoItemBinding
import com.yunshang.haile_life.ui.activity.MainActivity
import com.yunshang.haile_life.ui.view.dialog.CommonDialog
import com.yunshang.haile_life.ui.view.dialog.Hint3SecondDialog


class DeviceSelfCleaningFragment :
    BaseBusinessFragment<FragmentDeviceSelfCleaningBinding, DeviceSelfCleaningViewModel>(
        DeviceSelfCleaningViewModel::class.java, BR.vm
    ) {

    val mActivityViewModel by lazy {
        getActivityViewModelProvider(requireActivity())[OrderStatusViewModel::class.java]
    }


    // 拨打电话权限
    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result.values.any { it }) {
                // 授权权限成功
                mActivityViewModel.orderDetails.value?.serviceTelephone?.let {
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$it")))
                }
            } else {
                // 授权失败
                SToast.showToast(requireContext(), R.string.empty_permission)
            }
        }

    override fun layoutId(): Int = R.layout.fragment_device_self_cleaning

    override fun backBtn(): View = mBinding.barOrderDeviceSelfCleaningTitle.getBackBtn()

    override fun onBackListener() {
        requireActivity().finish()
        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
            putExtras(IntentParams.DefaultPageParams.pack(3))
        })
    }

    override fun initEvent() {
        super.initEvent()

        mActivityViewModel.orderDetails.observe(this) { detail ->
            detail?.let {
                mBinding.includeOrderInfo.llOrderInfoItems.buildChild<IncludeOrderInfoItemBinding, OrderItem>(
                    detail.orderItemList
                ) { index, childBinding, data ->
                    childBinding.title =
                        if (0 == index) StringUtils.getString(R.string.service) + "：" else ""
                    childBinding.content =
                        "${data.goodsItemName} ${data.unit.toRemove0Str()}${data.unitValue}"
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
    }

    override fun initView() {
        mBinding.avm = mActivityViewModel
        mBinding.root.setPadding(0, StatusBarUtils.getStatusBarHeight(), 0, 0)

        mBinding.refreshView.requestData = {
            mActivityViewModel.requestData()
        }

        if (true != mActivityViewModel.orderDetails.value?.fulfillInfo?.selfCleanFinish()
            && true != mViewModel.inValidOrder.value
        ) {
            mBinding.ivDeviceSelfCleaningMainRun.startAnimation(
                RotateAnimation(
                    0f,
                    360f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                ).apply {
                    duration = 2400
                    interpolator = LinearInterpolator()
                    repeatCount = Animation.INFINITE
                })
        }

        mBinding.btnDeviceSelfCleaningStart.setOnClickListener {
            mViewModel.startOrderDevice(
                mActivityViewModel.orderNo,
                mActivityViewModel.orderDetails.value?.fulfillInfo?.fulfillingItem?.fulfillId
            ) {
                Hint3SecondDialog.Builder("设备已启动").apply {
                    dialogBgResource = R.drawable.shape_dialog_bg
                }.build().show(childFragmentManager)

                Handler(Looper.getMainLooper()).postDelayed({
                    mActivityViewModel.requestData(false)
                }, 2000)
            }
        }

        mBinding.tvDeviceSelfCleaningCoerceDevice.setOnClickListener {
            if (true == mActivityViewModel.orderDetails.value?.fulfillInfo?.selfCleanFinish()) {
                SToast.showToast(requireContext(), "请点击开始洗涤")
                return@setOnClickListener
            }
            CommonDialog.Builder("您确定要强制启动该设备？").apply {
                dialogBgResource = R.drawable.shape_dialog_bg
                negativeTxt = StringUtils.getString(R.string.cancel)
                setPositiveButton(StringUtils.getString(R.string.sure)) {
                    mViewModel.coerceDevice(
                        requireContext(),
                        mActivityViewModel.orderNo,
                        mActivityViewModel.orderDetails.value?.fulfillInfo?.fulfillingItem?.fulfillId
                    )
                }
            }.build().show(childFragmentManager)
        }

        mBinding.tvDeviceSelfCleaningContactShop.setOnClickListener {
            requestPermission.launch(SystemPermissionHelper.callPhonePermissions())
        }
    }

    override fun initData() {
        if (true == mActivityViewModel.orderDetails.value?.fulfillInfo?.selfCleanFinish()) {
            // 失效时间
            mViewModel.validTime =
                mActivityViewModel.orderDetails.value?.fulfillInfo?.fulfillingItem?.finishTimeTimeStamp
                    ?: 1
            mViewModel.checkValidTime()
        } else {
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    mActivityViewModel.requestData(false)
                },
                mActivityViewModel.orderDetails.value?.fulfillInfo?.fulfillingItem?.finishTimeTimeStamp?.let {
                    if (it % 60 > 0) it % 60 * 1000L
                    else 60000L
                } ?: 60000L)
        }
    }
}